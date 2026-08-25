package com.degree.homedash.shared.model.states

data class FanState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
    val percentage: Int,
    val isOscillating: Boolean,
    val isMisting: Boolean,
) : ToggleableDeviceState, DeviceState
