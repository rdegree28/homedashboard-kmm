package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.api.HomeAssistantActionApi

// State instantiation for a light.
data class LightState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,

    private val api: HomeAssistantActionApi,
) : ToggleableEntityState {

    override suspend fun onToggle(): Boolean {
        api.toggleEntity(this)
        return !isOn
    }
}