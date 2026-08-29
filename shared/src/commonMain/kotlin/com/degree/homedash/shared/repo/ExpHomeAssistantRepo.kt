package com.degree.homedash.shared.repo

import co.touchlab.kermit.Logger
import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HaProtocolHelper
import com.degree.homedash.shared.api.PreviewExpHomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.device_metadata.DeviceMetadata
import com.degree.homedash.shared.model.device_metadata.FanMetadata
import com.degree.homedash.shared.model.device_metadata.HvacMode
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.device_metadata.ToggleableDeviceMetadata
import com.degree.homedash.shared.model.device_metadata.TriggerDeviceMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.collections.ifEmpty
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Public face of the Home Assistant stack: live entity states, the connection itself, history, and
 * the typed actions devices fire. Wired as a Koin `single` in `SharedModule`.
 *
 * [ExpHomeAssistantApi] is internal, so this forwards what callers need rather than handing out an
 * api. Device actions take metadata rather than ids — driving a device needs its identity, not its
 * live value — and are reached through the matching `DeviceMetadata` methods.
 */
class ExpHomeAssistantRepo internal constructor(
    private val api: ExpHomeAssistantApi,
) {

    /**
     * Live map of Home Assistant entity id → its latest state, for a roster to project against with
     * `DeviceMetadata.toUi`.
     *
     * Raw rather than typed on purpose: an entity's domain doesn't determine which device it is
     * (`sensor.*` backs climate, soil moisture and water level alike), so only the roster can decide.
     * Re-emits on every push for *any* entity, so callers projecting into UI state should
     * `distinctUntilChanged` downstream, as the ViewModels do.
     */
    fun loadEntityStates(): Flow<Map<String, EntityState>> = api.loadAllStates()

    /**
     * Live connection status. The app root provides it to every dashboard header, and the graph
     * screens re-fetch their history whenever it comes back up.
     */
    val connection: StateFlow<HaConnectionStatus> get() = api.connection

    /** Open (or restart) the connection with [config], reconnecting automatically until [disconnect]. */
    fun connect(config: HaConfig) = api.connect(config)

    /** Close the connection and stop reconnecting. */
    fun disconnect() = api.disconnect()


    /**
     * One entity's live state, re-emitting only when *that* entity changes rather than on every push.
     * Null while Home Assistant has never reported [entityId] — a disabled or mistyped id.
     */
    internal fun entityFor(entityId: String): Flow<EntityState?> =
        loadEntityStates().map { it[entityId] }.distinctUntilChanged()

    /** [entityFor] the entity backing [metadata]. */
    internal fun entityForDevice(metadata: DeviceMetadata): Flow<EntityState?> =
        entityFor(metadata.entityId)

    /**
     * [hoursBack] hours of numeric history for [entityId], for a device that charts one of its
     * sensors. Empty until the first fetch lands, and re-fetched on every reconnect: a history query
     * is a request/response round trip, so it can only run while connected, and the window it covers
     * moves on as the app stays open.
     *
     * A failed fetch reads as no data rather than taking the device's whole state flow down with it —
     * the chart says so itself, and the next reconnect tries again.
     */
    internal fun historyForEntity(
        entityId: String,
        hoursBack: Int,
    ): Flow<List<HistoryPoint>> =
        connection
            .filter { it == HaConnectionStatus.Connected }
            .map {
                try {
                    getHistoryForEntity(entityId, hoursBack)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.e(e) { "history fetch failed for $entityId" }
                    emptyList()
                }
            }
            .onStart { emit(emptyList()) }

    /**
     * Flips [entity] on or off.
     *
     * Takes metadata rather than an id because toggling needs the entity's identity, not its live
     * value — and the [ToggleableDeviceMetadata] parameter is what keeps non-toggleable entities out.
     * Reached through [ToggleableDeviceMetadata.onToggle] rather than called directly, so the only
     * caller today is `LightMetadata`.
     *
     * Fire-and-forget: HA answers a service call by pushing a new state, so there is nothing to await.
     */
    internal fun toggleEntity(entity: ToggleableDeviceMetadata) {
        api.toggleEntity(entityId = entity.entityId)
    }

    /**
     * Fires [trigger] — activating a scene, running a script, triggering an automation.
     *
     * Takes metadata rather than an id, the same way [toggleEntity] does: a trigger names the service
     * it fires and the entity it aims at, and nothing about the call can be read off live state.
     * Reached through [TriggerDeviceMetadata.onActivate] rather than called directly.
     */
    internal fun activateTrigger(trigger: TriggerDeviceMetadata) =
        api.callService(trigger.serviceDomain, trigger.service, trigger.targetEntityId)

    /** Turn [entityId] off, whatever its domain. */
    internal fun turnOff(entityId: String) =
        api.callService(entityId.substringBefore('.'), "turn_off", entityId)

    /** Run a `script.*` entity. */
    internal fun runScript(scriptEntityId: String) =
        api.callService("script", "turn_on", scriptEntityId)

    /**
     * Set a thermostat's setpoint. The value arrives back via the entity's `temperature` attribute.
     *
     * These five thermostat calls take metadata rather than an id for the same reason [toggleEntity]
     * does — driving a thermostat needs its identity, not its live value — and are reached through
     * the matching `ThermostatMetadata` methods rather than called directly.
     */
    internal fun setTargetTemperature(
        metadata: ThermostatMetadata,
        temperature: Double,
    ) = api.callService("climate", "set_temperature", metadata.entityId,
        buildJsonObject { put("temperature", temperature) })

    /** Set a thermostat's mode. The value arrives back as the entity's own state, not an attribute. */
    internal fun setHvacMode(
        metadata: ThermostatMetadata,
        mode: HvacMode,
    ) = api.callService("climate", "set_hvac_mode", metadata.entityId,
        buildJsonObject { put("hvac_mode", mode.haValue) })

    /** [fanMode] is one of the vendor strings [ThermostatMetadata.fanModes] declares, not a `fan.*` speed. */
    internal fun setThermostatFanMode(
        metadata: ThermostatMetadata,
        fanMode: String,
    ) = api.callService("climate", "set_fan_mode", metadata.entityId,
        buildJsonObject { put("fan_mode", fanMode) })

    /** [presetMode] is one of the vendor strings [ThermostatMetadata.presetModes] declares. */
    internal fun setPresetMode(
        metadata: ThermostatMetadata,
        presetMode: String,
    ) = api.callService("climate", "set_preset_mode", metadata.entityId,
        buildJsonObject { put("preset_mode", presetMode) })

    /**
     * Turn the extreme-setpoint mode on or off.
     *
     * Targets the `input_boolean.*` helper the metadata names, not the thermostat — the same
     * composite-entity arrangement as a fan's mister. No-ops for a thermostat that declares none.
     */
    internal fun setExtremeTemperatures(
        metadata: ThermostatMetadata,
        extreme: Boolean,
    ) {
        val helperId = metadata.extremeToggle?.entityId ?: return
        api.callService(helperId.substringBefore('.'), if (extreme) "turn_on" else "turn_off", helperId)
    }

    /** Set a fan's speed (0–100%). The value arrives back via the entity's `percentage` attribute. */
    internal fun setFanPercentage(
        metadata: FanMetadata,
        percentage: Int,
    ) = api.callService("fan", "set_percentage", metadata.entityId, buildJsonObject { put("percentage", percentage) })

    /**
     * Turn a fan's mister on or off.
     *
     * Targets the mister's own `humidifier.*` entity, not the fan's — Home Assistant models the two
     * separately (see [FanMetadata.MistingControl]). No-ops for a fan that has no mister.
     */
    internal fun setMisting(
        metadata: FanMetadata,
        misting: Boolean,
    ) {
        val misterId = metadata.misting?.entityId ?: return
        api.callService(misterId.substringBefore('.'), if (misting) "turn_on" else "turn_off", misterId)
    }

    /** Turn a fan's oscillation on or off. The value arrives back via the `oscillating` attribute. */
    internal fun setFanOscillating(
        metadata: FanMetadata,
        oscillating: Boolean,
    ) = api.callService("fan", "oscillate", metadata.entityId, buildJsonObject { put("oscillating", oscillating) })

    /**
     * Fetch [hoursBack] hours of numeric history for [entityId], ending now, picking the best source
     * for the window: raw recorder states for short ranges, long-term statistics beyond them.
     *
     * The recorder purges raw states after `purge_keep_days` (10 by default), so a raw query for a
     * month or a year silently returns only the last week or so — every chart looked capped at the
     * purge horizon. Statistics are rolled up hourly/daily and kept indefinitely, so longer windows
     * read from those instead and come back with a mean plus the min/max spread of each bucket.
     *
     * Falls back to raw states when an entity has no statistics at all (sensors without a
     * `state_class` never get them) — a short chart beats an empty one.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun getHistoryForEntity(
        entityId: String,
        hoursBack: Int,
    ): List<HistoryPoint> {
        val end = Clock.System.now()
        val start = end.minus(hoursBack.hours)
        val startIso = start.toString()
        val endIso = end.toString()

        if (hoursBack <= RAW_HISTORY_MAX_HOURS) return api.history(entityId, startIso, endIso)

        val period = if (hoursBack <= HOURLY_STATS_MAX_HOURS) HaProtocolHelper.StatisticsPeriod.HOUR else HaProtocolHelper.StatisticsPeriod.DAY
        return api.statistics(entityId, startIso, endIso, period)
            .ifEmpty { api.history(entityId, startIso, endIso) }
    }

    private val log = Logger.withTag("ExpHomeAssistantRepo")

    companion object {

        /**
         * Longest window still served from raw recorder states. Sits inside the recorder's default
         * 10-day retention so the short-range charts keep full per-sample detail, while anything
         * longer — where raw data would be partly purged anyway — comes from statistics.
         */
        private const val RAW_HISTORY_MAX_HOURS = 24 * 7

        /** Above this, hourly buckets would be too many points to plot usefully; switch to daily. */
        private const val HOURLY_STATS_MAX_HOURS = 24 * 90

        /**
         * An inert repo for `@Preview` use: no states ever arrive and actions do nothing.
         *
         * Previews render outside the app's Koin graph, so anything reaching for a repo — a
         * [ToggleableDeviceMetadata] card wanting somewhere to send its toggle — has nothing to
         * resolve. This exists because the real constructor is internal to `:shared`, so `:composeApp`
         * can't build a stand-in itself.
         */
        fun preview(): ExpHomeAssistantRepo = ExpHomeAssistantRepo(PreviewExpHomeAssistantApi)
    }
}