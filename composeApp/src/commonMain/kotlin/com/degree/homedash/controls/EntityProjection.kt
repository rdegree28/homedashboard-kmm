package com.degree.homedash.controls

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.device_metadata.ClimateMetadata
import com.degree.homedash.shared.model.device_metadata.DeviceMetadata
import com.degree.homedash.shared.model.device_metadata.DoorMetadata
import com.degree.homedash.shared.model.device_metadata.FanMetadata
import com.degree.homedash.shared.model.device_metadata.LightMetadata
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeSignalMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.device_metadata.PetFountainMetadata
import com.degree.homedash.shared.model.device_metadata.SoilMoistureMetadata
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.device_metadata.TriggerDeviceMetadata
import com.degree.homedash.ui.readingText
import kotlin.math.roundToInt

/**
 * Projects an entity's static descriptor plus its live state into the render state for its control.
 * [state] is null when Home Assistant hasn't reported the entity, which reads the same as unavailable.
 *
 * This is the one place raw [EntityState] becomes an [EntityUi]; screens supply metadata (usually from
 * `EntityMetadataRepo`) and never touch attributes themselves.
 *
 * [allStates] is only needed by entities that are composites of more than one Home Assistant entity —
 * a fan's mister is its own `humidifier.*` entity, so its on/off can't come from [state]. Defaults to
 * empty for the single-entity callers.
 */
fun DeviceMetadata.toEntityUi(
    state: EntityState?,
    allStates: Map<String, EntityState> = emptyMap(),
): EntityUi? = when (this) {
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
        oscillating = state?.attrBoolean("oscillating") == true,
        // Read off the mister's own entity, not the fan's.
        misting = misting?.let { allStates[it.entityId]?.isOn == true } == true,
    )

    is ClimateMetadata -> EntityUi.Climate(
        metadata = this,
        valueText = state.readingText(decimals = 1),
    )

    // Fully migrated to the device stack — rendered by DeviceControl, with no [EntityUi] form at all.
    is ThermostatMetadata -> null

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

    // Fully migrated to the device stack — rendered by SoilMoistureControl, with no [EntityUi] form.
    is SoilMoistureMetadata -> null

    // Fully migrated to the device stack — rendered by WaterLevelControl, with no [EntityUi] form.
    is PetFountainMetadata -> null

    // Deliberately ignores [state]: a launcher card has no Home Assistant entity behind it.
    is NavigationMetadata -> EntityUi.Navigation(this)

    // Fully migrated to the device stack — rendered by DeviceControl, with no [EntityUi] form at all.
    is TriggerDeviceMetadata -> null

    // Fully migrated to the device stack — rendered by DeviceControl, with no [EntityUi] form at all.
    is OfficeSignalMetadata -> null

    // Fully migrated to the device stack — rendered by DeviceControl, with no [EntityUi] form at all.
    is OfficeWorkstationMetadata -> null
}

/** Projects a whole screen's roster against the current [states] map. */
fun List<DeviceMetadata>.toEntityUis(states: Map<String, EntityState>): List<EntityUi> =
    mapNotNull { it.toEntityUi(states[it.entityId], states) }

/** Missing and unavailable are the same thing to a control: nothing to show. */
private fun EntityState?.isOffline(): Boolean = this == null || this.isUnavailable
