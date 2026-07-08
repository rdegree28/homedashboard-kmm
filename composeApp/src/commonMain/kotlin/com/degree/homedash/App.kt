package com.degree.homedash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.livingroom.LivingRoomScreen
import com.degree.homedash.livingroom.WaterGraphScreen
import com.degree.homedash.office.OfficeScreen
import com.degree.homedash.plants.PlantGraphScreen
import com.degree.homedash.plants.PlantsScreen
import com.degree.homedash.shared.data.HaConfig
import com.degree.homedash.shared.data.Users
import com.degree.homedash.ui.LocalConnectionStatus

@Composable
fun App(defaultConfig: HaConfig? = null) {
    val appVm: AppViewModel = viewModel { AppViewModel(defaultConfig) }
    val config by appVm.config.collectAsStateWithLifecycle()
    val currentUser by appVm.currentUser.collectAsStateWithLifecycle()
    val repository = appVm.repository
    val connection by repository.connection.collectAsStateWithLifecycle()

    // Entity id whose history is shown on the PlantGraph destination.
    var graphEntityId by remember { mutableStateOf<String?>(null) }

    // Navigation back stack; the last entry is the visible screen. Gate on config (Settings) then
    // login (Login) — each acts as an un-poppable root until satisfied — otherwise start on Home.
    val backStack = remember {
        mutableStateListOf(
            when {
                config == null -> Screen.Settings
                currentUser == null -> Screen.Login
                else -> Screen.Home
            }
        )
    }
    fun navigate(screen: Screen) = backStack.add(screen)
    fun goBack() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    // Wire the platform back gesture (Android system back, browser Back button) to the stack.
    PlatformBackHandler(backStackSize = backStack.size, onBack = ::goBack)

    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CompositionLocalProvider(LocalConnectionStatus provides connection) {
                Box(
                    Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ),
                ) {
                    when (backStack.last()) {
                        Screen.Settings -> SettingsScreen(
                            initial = config,
                            currentUser = currentUser,
                            onSave = { cfg ->
                                appVm.save(cfg)
                                if (backStack.size > 1) {
                                    goBack()
                                } else {
                                    backStack[0] = if (currentUser == null) Screen.Login else Screen.Home
                                }
                            },
                            onCancel = if (backStack.size > 1) ({ goBack() }) else null,
                            onLogout = currentUser?.let {
                                {
                                    appVm.logout()
                                    backStack.clear()
                                    backStack.add(Screen.Login)
                                }
                            },
                        )

                        Screen.Login -> LoginScreen(
                            users = Users.all,
                            onLogin = { user, pin ->
                                appVm.login(user, pin).also { ok ->
                                    if (ok) {
                                        backStack.clear()
                                        backStack.add(Screen.Home)
                                    }
                                }
                            },
                        )

                        Screen.Home -> HomeScreen(
                            onOpenOffice = { navigate(Screen.Office) },
                            onOpenPlants = { navigate(Screen.Plants) },
                            onOpenLivingRoom = { navigate(Screen.LivingRoom) },
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

                        Screen.LivingRoom -> LivingRoomScreen(
                            repository = repository,
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                            onOpenGraph = { id ->
                                graphEntityId = id
                                navigate(Screen.WaterGraph)
                            },
                        )

                        Screen.WaterGraph -> WaterGraphScreen(
                            repository = repository,
                            entityId = graphEntityId.orEmpty(),
                            onBack = ::goBack,
                        )
                    }
                }
            }
        }
    }
}

/** Top-level destinations; the launcher (Home) is the root of the back stack. */
private enum class Screen { Home, Office, Plants, LivingRoom, Settings, Login, PlantGraph, WaterGraph }
