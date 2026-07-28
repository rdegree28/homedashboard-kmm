package com.degree.homedash.shared.model.entity

/**
 * Static descriptor for an entity: its identity ([entityId]) plus capabilities that rarely change
 * (a light's rgb support, a fan's level count, a climate sensor's kind), as opposed to live values.
 * One implementation per entity type ([LightMetadata], [FanMetadata], …).
 */
sealed interface EntityMetadata {
    val entityId: String
}
