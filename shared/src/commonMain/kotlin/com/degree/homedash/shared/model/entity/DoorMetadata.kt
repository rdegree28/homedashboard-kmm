package com.degree.homedash.shared.model.entity

/**
 * A door contact sensor (HA `binary_sensor` with device class `opening`). Read-only, so identity is
 * all it carries — open/closed comes from the live state.
 */
data class DoorMetadata(
    override val entityId: String,
    override val displayName: String,
) : EntityMetadata
