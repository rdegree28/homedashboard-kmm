package com.degree.homedash.shared.model

/** A single numeric history sample: [timeSeconds] is a Unix timestamp (seconds), [value] the reading. */
data class HistoryPoint(
    val timeSeconds: Double,
    val value: Double,
)
