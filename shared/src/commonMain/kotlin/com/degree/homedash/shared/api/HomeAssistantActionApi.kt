package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ToggleableEntityState

/**
 * Interface for acting on home assistant actions.
 */
interface HomeAssistantActionApi {

    /**
     * Attempts to call toggle the entity toggle service on HA.
     */
    suspend fun toggleEntity(entityState: ToggleableEntityState)

}