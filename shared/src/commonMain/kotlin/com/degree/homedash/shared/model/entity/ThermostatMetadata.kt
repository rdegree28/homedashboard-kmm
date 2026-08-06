package com.degree.homedash.shared.model.entity

/**
 * A thermostat (`climate.*`).
 *
 * Every control is an optional capability, the way [FanMetadata] declares speed/oscillation/misting:
 * a thermostat that only has a setpoint declares [targetTemperature] and leaves the mode lists empty,
 * and the card renders only the stepper.
 */
data class ThermostatMetadata(
    override val entityId: String,
    override val displayName: String,
    /** Non-null when the setpoint is adjustable — the card shows its −/+ stepper exactly then. */
    val targetTemperature: TargetTemperature? = null,
    /** The modes this thermostat offers, in the order the pills render. Empty = no mode selector. */
    val hvacModes: List<HvacMode> = emptyList(),
    /** Vendor strings ("on"/"off" here, "auto"/"low"/"high" on the office heater). Empty = no selector. */
    val fanModes: List<String> = emptyList(),
    /** Vendor strings ("none"/"eco"). Empty = no selector. */
    val presetModes: List<String> = emptyList(),
    /** Suffix on both readouts. Not an attribute — `climate.*` entities publish no unit. */
    val unitLabel: String = "°",
) : EntityMetadata {

    /**
     * The setpoint's bounds and increment.
     *
     * Hand-declared rather than read from `min_temp`/`max_temp`/`target_temp_step`, the same way
     * [FanMetadata.SpeedAdjustment] hand-declares its level count: the Living Room thermostat
     * publishes no step at all, and *every* attribute disappears when a device drops offline, which
     * would otherwise collapse the stepper's range to nothing.
     */
    data class TargetTemperature(
        val min: Double,
        val max: Double,
        val step: Double,
    )

    // Defined for factories
    companion object
}

/**
 * Home Assistant's `climate` state values — a thermostat's `state` *is* its hvac mode.
 *
 * A closed set fixed by the `climate` integration, unlike fan/preset modes, which are vendor strings
 * and stay untyped.
 */
enum class HvacMode(val haValue: String) {
    Off("off"),
    Heat("heat"),
    Cool("cool"),
    HeatCool("heat_cool"),
    Auto("auto"),
    Dry("dry"),
    FanOnly("fan_only"),
    ;

    companion object {
        /** null for anything unrecognised, so a firmware change degrades to "—" rather than crashing. */
        fun fromHa(raw: String?): HvacMode? = entries.firstOrNull { it.haValue == raw }
    }
}

/** The `hvac_action` attribute: what the unit is doing right now, as opposed to what mode it's in. */
enum class HvacAction(val haValue: String) {
    Off("off"),
    Idle("idle"),
    Heating("heating"),
    Cooling("cooling"),
    Drying("drying"),
    Fan("fan"),
    Preheating("preheating"),
    Defrosting("defrosting"),
    ;

    companion object {
        /** null for anything unrecognised — and for the many devices that never report an action. */
        fun fromHa(raw: String?): HvacAction? = entries.firstOrNull { it.haValue == raw }
    }
}
