package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.states.ExpEntityState
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
     * Live device states, re-derived as Home Assistant pushes changes.
     *
     * Carries the raw snapshot alongside the typed one so a single push stays a single emission —
     * [ExpEntityState.withCompanions] needs raw entities to resolve companions, and a second flow for
     * them would double every update.
     */
    fun loadAllStates(): Flow<ExpStateSnapshot>

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

/** Typed device states plus the raw Home Assistant snapshot they were derived from. */
internal data class ExpStateSnapshot(
    val devices: Map<String, ExpEntityState>,
    val entities: Map<String, EntityState>,
)
