package com.degree.homedash.ui

import androidx.compose.ui.graphics.Color

/**
 * Shared accent/status colors used across dashboards. Material theme colors
 * ([androidx.compose.material3.MaterialTheme]) cover surfaces and text; these are the few semantic
 * hues (status, signals, soil moisture, charts) that aren't part of the theme.
 */
object AppColors {
    val StatusGreen = Color(0xFF2EA043) // connected, signal "available"
    val StatusAmber = Color(0xFFD9A406) // connecting, signal "focused", soil getting dry
    val StatusRed = Color(0xFFD83A3A) // error, signal "meeting", soil too dry
    val StatusGray = Color(0xFF888888) // disconnected, no reading
    val SignalOff = Color(0xFFB6B6B6) // signal "off"
    val Healthy = Color(0xFF66BB6A) // plants card, healthy soil
    val Wet = Color(0xFF4FC3F7) // humidity, very-wet soil, office card
    val TempWarm = Color(0xFFFF8A65) // temperature
    val Accent = Color(0xFF4C8DFF) // chart line/fill, fan on
    val LightOn = Color(0xFFFFC107) // light bulb on (amber)
    val WorkstationOn = Color(0xFFFFE135) // workstation on (banana)
    val GridLine = Color(0x22FFFFFF) // chart gridlines
    val ColumbiaBlue = Color(0xFF3458B2) // dashboard header background
    val ColumbiaBlueOn = Color(0xFFFFFFFF) // dark navy text/icons on the header
}
