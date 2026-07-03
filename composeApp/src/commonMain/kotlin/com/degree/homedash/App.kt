package com.degree.homedash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.office.OfficeScreen
import com.degree.homedash.plants.PlantGraphScreen
import com.degree.homedash.plants.PlantsScreen
import com.degree.homedash.shared.data.HaConfig

@Composable
fun App(defaultConfig: HaConfig? = null) {
    val appVm: AppViewModel = viewModel { AppViewModel(defaultConfig) }
    val config by appVm.config.collectAsStateWithLifecycle()
    val repository = appVm.repository

    // Entity id whose history is shown on the PlantGraph destination.
    var graphEntityId by remember { mutableStateOf<String?>(null) }

    // Navigation back stack; the last entry is the visible screen. Start on Settings when
    // unconfigured (it then acts as the un-poppable root), otherwise on Home.
    val backStack = remember { mutableStateListOf(if (config == null) Screen.Settings else Screen.Home) }
    fun navigate(screen: Screen) = backStack.add(screen)
    fun goBack() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    // Wire the platform back gesture (Android system back, browser Back button) to the stack.
    PlatformBackHandler(backStackSize = backStack.size, onBack = ::goBack)

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                when (backStack.last()) {
                    Screen.Settings -> SettingsScreen(
                        initial = config,
                        onSave = { cfg ->
                            appVm.save(cfg)
                            if (backStack.size > 1) goBack() else backStack[0] = Screen.Home
                        },
                        onCancel = if (backStack.size > 1) ({ goBack() }) else null,
                    )

                    Screen.Home -> HomeScreen(
                        onOpenOffice = { navigate(Screen.Office) },
                        onOpenPlants = { navigate(Screen.Plants) },
                        onOpenSettings = { navigate(Screen.Settings) },
                    )

                    Screen.Office -> OfficeScreen(
                        repository = repository,
                        onBack = ::goBack,
                        onOpenSettings = { navigate(Screen.Settings) },
                    )

                    Screen.Plants -> PlantsScreen(
                        repository = repository,
                        onBack = ::goBack,
                        onOpenSettings = { navigate(Screen.Settings) },
                        onOpenGraph = { id ->
                            graphEntityId = id
                            navigate(Screen.PlantGraph)
                        },
                    )

                    Screen.PlantGraph -> PlantGraphScreen(
                        repository = repository,
                        entityId = graphEntityId.orEmpty(),
                        onBack = ::goBack,
                    )
                }
            }
        }
    }
}

/** Top-level destinations; the launcher (Home) is the root of the back stack. */
private enum class Screen { Home, Office, Plants, Settings, PlantGraph }
