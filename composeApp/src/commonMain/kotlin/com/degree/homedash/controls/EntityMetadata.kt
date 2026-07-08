package com.degree.homedash.controls

import androidx.compose.runtime.Immutable

/**
 * Static descriptor for an entity: its identity and capabilities that rarely change (as opposed to
 * live values, which live in [EntityUi]). Capabilities that gate UI — a light's [Light.rgb], a fan's
 * [Fan.levelCount], a sensor's [Climate.kind] — belong here so state classes stay lean and
 * metadata-only sub-composables can skip recomposition when live values change.
 */
@Immutable
sealed interface EntityMetadata {
    val entityId: String

    @Immutable
    data class Light(override val entityId: String, val rgb: Boolean = false) : EntityMetadata

    @Immutable
    data class Fan(override val entityId: String, val levelCount: Int) : EntityMetadata

    @Immutable
    data class Climate(override val entityId: String, val kind: ClimateKind) : EntityMetadata

    @Immutable
    data class Door(override val entityId: String) : EntityMetadata

    @Immutable
    data class SoilMoisture(override val entityId: String) : EntityMetadata

    @Immutable
    data class WaterLevel(override val entityId: String) : EntityMetadata
}

/** Which climate sensor a [EntityMetadata.Climate] is — selects the row's icon + tint. */
enum class ClimateKind { Temperature, Humidity }
