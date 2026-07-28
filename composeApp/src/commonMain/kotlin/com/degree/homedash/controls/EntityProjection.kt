package com.degree.homedash.controls

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.entity.DoorMetadata
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.model.entity.SoilMoistureMetadata
import com.degree.homedash.shared.model.entity.WaterLevelMetadata
import com.degree.homedash.ui.formatNumber
import com.degree.homedash.ui.formatNumberOrSelf
import kotlin.math.roundToInt

/**
 * Projects an entity's static descriptor plus its live state into the render state for its control.
 * [state] is null when Home Assistant hasn't reported the entity, which reads the same as unavailable.
 *
 * This is the one place raw [EntityState] becomes an [EntityUi]; screens supply metadata (usually from
 * `EntityMetadataRepo`) and never touch attributes themselves.
 */
fun EntityMetadata.toEntityUi(state: EntityState?): EntityUi = when (this) {
    is LightMetadata -> EntityUi.Light(
        metadata = this,
        isOn = state?.isOn == true,
        offline = state.isOffline(),
    )

    is FanMetadata -> EntityUi.Fan(
        metadata = this,
        isOn = state?.isOn == true,
        offline = state.isOffline(),
        percentage = state?.attrDouble("percentage")?.roundToInt() ?: 0,
    )

    is ClimateMetadata -> EntityUi.Climate(
        metadata = this,
        valueText = state.readingText(decimals = 1),
    )

    is DoorMetadata -> {
        val open = state?.state == "on" // device_class opening: on = open
        EntityUi.Door(
            metadata = this,
            statusText = when {
                state.isOffline() -> "—"
                open -> "Open"
                else -> "Closed"
            },
            open = open,
            unavailable = state.isOffline(),
        )
    }

    is SoilMoistureMetadata -> {
        val pct = state.percentOrNull()
        EntityUi.SoilMoisture(
            metadata = this,
            pct = pct,
            valueText = pct?.let { "${formatNumber(it, decimals = 1)} %" } ?: "—",
        )
    }

    is WaterLevelMetadata -> {
        val pct = state.percentOrNull()
        EntityUi.WaterLevel(
            metadata = this,
            pct = pct,
            valueText = pct?.let { "${formatNumber(it, decimals = 0)} %" } ?: "—",
        )
    }
}

/** Projects a whole screen's roster against the current [states] map. */
fun List<EntityMetadata>.toEntityUis(states: Map<String, EntityState>): List<EntityUi> =
    map { it.toEntityUi(states[it.entityId]) }

/** Missing and unavailable are the same thing to a control: nothing to show. */
private fun EntityState?.isOffline(): Boolean = this == null || this.isUnavailable

/** The numeric state, or null when it isn't a usable reading. */
private fun EntityState?.percentOrNull(): Double? =
    this?.state?.toDoubleOrNull()?.takeUnless { isUnavailable }

/** Formatted reading with its unit — always "—" when there's nothing to report. */
private fun EntityState?.readingText(decimals: Int): String {
    if (this == null || isUnavailable) return "—"
    val unit = attrString("unit_of_measurement").orEmpty()
    return "${formatNumberOrSelf(state, decimals)} $unit".trim()
}
