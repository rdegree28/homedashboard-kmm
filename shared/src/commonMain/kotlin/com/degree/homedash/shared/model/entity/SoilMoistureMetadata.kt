package com.degree.homedash.shared.model.entity

/**
 * A plant soil-moisture sensor, rendered as a percentage gauge. Read-only, so identity is all it
 * carries — the level comes from the live state.
 */
data class SoilMoistureMetadata(
    override val entityId: String,
) : EntityMetadata
