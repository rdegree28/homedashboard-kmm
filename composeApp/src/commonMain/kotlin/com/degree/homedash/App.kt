package com.degree.homedash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.di.appModule
import com.degree.homedash.bedroom.BedroomScreen
import com.degree.homedash.livingroom.LivingRoomScreen
import com.degree.homedash.office.OfficeScreen
import com.degree.homedash.pets.PetsScreen
import com.degree.homedash.pets.WaterGraphScreen
import com.degree.homedash.plants.PlantGraphScreen
import com.degree.homedash.plants.PlantsScreen
import com.degree.homedash.shared.model.FeatureFlag
import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.di.sharedModule
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata.NavigationTarget
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.LocalHaConnectionStatus
import com.degree.homedash.update.rememberUpdateAvailable
import com.degree.homedash.update.reloadApp
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(defaultConfig: HaConfig? = null) {
    // The single Koin start for both platforms — the entry points (MainActivity, wasmJs main) just
    // call App(), so starting here avoids duplicating it and can't double-start on Activity recreation.
    KoinApplication(application = { modules(sharedModule, appModule(defaultConfig)) }) {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    val appVm: AppViewModel = koinViewModel()
    val config by appVm.config.collectAsStateWithLifecycle()
    val currentUser by appVm.currentUser.collectAsStateWithLifecycle()
    val featureFlags by appVm.featureFlags.collectAsStateWithLifecycle()
    val repository: HomeAssistantRepo = koinInject()
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
            CompositionLocalProvider(LocalHaConnectionStatus provides connection) {
                Box(
                    Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ),
                ) {
                    // Sits above the screen content so a tab left open for days can still be updated.
                    if (rememberUpdateAvailable()) {
                        UpdateBanner(onReload = ::reloadApp, modifier = Modifier.align(Alignment.BottomCenter).zIndex(1f))
                    }
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
                            users = appVm.users,
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
                            onNavigate = { target -> navigate(target.toScreen()) },
                            onOpenSettings = { navigate(Screen.Settings) },
                        )

                        Screen.Office -> OfficeScreen(
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                        )

                        Screen.Plants -> PlantsScreen(
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                            onOpenGraph = { id ->
                                graphEntityId = id
                                navigate(Screen.PlantGraph)
                            },
                        )

                        Screen.PlantGraph -> PlantGraphScreen(
                            entityId = graphEntityId.orEmpty(),
                            onBack = ::goBack,
                        )

                        Screen.Bedroom -> BedroomScreen(
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                        )

                        Screen.LivingRoom -> LivingRoomScreen(
                            showLights = FeatureFlag.ViewLivingRoomLights in featureFlags,
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                        )

                        Screen.Pets -> PetsScreen(
                            onBack = ::goBack,
                            onOpenSettings = { navigate(Screen.Settings) },
                            onOpenGraph = { id ->
                                graphEntityId = id
                                navigate(Screen.WaterGraph)
                            },
                        )

                        Screen.WaterGraph -> WaterGraphScreen(
                            entityId = graphEntityId.orEmpty(),
                            onBack = ::goBack,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Offers a reload when a newer build has been deployed. Deliberately a prompt rather than an
 * automatic reload — this runs on a wall tablet, and reloading under someone's finger is worse than
 * being a version behind for a minute.
 */
@Composable
private fun UpdateBanner(onReload: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onReload,
        modifier = modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = AppColors.ColumbiaBlue,
        contentColor = AppColors.ColumbiaBlueOn,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("A new version is available", style = MaterialTheme.typography.bodyLarge)
            Text("Reload", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** Top-level destinations; the launcher (Home) is the root of the back stack. */
private enum class Screen { Home, Office, Plants, LivingRoom, Bedroom, Pets, Settings, Login, PlantGraph, WaterGraph }

/** Maps a launcher card's destination onto this file's private [Screen] stack. */
private fun NavigationTarget.toScreen(): Screen = when (this) {
    NavigationTarget.Office -> Screen.Office
    NavigationTarget.Plants -> Screen.Plants
    NavigationTarget.LivingRoom -> Screen.LivingRoom
    NavigationTarget.Bedroom -> Screen.Bedroom
    NavigationTarget.Pets -> Screen.Pets
}
