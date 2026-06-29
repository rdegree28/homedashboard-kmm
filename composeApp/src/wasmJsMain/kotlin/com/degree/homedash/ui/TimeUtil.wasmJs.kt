package com.degree.homedash.ui

// JS Date.getTimezoneOffset() returns minutes that local time is BEHIND UTC, so negate it.
private fun jsTimezoneOffsetMinutes(): Double = js("new Date().getTimezoneOffset()")

actual fun localUtcOffsetSeconds(): Int = (-jsTimezoneOffsetMinutes() * 60.0).toInt()
