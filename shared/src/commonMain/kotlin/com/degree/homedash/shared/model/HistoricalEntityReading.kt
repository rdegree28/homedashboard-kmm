package com.degree.homedash.shared.model

/**
 * One numeric sensor reading and the unit Home Assistant reports it in — a device state's building
 * block for any measured value, and the units the [HistoryPoint]s of that same sensor are plotted in.
 *
 * [value] is null when the sensor is missing, unavailable, or reporting something non-numeric, which
 * the UI shows as a dash. The unit is kept beside it rather than baked into a display string, so a
 * reading can still be compared, charted, or converted.
 */
data class HistoricalEntityReading(
    val value: Double?,
    val unit: String,
) {
    companion object {
        /** Nothing usable to show. */
        val Missing = HistoricalEntityReading(value = null, unit = "")
    }
}

/**
 * What [this] entity is publishing right now — its numeric state and unit, or
 * [HistoricalEntityReading.Missing] when it is absent, unavailable, or reporting something
 * non-numeric.
 */
internal fun EntityState?.toReading(): HistoricalEntityReading {
    if (this == null || isUnavailable) return HistoricalEntityReading.Missing
    return HistoricalEntityReading(
        value = state.toDoubleOrNull(),
        unit = attrString("unit_of_measurement").orEmpty(),
    )
}
