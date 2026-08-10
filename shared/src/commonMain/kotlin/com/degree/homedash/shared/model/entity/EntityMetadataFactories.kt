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
        temperaturePresets = LIVING_ROOM_PRESETS,
        extremeToggle = ThermostatMetadata.ExtremeToggle("input_boolean.extreme_temperatures"),
    )
}

/**
 * The Living Room presets, in pill order. `heat` applies while the unit is heating, `cool` while it
 * is cooling; the `extreme` pair replaces them while `input_boolean.extreme_temperatures` is on.
 *
 * Straight from the band matrix:
 * ```
 * Band     Mode   Economy  Extreme  Normal
 * Comfort  Cool     78       75       72
 * Comfort  Heat     60       65       67
 * Sleep    Cool     74       71       69
 * Sleep    Heat     60       61       62
 * ```
 * Comfort and Sleep take the Normal and Extreme columns. Economy is a band in its own right rather
 * than a third column, so it holds the Economy figures and — for now — ignores the extreme toggle by
 * declaring the same pair twice. That leaves an obvious place to give it its own extreme values
 * later without reshaping anything.
 */
private val LIVING_ROOM_PRESETS: List<TemperaturePreset> = listOf(
    TemperaturePreset(
        kind = PresetKind.Comfort,
        normal = SetpointPair(heat = 67.0, cool = 72.0),
        extreme = SetpointPair(heat = 65.0, cool = 75.0),
    ),
    TemperaturePreset(
        kind = PresetKind.Sleep,
        normal = SetpointPair(heat = 62.0, cool = 69.0),
        extreme = SetpointPair(heat = 61.0, cool = 71.0),
    ),
    TemperaturePreset(
        kind = PresetKind.Economy,
        // The matrix gives Economy twice — 78°/60° during the Comfort band, 74°/60° during Sleep.
        // A single Economy pill carries one, and it's the daytime pair: the deeper of the two, and
        // the one confirmed for cooling. Heating is 60° in either band regardless.
        normal = SetpointPair(heat = 60.0, cool = 78.0),
        extreme = SetpointPair(heat = 60.0, cool = 78.0),
    ),
)