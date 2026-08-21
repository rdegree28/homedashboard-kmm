package com.degree.homedash.controls

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.model.states.ExpEntityState
import com.degree.homedash.shared.model.states.LightState

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
fun EntityMetadata.toExpEntityUi(
    state: ExpEntityState,
): ExpEntityUi = when (this) {
    is LightMetadata -> {
        state as LightState
        LightEntityUi(
            id = this.entityId,
            name = this.displayName,
            state = state,
        )
    }

    else -> throw IllegalStateException("No Experimental UI mapper for metadata type")
//
//    is FanMetadata -> EntityUi.Fan(
//        metadata = this,
//        isOn = state?.isOn == true,
//        offline = state.isOffline(),
//        percentage = state?.attrDouble("percentage")?.roundToInt() ?: 0,
//        oscillating = state?.attrBoolean("oscillating") == true,
//        // Read off the mister's own entity, not the fan's.
//        misting = misting?.let { allStates[it.entityId]?.isOn == true } == true,
//    )
//
//    is ClimateMetadata -> EntityUi.Climate(
//        metadata = this,
//        valueText = state.readingText(decimals = 1),
//    )
//
//    is ThermostatMetadata -> {
//        val offline = state.isOffline()
//        EntityUi.Thermostat(
//            metadata = this,
//            offline = offline,
//            // An unavailable entity's state is literally "unavailable", so this already lands on null.
//            hvacMode = HvacMode.fromHa(state?.state),
//            // The attributes are guarded anyway: some integrations keep stale ones around while
//            // the device is gone, which would show a live-looking readout for an offline unit.
//            hvacAction = if (offline) null else HvacAction.fromHa(state?.attrString("hvac_action")),
//            // Null in heat_cool, where HA publishes target_temp_low/high instead — the stepper reads
//            // "—" and disables rather than pretending to drive a setpoint that isn't there.
//            targetTemperature = if (offline) null else state?.attrDouble("temperature"),
//            currentTemperature = if (offline) null else state?.attrDouble("current_temperature"),
//            currentHumidity = if (offline) null else state?.attrDouble("current_humidity"),
//            fanMode = if (offline) null else state?.attrString("fan_mode"),
//            presetMode = if (offline) null else state?.attrString("preset_mode"),
//            // Its own input_boolean, so it survives the thermostat being unavailable — the preset
//            // pills still need to show which setpoints they would write.
//            extremeActive = extremeToggle?.let { allStates[it.entityId]?.isOn == true } == true,
//        )
//    }
//
//    is DoorMetadata -> {
//        val open = state?.state == "on" // device_class opening: on = open
//        EntityUi.Door(
//            metadata = this,
//            statusText = when {
//                state.isOffline() -> "—"
//                open -> "Open"
//                else -> "Closed"
//            },
//            open = open,
//            unavailable = state.isOffline(),
//        )
//    }
//
//    is SoilMoistureMetadata -> {
//        val pct = state.percentOrNull()
//        EntityUi.SoilMoisture(
//            metadata = this,
//            pct = pct,
//            valueText = pct?.let { "${formatNumber(it, decimals = 1)} %" } ?: "—",
//        )
//    }
//
//    is WaterLevelMetadata -> {
//        val pct = state.percentOrNull()
//        EntityUi.WaterLevel(
//            metadata = this,
//            pct = pct,
//            valueText = pct?.let { "${formatNumber(it, decimals = 0)} %" } ?: "—",
//            // Read off the filter's own sensor, not the water level's — the same second-entity
//            // arrangement as a fan's mister.
//            filterDaysRemaining = filterHealth?.let { allStates[it.entityId].daysOrNull() },
//        )
//    }
//
//    // Deliberately ignores [state]: a launcher card has no Home Assistant entity behind it.
//    is NavigationMetadata -> EntityUi.Navigation(this)
//
//    // Likewise stateless — a trigger fires a service, it doesn't report anything.
//    is TriggerEntityMetadata -> EntityUi.Trigger(this)
}

/** Projects a whole screen's roster against the current [states] map. */
fun Map<EntityMetadata, ExpEntityState>.toEntityUis(): List<ExpEntityUi> =
    mapNotNull {
        try {
            it.key.toExpEntityUi(it.value)
        } catch (e: Exception) {
            null
        }
    }