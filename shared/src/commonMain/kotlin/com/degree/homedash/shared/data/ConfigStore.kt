package com.degree.homedash.shared.data

import com.degree.homedash.shared.api.HaConfig
import com.russhwolf.settings.Settings

/** Persists the [com.degree.homedash.shared.api.HaConfig] (base URL + token) across launches. */
class ConfigStore(
    private val settings: Settings = createSettings(),
) {

    /** The saved config, or null when none has been stored yet. */
    fun load(): HaConfig? {
        val url = settings.getStringOrNull(KEY_URL)
        val token = settings.getStringOrNull(KEY_TOKEN)
        return if (!url.isNullOrBlank() && !token.isNullOrBlank()) HaConfig(url, token) else null
    }

    /** Stores [config], replacing whatever was saved before. */
    fun save(config: HaConfig) {
        settings.putString(KEY_URL, config.baseUrl)
        settings.putString(KEY_TOKEN, config.token)
    }

    /** Forgets the saved config, so the app starts at Settings again. */
    fun clear() {
        settings.remove(KEY_URL)
        settings.remove(KEY_TOKEN)
    }

    private companion object {
        const val KEY_URL = "ha_url"
        const val KEY_TOKEN = "ha_token"
    }
}
