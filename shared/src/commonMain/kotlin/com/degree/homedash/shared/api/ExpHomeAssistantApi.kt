package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * The experimental stack's Home Assistant surface: live typed states in, entity actions out.
 *
 * Internal to the module — repos are the public boundary, so callers outside `:shared` reach this
 * through [com.degree.homedash.shared.repo.HomeAssistantRepo] rather than holding an api.
 */
internal interface ExpHomeAssistantApi {

    /**
     * Live map of Home Assistant entity id → its latest [EntityState], updated as HA pushes changes.
     *
     * Deliberately raw: which typed device state an entity becomes depends on the roster, not on the
     * entity's domain — `sensor.*` alone backs climate, soil moisture and water level — so typing
     * happens in `StatefulDeviceMetadata.toState`, not here.
     */
    fun loadAllStates(): Flow<Map<String, EntityState>>

    /** Live connection status to the Home Assistant WebSocket API. */
    val connection: StateFlow<HaConnectionStatus>

    /** Open (or restart) the connection using [config], reconnecting automatically until [disconnect]. */
    fun connect(config: HaConfig)

    /** Close the connection and stop reconnecting. */
    fun disconnect()

    /**
     * Fetch numeric history for [entityId] between [startIso] and [endIso] (ISO-8601 timestamps),
     * returning samples in time order. Non-numeric states are skipped; an empty list means no data in
     * the range (or the request failed).
     *
     * Raw recorder states, so the window has to stay inside the recorder's retention — picking
     * between this and [statistics] is the repo's job, not the caller's.
     */
    suspend fun history(
        entityId: String,
        startIso: String,
        endIso: String,
    ): List<HistoryPoint>

    /**
     * Fetch [entityId]'s long-term statistics between [startIso] and [endIso], aggregated into
     * [period]-wide buckets carrying mean/min/max. Unlike [history] — which reads raw recorder states
     * and so only reaches back as far as the recorder's purge window — hourly and daily statistics are
     * retained indefinitely, so this is the only way to chart windows longer than that.
     *
     * Empty when the entity has no statistics (only sensors with a `state_class` get them) or the
     * request failed.
     */
    suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: HaProtocolHelper.StatisticsPeriod,
    ): List<HistoryPoint>

    /**
     * Calls the entity's toggle service on HA.
     *
     * Fire-and-forget: HA answers a service call by pushing the resulting state back over the socket,
     * so there is nothing to await. Implementations own the scope the call runs on, which keeps it
     * alive when the composable that triggered it leaves the screen.
     */
    fun toggleEntity(entityId: String)

    /**
     * Invoke the Home Assistant service `domain.service`, optionally targeting [entityId] and passing
     * [serviceData]. Fire-and-forget: any effect surfaces later via the [states] flow. Silently
     * dropped while disconnected.
     */
    fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    )
}
