package com.degree.homedash.office

/** Entity ids for the Office room, mirrored from the existing Lovelace dashboard. */
object OfficeEntities {
    const val OFFICE_LIGHT = "light.office_light"
    const val SMALL_LIGHT = "light.office_small_light"

    const val OFFICE_FAN = "fan.office_fan_office_fan"
    const val BOX_FAN = "fan.office_box_fan"

    const val SIGNAL_MODE = "sensor.office_signal_mode"
    const val TRAFFIC_SIGNAL = "light.office_traffic_signal"
    const val SCRIPT_GREEN = "script.office_signal_set_green"
    const val SCRIPT_AMBER = "script.office_signal_set_amber"
    const val SCRIPT_RED = "script.office_signal_set_red"

    const val WORKSTATION = "switch.office_workstation"
    const val HEXAGON = "light.hexagon_lights"

    const val POWER = "sensor.office_workstation_power"
    const val ENERGY = "sensor.office_workstation_summation_delivered"
}

/** The four office "signal" states; [stateValue] matches `sensor.office_signal_mode`. */
enum class SignalMode(val label: String, val stateValue: String) {
    OFF("Off", "off"),
    AVAILABLE("Available", "green"),
    FOCUSED("Focused", "amber"),
    MEETING("Meeting", "red"),
}
