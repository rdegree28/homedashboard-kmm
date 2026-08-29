package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.entity.HvacAction
import com.degree.homedash.shared.model.entity.HvacMode

/**
 * Live state of a thermostat.
 *
 * Unlike the sensor states the numbers stay numbers rather than pre-formatted text: the setpoint
 * stepper does arithmetic on them, and the card computes a dew point from the pair, so a display
 * string would only have to be parsed back.
 *
 * Every reading is null while the unit is offline — some integrations keep stale attributes around
 * when a device drops off the network, and a live-looking readout for a unit that isn't there is
 * worse than a dash.
 */
data class ThermostatState(
    override val entityId: String,
    override val isOffline: Boolean,
    /** The entity's own state. null when offline, or reporting a mode we don't model. */
    val hvacMode: HvacMode? = null,
    /** `hvac_action` — the live Idle/Heating/Cooling badge. null when the device omits it. */
    val hvacAction: HvacAction? = null,
    /** `temperature`. Absent in heat_cool, which publishes a low/high pair instead. */
    val targetTemperature: Double? = null,
    /** `current_temperature` — the ambient reading shown under the setpoint. */
    val currentTemperature: Double? = null,
    /** `current_humidity`, as a percentage. null on the many thermostats that don't report it. */
    val currentHumidity: Double? = null,
    /** Raw Home Assistant strings, matched against the lists the metadata declares. */
    val fanMode: String? = null,
    val presetMode: String? = null,
    /**
     * Whether the extreme setpoints are in force. Read off the `input_boolean` the metadata names,
     * not the thermostat — so it survives the unit being unavailable, since the preset pills still
     * need to show which setpoints they would write.
     */
    val extremeActive: Boolean = false,
) : DeviceState
