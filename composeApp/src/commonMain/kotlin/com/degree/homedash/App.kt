package com.degree.homedash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.degree.homedash.office.OfficeScreen
import com.degree.homedash.plants.PlantsScreen
import com.degree.homedash.shared.data.ConfigStore
import com.degree.homedash.shared.data.HaConfig
import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.network.HaWebSocketClient

@Composable
fun App(defaultConfig: HaConfig? = null) {
    val configStore = remember { ConfigStore() }
    val repository = remember { HaRepository(HaWebSocketClient()) }

    var config by remember { mutableStateOf(configStore.load() ?: defaultConfig) }
    var showSettings by remember { mutableStateOf(config == null) }
    var screen by remember { mutableStateOf(Screen.Home) }

    LaunchedEffect(config) {
        config?.let { repository.connect(it) }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val current = config
            if (showSettings || current == null) {
                SettingsScreen(
                    initial = current,
                    onSave = { cfg ->
                        configStore.save(cfg)
                        config = cfg
                        showSettings = false
                    },
                    onCancel = if (current != null) ({ showSettings = false }) else null,
                )
            } else {
                when (screen) {
                    Screen.Home -> HomeScreen(
                        onOpenOffice = { screen = Screen.Office },
                        onOpenPlants = { screen = Screen.Plants },
                        onOpenSettings = { showSettings = true },
                    )

                    Screen.Office -> OfficeScreen(
                        repository = repository,
                        onBack = { screen = Screen.Home },
                        onOpenSettings = { showSettings = true },
                    )

                    Screen.Plants -> PlantsScreen(
                        repository = repository,
                        onBack = { screen = Screen.Home },
                        onOpenSettings = { showSettings = true },
                    )
                }
            }
        }
    }
}

/** Top-level destinations reachable from the home launcher. */
private enum class Screen { Home, Office, Plants }
