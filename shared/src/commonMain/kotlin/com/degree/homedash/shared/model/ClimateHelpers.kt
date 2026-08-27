package com.degree.homedash.shared.model

import kotlin.math.ln

/**
 * Dew point (Magnus–Tetens) in the same scale [temperature] is given in, or null when [rh] isn't a
 * usable percentage — the formula's `ln(rh/100)` diverges at zero.
 *
 * Lives here rather than in the UI because it is a reading in its own right: a device state computes
 * it from the sensor pair it names (see `ClimateMetadata.DewPointSource`), and a thermostat
 * card computes it from the `current_temperature`/`current_humidity` attributes it already holds.
 */
fun dewPoint(
    temperature: Double,
    rh: Double,
    fahrenheit: Boolean,
): Double? {
    if (rh <= 0.0) return null
    val tempC = if (fahrenheit) (temperature - 32.0) * 5.0 / 9.0 else temperature
    val dewC = dewPointCelsius(tempC, rh)
    return if (fahrenheit) dewC * 9.0 / 5.0 + 32.0 else dewC
}

/** Magnus–Tetens dew point in °C from a Celsius temperature and relative humidity percentage (0–100). */
private fun dewPointCelsius(
    tempC: Double,
    rh: Double,
): Double {
    val a = 17.62
    val b = 243.12
    val gamma = ln(rh / 100.0) + a * tempC / (b + tempC)
    return b * gamma / (a - gamma)
}
