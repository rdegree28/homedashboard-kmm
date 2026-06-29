package com.degree.homedash.ui

import java.time.Instant
import java.time.ZoneId

actual fun localUtcOffsetSeconds(): Int =
    ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds
