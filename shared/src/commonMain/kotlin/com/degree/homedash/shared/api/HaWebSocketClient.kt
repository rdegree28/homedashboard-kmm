package com.degree.homedash.shared.api

import co.touchlab.kermit.Logger
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.states.ExpEntityState
import com.degree.homedash.shared.model.states.LightState
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maintains a live connection to Home Assistant's WebSocket API, exposing the current entity
 * states and connection status as [kotlinx.coroutines.flow.StateFlow]s and reconnecting automatically with backoff.
 */
internal class HaWebSocketClient(
    private val clientFactory: () -> HttpClient = { createHttpClient { install(WebSockets) } },
) : HaClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("HaWebSocketClient")

    private val _states = MutableStateFlow<Map<String, EntityState>>(emptyMap())
    override val states: StateFlow<Map<String, EntityState>> = _states.asStateFlow()

    private val _expStates = MutableStateFlow<Map<String, ExpEntityState>>(emptyMap())
    override val expStates: StateFlow<Map<String, ExpEntityState>> = _expStates.asStateFlow()

    private val _connection = MutableStateFlow<HaConnectionStatus>(HaConnectionStatus.Disconnected)
    override val connection: StateFlow<HaConnectionStatus> = _connection.asStateFlow()

    // All `result` messages are re-broadcast here so request() can await its matching id. The id is
    // extracted once by the receive loop: awaiters see every result, so matching on a pre-read id
    // keeps them from re-parsing each payload (the get_states reply is a few hundred KB).
    private val results = MutableSharedFlow<ResultMessage>(extraBufferCapacity = 16)

    /** A `result` frame with its id already read off, plus the raw text for the caller to parse. */
    private data class ResultMessage(val id: Long?, val text: String)

    // Toggle-capable ExpEntityStates carry the action api that drives them; the api only needs a
    // HaClient, so this client supplies itself rather than the graph handing one back in.
    private val actionApi by lazy { WebSocketHomeAssistantActionApi(this) }

    private val idMutex = Mutex()
    private var lastId = 0L
    private var sessionJob: Job? = null

    // The active session, used by callService to push commands while connected.
    private var session: DefaultClientWebSocketSession? = null

    private suspend fun nextId(): Long = idMutex.withLock { ++lastId }

    override fun start(config: HaConfig) {
        sessionJob?.cancel()
        sessionJob = scope.launch {
            var backoffMs = 1_000L.milliseconds
            while (isActive) {
                try {
                    _connection.value = HaConnectionStatus.Connecting
                    runSession(config)
                    backoffMs = 1_000L.milliseconds
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.e(e) { "WebSocket session ended with error" }
                    _connection.value = HaConnectionStatus.Error(e.message)
                }
                if (!isActive) break
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(15_000L.milliseconds)
            }
            _connection.value = HaConnectionStatus.Disconnected
        }
    }

    override fun stop() {
        sessionJob?.cancel()
        sessionJob = null
        _connection.value = HaConnectionStatus.Disconnected
    }

    /** Fire a service call if connected; silently dropped while disconnected. */
    override suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?,
    ) {
        val active = session ?: return
        active.send(Frame.Text(HaProtocolHelper.encodeCallService(nextId(), domain, service, entityId, serviceData)))
    }

    /** Send a command (built with the allocated id) and await its matching `result` message. */
    override suspend fun request(buildCommand: (Long) -> String): String = coroutineScope {
        val id = nextId()
        // Subscribe before sending (UNDISPATCHED) so we can't miss a fast reply.
        val awaiter = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(20_000L.milliseconds) {
                results.first { it.id == id }.text
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
                send(Frame.Text(HaProtocolHelper.encodeAuth(config.token)))
                val authResp = (incoming.receive() as Frame.Text).readText()
                log.i { "auth response: ${HaProtocolHelper.messageType(authResp)}" }
                if (HaProtocolHelper.messageType(authResp) != "auth_ok") {
                    throw HaException("Authentication failed (${HaProtocolHelper.messageType(authResp)})")
                }

                // Expose the session BEFORE announcing Connected: consumers react to Connected by firing
                // requests (e.g. history fetches) through request()/session, so a still-null session here
                // would make those throw "Not connected" and silently fail.
                session = this
                _connection.value = HaConnectionStatus.Connected
                log.i { "connected; seeding states + subscribing" }

                // 2. Seed current states + subscribe to live changes.
                val statesId = nextId()
                send(Frame.Text(HaProtocolHelper.encodeGetStates(statesId)))
                send(Frame.Text(HaProtocolHelper.encodeSubscribeStateChanged(nextId())))

                // 4. Receive loop (ends when the server/network closes the channel).
                try {
                    while (true) {
                        val frame = incoming.receive()
                        if (frame is Frame.Text) handleMessage(frame.readText(), statesId)
                    }
                } catch (_: ClosedReceiveChannelException) {
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

    /**
     * Route one inbound frame. The frame is parsed **once** here and the root object handed to each
     * reader — [HaProtocolHelper]'s String entry points each parse afresh, which on the `get_states`
     * reply meant parsing a few hundred KB four times over.
     */
    private fun handleMessage(
        text: String,
        statesId: Long,
    ) {
        val root = HaProtocolHelper.parseRoot(text)
        if (root == null) {
            log.w { "dropping unparseable frame: ${text.summarize()}" }
            return
        }
        when (HaProtocolHelper.messageType(root)) {
            "result" -> {
                val id = HaProtocolHelper.resultId(root)
                results.tryEmit(ResultMessage(id, text)) // let any pending request() match by id
                if (id == statesId && HaProtocolHelper.isResultSuccess(root)) {
                    val list = HaProtocolHelper.parseStates(root)
                    if (list.isNotEmpty()) {
                        _states.value = list.associateBy { it.entityId }
                        _expStates.value = list.mapNotNull { state ->
                            state.toExpEntityState()?.let { state.entityId to it }
                        }.toMap()
                    } else {
                        log.w { "get_states returned no entities: ${text.summarize()}" }
                    }
                }
            }
            "event" -> {
                val change = HaProtocolHelper.parseStateChanged(root)
                if (change == null) {
                    log.w { "unrecognised event frame: ${text.summarize()}" }
                    return
                }
                _states.update { current ->
                    if (change.newState == null) current - change.entityId
                    else current + (change.entityId to change.newState)
                }
                _expStates.update { current ->
                    // A domain with no experimental mapping yet reads the same as a removal, so a
                    // stale entry can't outlive the change that dropped it.
                    val exp = change.newState?.toExpEntityState()
                    if (exp == null) current - change.entityId
                    else current + (change.entityId to exp)
                }
            }
        }
    }

    /**
     * Map a raw [EntityState] onto its experimental counterpart, or null while the domain has no
     * [ExpEntityState] type yet — the exp flow simply omits those entities.
     */
    private fun EntityState.toExpEntityState(): ExpEntityState? = when (domain) {
        "light" -> LightState(
            entityId = entityId,
            isOn = isOn,
            isOffline = isUnavailable,
            api = actionApi,
        )

        else -> null
    }
}

/** Frames run to hundreds of KB — never log one whole. */
private fun String.summarize(limit: Int = 200): String =
    if (length <= limit) this else "${take(limit)}… (${length} chars)"