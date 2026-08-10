package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HaWebSocketClient
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.api.WebSocketHomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.entity.HvacMode
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

    /**
     * Turn a fan's mister on or off.
     *
     * [entityId] is the mister's own `humidifier.*` entity, not the fan's — Home Assistant models the
     * two separately (see `FanMetadata.MistingControl`).
     */
    suspend fun setMisting(
        entityId: String,
        misting: Boolean,
    ) = if (misting) turnOn(entityId) else turnOff(entityId)

    /** Turn a fan's oscillation on or off. The value arrives back via the `oscillating` attribute. */
    suspend fun setFanOscillating(
        entityId: String,
        oscillating: Boolean,
    ) = api.callService("fan", "oscillate", entityId,
        buildJsonObject { put("oscillating", oscillating) })

    /** Set a thermostat's setpoint. The value arrives back via the entity's `temperature` attribute. */
    suspend fun setTargetTemperature(
        entityId: String,
        temperature: Double,
    ) = api.callService("climate", "set_temperature", entityId,
        buildJsonObject { put("temperature", temperature) })

    /** Set a thermostat's mode. The value arrives back as the entity's own state, not an attribute. */
    suspend fun setHvacMode(
        entityId: String,
        mode: HvacMode,
    ) = api.callService("climate", "set_hvac_mode", entityId,
        buildJsonObject { put("hvac_mode", mode.haValue) })

    /**
     * Set a thermostat's fan mode. [fanMode] is one of the vendor strings the entity reports in
     * `fan_modes`, which `ThermostatMetadata` declares.
     *
     * Named for thermostats rather than plain `setFanMode` because [setFanPercentage] and
     * [setFanOscillating] next door act on `fan.*` entities, which these do not.
     */
    suspend fun setThermostatFanMode(
        entityId: String,
        fanMode: String,
    ) = api.callService("climate", "set_fan_mode", entityId,
        buildJsonObject { put("fan_mode", fanMode) })

    /**
     * Turn the extreme-setpoint mode on or off.
     *
     * [entityId] is the `input_boolean.*` helper the thermostat's metadata names, not the thermostat
     * — the same composite-entity shape as [setMisting].
     */
    suspend fun setExtremeTemperatures(
        entityId: String,
        extreme: Boolean,
    ) = if (extreme) turnOn(entityId) else turnOff(entityId)

    /** Set a thermostat's preset. [presetMode] is one of the entity's reported `preset_modes`. */
    suspend fun setPresetMode(
        entityId: String,
        presetMode: String,
    ) = api.callService("climate", "set_preset_mode", entityId,
        buildJsonObject { put("preset_mode", presetMode) })

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