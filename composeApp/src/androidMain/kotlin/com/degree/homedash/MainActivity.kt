package com.degree.homedash

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.degree.homedash.shared.data.HaConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force white (light) status-bar icons — they sit over the dark blue header bar.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        val config = BuildConfig.HA_URL.takeIf { it.isNotBlank() }
            ?.let { url -> HaConfig(baseUrl = url, token = BuildConfig.HA_TOKEN) }

        setContent {
            App(config)
        }
    }
}
