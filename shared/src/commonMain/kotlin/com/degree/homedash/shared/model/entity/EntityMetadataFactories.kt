package com.degree.homedash.shared.model.entity

/*

 File contains some factories for commonly created entities.

 */

fun FanMetadata.Companion.dellaTowerFan(
    entityId: String,
    displayName: String,
): FanMetadata {
    return FanMetadata(
        entityId = entityId,
        displayName = displayName,
        speedAdjustment = FanMetadata.SpeedAdjustment(12),
        hasOscillationFeature = true,
    )
}

/**
 * The Living Room thermostat. Bounds, modes and presets confirmed against `get_states` 2026-08-06;
 * the unit publishes no `target_temp_step`, so the 1° increment is declared here.
 *
 * `heat_cool` is deliberately left out of [ThermostatMetadata.hvacModes] even though the hardware
 * offers it: it supports TARGET_TEMPERATURE_RANGE, so in that mode Home Assistant stops publishing
 * `temperature` and publishes `target_temp_low`/`target_temp_high` instead, which a single setpoint
 * stepper can't drive. The card still degrades gracefully if the mode is set from the HA app.
 *
 * The unit's `fan_modes` (`on`/`off`) are left out too. On a whole-house system that only forces the
 * blower to run continuously — rarely wanted, and not worth a permanent row on the card. Declaring
 * [ThermostatMetadata.fanModes] brings the row back with no other change.
 */
fun ThermostatMetadata.Companion.livingRoomThermostat(
    entityId: String,
    displayName: String,
): ThermostatMetadata {
    return ThermostatMetadata(
        entityId = entityId,
        displayName = displayName,
        targetTemperature = ThermostatMetadata.TargetTemperature(min = 50.0, max = 90.0, step = 1.0),
        hvacModes = listOf(HvacMode.Off, HvacMode.Heat, HvacMode.Cool),
        presetModes = listOf("none", "eco"),
    )
}