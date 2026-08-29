package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.ThermostatState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

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
    /**
     * The dashboard's own temperature presets, in the order their pills render. Empty = no row.
     *
     * Not Home Assistant's `preset_mode` (that's [presetModes]) — these are ours: each one just
     * writes a setpoint, so a preset is "active" purely because the thermostat happens to sit on its
     * temperature. Nothing to store, and it stays correct when the setpoint is changed from the HA
     * app or the physical unit.
     */
    val temperaturePresets: List<TemperaturePreset> = emptyList(),
    /** Non-null when an `input_boolean` gates the extreme setpoints. */
    val extremeToggle: ExtremeToggle? = null,
    /** Suffix on both readouts. Not an attribute — `climate.*` entities publish no unit. */
    val unitLabel: String = "°",
    /**
     * Which scale the readings are in. Declared for the same reason [unitLabel] is — `climate.*`
     * entities publish no unit — and needed by the card's dew point, which is computed in Celsius
     * and converted back, so a bare "°" isn't enough to go on.
     */
    val fahrenheit: Boolean = true,
) : StatefulDeviceMetadata<ThermostatState> {

    /**
     * Assembled from two entities: the extreme-setpoint mode lives in its own `input_boolean.*` (see
     * [ExtremeToggle]), so its flow is combined in rather than read off [entityId]. A thermostat
     * without one contributes a constant null instead.
     *
     * The attributes are guarded on [isOffline] rather than read blind: some integrations keep stale
     * attributes around while the device is gone, which would show a live-looking readout for a unit
     * that isn't there. [extremeActive] is deliberately outside that guard — it belongs to the helper,
     * not the thermostat, and the preset pills still need to show which setpoints they would write.
     */
    override fun loadState(repo: ExpHomeAssistantRepo): Flow<ThermostatState> =
        combine(
            repo.entityForDevice(this),
            extremeToggle?.let { repo.entityFor(it.entityId) } ?: flowOf(null),
        ) { entity, extreme ->
            val offline = entity == null || entity.isUnavailable
            ThermostatState(
                entityId = entityId,
                isOffline = offline,
                // An unavailable entity's state is literally "unavailable", so this already lands on null.
                hvacMode = HvacMode.fromHa(entity?.state),
                hvacAction = if (offline) null else HvacAction.fromHa(entity?.attrString("hvac_action")),
                // Null in heat_cool, where HA publishes target_temp_low/high instead — the stepper reads
                // "—" and disables rather than pretending to drive a setpoint that isn't there.
                targetTemperature = if (offline) null else entity?.attrDouble("temperature"),
                currentTemperature = if (offline) null else entity?.attrDouble("current_temperature"),
                currentHumidity = if (offline) null else entity?.attrDouble("current_humidity"),
                fanMode = if (offline) null else entity?.attrString("fan_mode"),
                presetMode = if (offline) null else entity?.attrString("preset_mode"),
                extremeActive = extreme?.isOn == true,
            )
        }

    /** Writes a new setpoint. Fire-and-forget, like every other device action. */
    fun setTargetTemperature(temperature: Double, repo: ExpHomeAssistantRepo) =
        repo.setTargetTemperature(metadata = this, temperature = temperature)

    fun setHvacMode(mode: HvacMode, repo: ExpHomeAssistantRepo) =
        repo.setHvacMode(metadata = this, mode = mode)

    /** [mode] is one of the vendor strings [fanModes] declares, not a `fan.*` entity speed. */
    fun setFanMode(mode: String, repo: ExpHomeAssistantRepo) =
        repo.setThermostatFanMode(metadata = this, fanMode = mode)

    /** [mode] is one of the vendor strings [presetModes] declares — HA's presets, not ours. */
    fun setPresetMode(mode: String, repo: ExpHomeAssistantRepo) =
        repo.setPresetMode(metadata = this, presetMode = mode)

    /** Flips the [extremeToggle] helper; a no-op on a thermostat that declares none. */
    fun setExtremeTemperatures(extreme: Boolean, repo: ExpHomeAssistantRepo) =
        repo.setExtremeTemperatures(metadata = this, extreme = extreme)

    /**
     * The Home Assistant helper holding whether extreme setpoints are in force.
     *
     * Its own `input_boolean.*` entity rather than app state, so the wall tablet, phone and web all
     * agree and an automation can flip it. The composite-entity shape mirrors
     * `FanMetadata.MistingControl`.
     */
    data class ExtremeToggle(val entityId: String)

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
 * One of the dashboard's temperature presets: tapping its pill writes a setpoint, and the pill fills
 * whenever the thermostat already sits on that setpoint.
 *
 * Four numbers rather than one because a setpoint means opposite things in the two hvac modes — 66°
 * is frugal when heating and extravagant when cooling — and [extreme] declares its own pair outright
 * rather than deriving from an offset, so each combination can be tuned on its own.
 */
data class TemperaturePreset(
    val kind: PresetKind,
    val normal: SetpointPair,
    val extreme: SetpointPair,
) {
    /**
     * The setpoint this preset writes right now, or null when the mode makes it meaningless.
     *
     * Null for everything but heat and cool: `off` has nothing to target, and `heat_cool`/`auto`
     * drive a low/high pair that a single setpoint can't express (the same reason the stepper backs
     * off in that mode).
     */
    fun setpointFor(mode: HvacMode?, extremeActive: Boolean): Double? {
        val pair = if (extremeActive) extreme else normal
        return when (mode) {
            HvacMode.Heat -> pair.heat
            HvacMode.Cool -> pair.cool
            else -> null
        }
    }

    companion object
}

/** A preset's two setpoints — which one applies depends on the thermostat's current hvac mode. */
data class SetpointPair(val heat: Double, val cool: Double)

/** The presets the dashboard offers. [Extreme] is not one of these — it's a modifier, not a target. */
enum class PresetKind { Comfort, Sleep, Economy }

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
