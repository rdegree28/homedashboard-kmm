package com.degree.homedash.ui

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
