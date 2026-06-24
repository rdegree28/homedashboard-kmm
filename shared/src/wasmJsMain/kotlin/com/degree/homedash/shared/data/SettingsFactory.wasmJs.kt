package com.degree.homedash.shared.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

// Backed by the browser's localStorage.
actual fun createSettings(): Settings = StorageSettings()
