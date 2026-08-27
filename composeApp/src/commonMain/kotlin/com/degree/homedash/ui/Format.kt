package com.degree.homedash.ui

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.states.ClimateState
import com.degree.homedash.shared.model.HistoricalEntityReading
import kotlin.math.pow
import kotlin.math.round

/** Round [value] to [decimals] places, dropping trailing zeros (e.g. 72.50 -> "72.5", 48.0 -> "48"). */
fun formatNumber(value: Double, decimals: Int = 1): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(value * factor) / factor
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

/** Like [formatNumber] but takes a raw string, passing non-numeric values through unchanged. */
fun formatNumberOrSelf(raw: String, decimals: Int = 1): String =
    raw.toDoubleOrNull()?.let { formatNumber(it, decimals) } ?: raw

/**
 * An entity's state formatted for display: the reading rounded to [decimals], with its
 * `unit_of_measurement` appended. A missing entity always reads "—".
 *
 * [dashWhenUnavailable] also dashes an entity reporting `unavailable`/`unknown`, which is what most
 * readouts want. Pass false for sensors that should show whatever HA last reported instead.
 */
fun EntityState?.readingText(decimals: Int, dashWhenUnavailable: Boolean = true): String {
    if (this == null) return "—"
    if (dashWhenUnavailable && isUnavailable) return "—"
    val unit = attrString("unit_of_measurement").orEmpty()
    return "${formatNumberOrSelf(state, decimals)} $unit".trim()
}

fun ClimateState?.readingText(decimals: Int): String {
    if (this == null || isOffline) return "—"
    return reading.readingText(decimals)
}

/**
 * A sensor reading formatted for display: the value rounded to [decimals] with its unit appended, or
 * "—" when the sensor has nothing usable to report.
 */
fun HistoricalEntityReading.readingText(decimals: Int): String {
    val number = value ?: return "—"
    return "${formatNumber(number, decimals)} $unit".trim()
}
