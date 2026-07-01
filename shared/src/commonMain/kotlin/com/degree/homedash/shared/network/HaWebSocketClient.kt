package com.degree.homedash.shared.network

import co.touchlab.kermit.Logger
import com.degree.homedash.shared.data.HaConfig
import com.degree.homedash.shared.model.EntityState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Maintains a live connection to Home Assistant's WebSocket API, exposing the current entity
 * states and connection status as [StateFlow]s and reconnecting automatically with backoff.
 */
class HaWebSocketClient(
    private val clientFactory: () -> HttpClient = { createHttpClient { install(WebSockets) } },
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("HaWebSocketClient")

    private val _states = MutableStateFlow<Map<String, EntityState>>(emptyMap())
    val states: StateFlow<Map<String, EntityState>> = _states.asStateFlow()

    private val _connection = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connection: StateFlow<ConnectionStatus> = _connection.asStateFlow()

    // All `result` messages are re-broadcast here so request() can await its matching id.
    private val results = MutableSharedFlow<String>(extraBufferCapacity = 16)

    private val idMutex = Mutex()
    private var lastId = 0L
    private var sessionJob: Job? = null

    // The active session, used by callService to push commands while connected.
    private var session: DefaultClientWebSocketSession? = null

    private suspend fun nextId(): Long = idMutex.withLock { ++lastId }

    fun start(config: HaConfig) {
        sessionJob?.cancel()
        sessionJob = scope.launch {
            var backoffMs = 1_000L
            while (isActive) {
                try {
                    _connection.value = ConnectionStatus.Connecting
                    runSession(config)
                    backoffMs = 1_000L
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.e(e) { "WebSocket session ended with error" }
                    _connection.value = ConnectionStatus.Error(e.message)
                }
                if (!isActive) break
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(15_000L)
            }
            _connection.value = ConnectionStatus.Disconnected
        }
    }

    fun stop() {
        sessionJob?.cancel()
        sessionJob = null
        _connection.value = ConnectionStatus.Disconnected
    }

    /** Fire a service call if connected; silently dropped while disconnected. */
    suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    ) {
        val active = session ?: return
        active.send(Frame.Text(HaProtocol.encodeCallService(nextId(), domain, service, entityId, serviceData)))
    }

    /** Send a command (built with the allocated id) and await its matching `result` message. */
    suspend fun request(buildCommand: (Long) -> String): String = coroutineScope {
        val id = nextId()
        // Subscribe before sending (UNDISPATCHED) so we can't miss a fast reply.
        val awaiter = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(20_000L) {
                results.first { HaProtocol.resultId(it) == id }
            }
        }
        val active = session ?: run {
            awaiter.cancel()
            throw HaException("Not connected")
        }
        active.send(Frame.Text(buildCommand(id)))
        awaiter.await()
    }

    private suspend fun runSession(config: HaConfig) {
        val client = clientFactory()
        val wsUrl = config.webSocketUrl()
        try {
            log.i { "opening WebSocket: $wsUrl" }
            client.webSocket(wsUrl) {
                log.i { "WebSocket open; awaiting auth_required" }
                // 1. Auth handshake.
                (incoming.receive() as Frame.Text).readText() // auth_required
                log.i { "auth_required received; sending auth" }
                send(Frame.Text(HaProtocol.encodeAuth(config.token)))
                val authResp = (incoming.receive() as Frame.Text).readText()
                log.i { "auth response: ${HaProtocol.messageType(authResp)}" }
                if (HaProtocol.messageType(authResp) != "auth_ok") {
                    throw HaException("Authentication failed (${HaProtocol.messageType(authResp)})")
                }
                _connection.value = ConnectionStatus.Connected
                log.i { "connected; seeding states + subscribing" }

                // 2. Seed current states + subscribe to live changes.
                val statesId = nextId()
                send(Frame.Text(HaProtocol.encodeGetStates(statesId)))
                send(Frame.Text(HaProtocol.encodeSubscribeStateChanged(nextId())))

                // 3. Expose this session for outbound service calls.
                session = this

                // 4. Receive loop (ends when the server/network closes the channel).
                try {
                    while (true) {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) handleMessage(frame.readText(), statesId)
                    }
                } catch (e: ClosedReceiveChannelException) {
                    // remote closed the connection; outer loop will reconnect.
                } finally {
                    session = null
                }
            }
        } finally {
            session = null
            client.close()
        }
    }

    private fun handleMessage(
        text: String,
        statesId: Long,
    ) {
        when (HaProtocol.messageType(text)) {
            "result" -> {
                results.tryEmit(text) // let any pending request() match by id
                if (HaProtocol.resultId(text) == statesId && HaProtocol.isResultSuccess(text)) {
                    val list = HaProtocol.parseStates(text)
                    if (list.isNotEmpty()) _states.value = list.associateBy { it.entityId }
                }
            }
            "event" -> {
                val change = HaProtocol.parseStateChanged(text) ?: return
                _states.update { current ->
                    if (change.newState == null) current - change.entityId
                    else current + (change.entityId to change.newState)
                }
            }
        }
    }
}
