package com.degree.homedash.shared.model.states

// State instantiation for a light.
data class LightState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
) : ToggleableEntityState
