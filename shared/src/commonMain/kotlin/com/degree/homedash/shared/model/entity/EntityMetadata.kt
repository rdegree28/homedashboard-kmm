package com.degree.homedash.shared.model.entity

/**
 * Static descriptor for an entity: its identity ([entityId]), the label to show for it
 * ([displayName]), plus capabilities that rarely change (a fan's speed steps, a climate sensor's
 * kind) as opposed to live values. One implementation per entity type ([LightMetadata],
 * [FanMetadata], …).
 */
sealed interface EntityMetadata {
    val entityId: String

    /** Label shown on the control — the dashboard's name for it, not Home Assistant's friendly name. */
    val displayName: String
}
