package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.*
import androidx.compose.runtime.Immutable

/**
 * Live render state for an entity, one variant per control type. Each variant nests its *typed*
 * [EntityMetadata] (so a light's state can't be paired with fan metadata) plus the values that change
 * with each state push — the label lives on the metadata, not here. Built by [toEntityUi] and
 * rendered by [EntityControl]; screens hand lists of these to `ControlGroup`.
 */
@Immutable
sealed interface EntityUi {
    val metadata: EntityMetadata

    @Immutable
    data class Light(
        override val metadata: LightMetadata,
        val isOn: Boolean,
        val offline: Boolean,
    ) : EntityUi

    @Immutable
    data class Fan(
        override val metadata: FanMetadata,
        val isOn: Boolean,
        val offline: Boolean,
        val percentage: Int,
    ) : EntityUi

    @Immutable
    data class Climate(
        override val metadata: ClimateMetadata,
        val valueText: String,
        val subvalueText: String? = null,
    ) : EntityUi

    @Immutable
    data class Door(
        override val metadata: DoorMetadata,
        val statusText: String,
        val open: Boolean,
        val unavailable: Boolean,
    ) : EntityUi

    @Immutable
    data class SoilMoisture(
        override val metadata: SoilMoistureMetadata,
        val pct: Double?,
        val valueText: String,
    ) : EntityUi

    @Immutable
    data class WaterLevel(
        override val metadata: WaterLevelMetadata,
        val pct: Double?,
        val valueText: String,
    ) : EntityUi
}

/** Convenience for list keys and actions — the underlying entity id. */
val EntityUi.entityId: String get() = metadata.entityId

/** Convenience for rendering — the label this entity shows. */
val EntityUi.displayName: String get() = metadata.displayName
