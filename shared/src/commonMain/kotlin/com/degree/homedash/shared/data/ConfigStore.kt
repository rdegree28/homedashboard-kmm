package com.degree.homedash.shared.data

import com.russhwolf.settings.Settings

/** Persists the [HaConfig] (base URL + token) across launches. */
class ConfigStore(private val settings: Settings = createSettings()) {

    fun load(): HaConfig? {
        val url = settings.getStringOrNull(KEY_URL)
        val token = settings.getStringOrNull(KEY_TOKEN)
        return if (!url.isNullOrBlank() && !token.isNullOrBlank()) HaConfig(url, token) else null
    }

    fun save(config: HaConfig) {
        settings.putString(KEY_URL, config.baseUrl)
        settings.putString(KEY_TOKEN, config.token)
    }

    fun clear() {
        settings.remove(KEY_URL)
        settings.remove(KEY_TOKEN)
    }

    private companion object {
        const val KEY_URL = "ha_url"
        const val KEY_TOKEN = "ha_token"
    }
}
