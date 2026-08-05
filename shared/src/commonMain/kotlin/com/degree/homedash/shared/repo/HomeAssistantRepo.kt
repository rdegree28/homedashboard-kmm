package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HaWebSocketClient
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * High-level entry point for the UI: live entity states + connection status, plus typed actions.
 * Talks only to [HomeAssistantApi]; Office-specific orchestration lives a layer above (the UI/state-holder).
 */
class HomeAssistantRepo internal constructor(
    private val api: HomeAssistantApi,
) {

    val states: StateFlow<Map<String, EntityState>> = api.states
    val connection: StateFlow<HaConnectionStatus> = api.connection

    fun entity(entityId: String): EntityState? = states.value[entityId]

    fun connect(config: HaConfig) = api.connect(config)
    fun disconnect() = api.disconnect()

    suspend fun toggle(entityId: String) =
        api.callService(entityId.substringBefore('.'), "toggle", entityId)

    suspend fun turnOn(entityId: String) =
        api.callService(entityId.substringBefore('.'), "turn_on", entityId)

    suspend fun turnOff(entityId: String) =
        api.callService(entityId.substringBefore('.'), "turn_off", entityId)

    /** Run a `script.*` entity. */
    suspend fun runScript(scriptEntityId: String) =
        api.callService("script", "turn_on", scriptEntityId)

    /** Set a fan's speed (0–100%). The value arrives back via the entity's `percentage` attribute. */
    suspend fun setFanPercentage(
        entityId: String,
        percentage: Int,
    ) = api.callService("fan", "set_percentage", entityId,
        buildJsonObject { put("percentage", percentage) })

    /** Turn a fan's oscillation on or off. The value arrives back via the `oscillating` attribute. */
    suspend fun setFanOscillating(
        entityId: String,
        oscillating: Boolean,
    ) = api.callService("fan", "oscillate", entityId,
        buildJsonObject { put("oscillating", oscillating) })

    suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    ) = api.callService(domain, service, entityId, serviceData)

    /** Fetch [hoursBack] hours of numeric history for [entityId], ending now. */
    @OptIn(ExperimentalTime::class)
    suspend fun powerHistory(
        entityId: String,
        hoursBack: Int,
    ): List<HistoryPoint> {
        val end = Clock.System.now()
        val start = end.minus(hoursBack.hours)
        return api.history(entityId, start.toString(), end.toString())
    }

    companion object {
        /** The production repository backed by a live Home Assistant WebSocket connection. */
        fun create(): HomeAssistantRepo = HomeAssistantRepo(WebSocketHomeAssistantApi(HaWebSocketClient()))
    }
}