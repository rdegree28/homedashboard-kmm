package com.degree.homedash.shared.data

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import com.degree.homedash.shared.network.HaProtocol
import com.degree.homedash.shared.network.HaWebSocketClient
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * High-level entry point for the UI: live entity states + connection status, plus typed actions.
 * Office-specific orchestration lives a layer above (the UI/state-holder).
 */
class HaRepository(private val client: HaWebSocketClient) {

    val states: StateFlow<Map<String, EntityState>> = client.states
    val connection: StateFlow<ConnectionStatus> = client.connection

    fun entity(entityId: String): EntityState? = states.value[entityId]

    fun connect(config: HaConfig) = client.start(config)
    fun disconnect() = client.stop()

    suspend fun toggle(entityId: String) =
        client.callService(entityId.substringBefore('.'), "toggle", entityId)

    suspend fun turnOn(entityId: String) =
        client.callService(entityId.substringBefore('.'), "turn_on", entityId)

    suspend fun turnOff(entityId: String) =
        client.callService(entityId.substringBefore('.'), "turn_off", entityId)

    /** Run a `script.*` entity. */
    suspend fun runScript(scriptEntityId: String) =
        client.callService("script", "turn_on", scriptEntityId)

    /** Set a fan's speed (0–100%). The value arrives back via the entity's `percentage` attribute. */
    suspend fun setFanPercentage(entityId: String, percentage: Int) =
        client.callService("fan", "set_percentage", entityId, buildJsonObject { put("percentage", percentage) })

    suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    ) = client.callService(domain, service, entityId, serviceData)

    /** Fetch [hoursBack] hours of numeric history for [entityId] over the WebSocket. */
    @OptIn(ExperimentalTime::class)
    suspend fun powerHistory(entityId: String, hoursBack: Int): List<HistoryPoint> {
        val end = Clock.System.now()
        val start = end.minus(hoursBack.hours)
        val text = client.request { id ->
            HaProtocol.encodeHistory(id, entityId, start.toString(), end.toString())
        }
        return HaProtocol.parseHistory(text, entityId)
    }
}
