package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow

/**
 * The experimental stack's Home Assistant surface: live typed states in, entity actions out.
 *
 * Internal to the module — repos are the public boundary, so callers outside `:shared` reach this
 * through [com.degree.homedash.shared.repo.ExpHomeAssistantRepo] rather than holding an api.
 */
internal interface ExpHomeAssistantApi {

    /** Live map of entity id → typed [ExpEntityState], re-derived as Home Assistant pushes changes. */
    fun loadAllStates(): Flow<Map<String, ExpEntityState>>

    /**
     * Calls the entity's toggle service on HA.
     *
     * Fire-and-forget: HA answers a service call by pushing the resulting state back over the socket,
     * so there is nothing to await. Implementations own the scope the call runs on, which keeps it
     * alive when the composable that triggered it leaves the screen.
     */
    fun toggleEntity(entityId: String)
}
