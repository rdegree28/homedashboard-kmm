package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

/**
 * The experimental stack's Home Assistant surface: live typed states in, entity actions out.
 *
 * Internal to the module — repos are the public boundary, so callers outside `:shared` reach this
 * through [com.degree.homedash.shared.repo.ExpHomeAssistantRepo] rather than holding an api.
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

    /**
     * Numeric history for [entityId] over the last [hoursBack] hours — the samples behind a device's
     * chart.
     *
     * Emits an empty list straight away and the fetched samples once they land, re-fetching whenever
     * the socket (re)connects: a history query is a request/response round trip, so it can only run
     * while connected, and the window it covers moves on as the app stays open.
     */
    fun loadHistoryForEntity(entityId: String, hoursBack: Int): Flow<List<HistoryPoint>>

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
