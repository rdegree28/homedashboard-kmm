package com.degree.homedash.ui

import com.degree.homedash.shared.model.EntityState
import kotlin.math.ln

/**
 * Formatted dew point derived from a temperature + relative-humidity sensor pair (Magnus–Tetens),
 * in the temperature sensor's own unit (°F/°C). Returns null until both sensors report a usable
 * numeric value (temperature present, relative humidity present and > 0).
 */
fun dewPointText(tempState: EntityState?, humidityState: EntityState?): String? {
    val tempUnit = tempState?.attrString("unit_of_measurement").orEmpty()
    val fahrenheit = tempUnit.contains("F", ignoreCase = true)
    val temp = tempState?.state?.toDoubleOrNull()?.takeUnless { tempState.isUnavailable } ?: return null
    val rh = humidityState?.state?.toDoubleOrNull()?.takeUnless { humidityState.isUnavailable } ?: return null
    if (rh <= 0.0) return null

    val tempC = if (fahrenheit) (temp - 32.0) * 5.0 / 9.0 else temp
    val dewC = dewPointCelsius(tempC, rh)
    val dew = if (fahrenheit) dewC * 9.0 / 5.0 + 32.0 else dewC
    return "${formatNumber(dew, decimals = 1)} $tempUnit".trim()
}

/** Magnus–Tetens dew point in °C from a Celsius temperature and relative humidity percentage (0–100). */
private fun dewPointCelsius(tempC: Double, rh: Double): Double {
    val a = 17.62
    val b = 243.12
    val gamma = ln(rh / 100.0) + a * tempC / (b + tempC)
    return b * gamma / (a - gamma)
}
