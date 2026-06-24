package com.degree.homedash

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.data.HaConfig
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Temporary: seed a default config from local.properties (WebDefaults) so the web build skips
    // the Settings screen. We OVERWRITE any stored config so a stale/broken value saved from an
    // earlier Settings attempt can't shadow this default (which would hang on a bad address).
    val defaultConfig = WebDefaults.HA_URL.takeIf { it.isNotBlank() }
        ?.let { url -> HaConfig(baseUrl = url, token = WebDefaults.HA_TOKEN) }
    if (defaultConfig != null) {
        ConfigStore().save(defaultConfig)
    }

    ComposeViewport(document.body!!) {
        App(defaultConfig)
    }
}
