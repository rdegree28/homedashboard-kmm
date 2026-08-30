package com.degree.homedash.shared.model.device_state

/** Live state of a light: on or off, and whether Home Assistant is reporting it at all. */
data class LightState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
) : ToggleableDeviceState, DeviceState
