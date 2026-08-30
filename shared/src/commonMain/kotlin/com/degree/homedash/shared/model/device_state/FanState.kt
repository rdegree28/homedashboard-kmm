package com.degree.homedash.shared.model.device_state

/**
 * Live state of a fan: whether it is running, how fast, and what its extras are doing.
 *
 * [isMisting] is read from the mister's own `humidifier.*` entity rather than the fan's — Home
 * Assistant models the two separately (see `FanMetadata.MistingControl`).
 */
data class FanState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
    val percentage: Int,
    val isOscillating: Boolean,
    val isMisting: Boolean,
) : ToggleableDeviceState, DeviceState
