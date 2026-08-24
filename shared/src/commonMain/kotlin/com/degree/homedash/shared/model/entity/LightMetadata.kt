package com.degree.homedash.shared.model.entity

/**
 * A light.
 */
data class LightMetadata(
    override val entityId: String,
    override val displayName: String,
) : ToggleableDeviceMetadata, DeviceMetadata
