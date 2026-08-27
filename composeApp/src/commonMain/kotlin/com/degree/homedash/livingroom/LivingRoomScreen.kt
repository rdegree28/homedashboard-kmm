package com.degree.homedash.livingroom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.PreviewKoin
import com.degree.homedash.controls.previewTrigger
import com.degree.homedash.shared.model.entity.TriggerDeviceMetadata
import org.koin.compose.viewmodel.koinViewModel
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons

@Composable
fun LivingRoomScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    showLights: Boolean = false,
) {
    val vm: LivingRoomViewModel = koinViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    LivingRoomContent(
        modifier = modifier,
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onActivate = vm::activate,
        showLights = showLights,
    )
}

/** Stateless Living Room UI — projected light states in, toggle actions out. */
@Composable
fun LivingRoomContent(
    modifier: Modifier = Modifier,
    ui: LivingRoomUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onActivate: (TriggerDeviceMetadata.ServiceCall) -> Unit,
    showLights: Boolean = false,
) {
    // Scene cards are the only entities left on the action path; the devices carry their own behavior.
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Activate -> onActivate(action.call)
            else -> Unit
        }
    }

    DashboardScaffold(
        modifier = modifier,
        title = "Living Room",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        icon = RoomIcons.Sofa,
    ) {
        // Living Room controls are gated behind the viewLivingRoomLights feature flag.
        if (showLights) {
            if (ui.triggers.isNotEmpty()) {
                ControlGroup(
                    title = "Scenes",
                    entities = ui.triggers,
                    useCardUis = true,
                    onAction = onAction,
                )
            }

            ControlGroup(
                title = "Lights",
                devices = ui.lights,
            )

            ControlGroup(
                title = "Fans",
                devices = ui.fans,
            )

            ControlGroup(
                title = "Climate",
                devices = ui.climate,
            )
        }
    }
}

@Preview(widthDp = 380)
@Composable
private fun LivingRoomScreenPreview() = PreviewKoin {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LivingRoomContent(
                ui = LivingRoomUiState(
                    triggers = listOf(previewTrigger("Main Lights")),
                    lights = previewLights,
                    fans = previewFans,
                    climate = previewClimate,
                ),
                onBack = {},
                onOpenSettings = {},
                onActivate = {},
                showLights = true,
            )
        }
    }
}
