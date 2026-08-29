package com.degree.homedash.shared.model.device_metadata

/**
 * Static descriptor for a device: its identity ([entityId]), the label to show for it
 * ([displayName]), plus capabilities that rarely change (a fan's speed steps, a climate sensor's
 * kind) as opposed to the live values in its `DeviceState`. One implementation per device type
 * ([LightMetadata], [FanMetadata], …).
 */
sealed interface DeviceMetadata {
    /** Id of the Home Assistant entity that backs this device. */
    val entityId: String

    /** Label shown on the control — the dashboard's name for it, not Home Assistant's friendly name. */
    val displayName: String
}
