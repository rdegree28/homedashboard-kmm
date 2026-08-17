package com.degree.homedash.shared.model

/**
 * A single numeric history sample: [timeSeconds] is a Unix timestamp (seconds), [value] the reading.
 *
 * [min]/[max] describe the spread the sample covers, and default to [value] — a raw recorder state is
 * an instantaneous reading, so it has none. Long-term statistics buckets (an hour or a day of data
 * reduced to mean/min/max) carry a real spread here, which charts draw as a band behind the line.
 */
data class HistoryPoint(
    val timeSeconds: Double,
    val value: Double,
    val min: Double = value,
    val max: Double = value,
)
