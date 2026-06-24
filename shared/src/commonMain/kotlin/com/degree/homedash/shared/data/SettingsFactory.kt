package com.degree.homedash.shared.data

import com.russhwolf.settings.Settings

/** Platform-backed key-value store (SharedPreferences on Android, localStorage on web). */
expect fun createSettings(): Settings
