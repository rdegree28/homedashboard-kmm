package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.api.HomeAssistantActionApi

/**
 * Entity state interface that holds for toggleable cards.
 */
interface ToggleableEntityState : ExpEntityState {

    /**
     * Whether the entity is currently "on" or not. It is expected that onToggle() will revert this
     * state.
     */
    val isOn: Boolean

    /**
     * Action that is called when the device is toggled. Returns an optimistic state of the result
     * of the toggle.
     */
    suspend fun onToggle(): Boolean
}