package com.degree.homedash.shared.data

import com.russhwolf.settings.Settings

// no-arg factory: backed by SharedPreferences via androidx.startup.
actual fun createSettings(): Settings = Settings()
