package com.degree.homedash.shared.model.entity

/**
 * A water-level sensor (the pet fountain), rendered as a percentage gauge. Read-only, so identity is
 * all it carries — the level comes from the live state.
 */
data class WaterLevelMetadata(
    override val entityId: String,
) : EntityMetadata
