package com.degree.homedash.shared.model.device_state

/**
 * State of a device that can be flipped on and off. Render data only — the toggle itself hangs off
 * [com.degree.homedash.shared.model.device_metadata.ToggleableDeviceMetadata], since flipping a device needs
 * its identity rather than its current value.
 */
interface ToggleableDeviceState {

    /** Whether the device is currently "on". */
    val isOn: Boolean
}
