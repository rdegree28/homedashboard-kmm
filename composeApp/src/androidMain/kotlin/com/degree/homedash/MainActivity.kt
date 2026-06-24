package com.degree.homedash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.degree.homedash.shared.data.HaConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val config = BuildConfig.HA_URL.takeIf { it.isNotBlank() }
            ?.let { url -> HaConfig(baseUrl = url, token = BuildConfig.HA_TOKEN) }

        setContent {
            App(config)
        }
    }
}
