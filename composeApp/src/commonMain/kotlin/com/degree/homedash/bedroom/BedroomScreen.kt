package com.degree.homedash.bedroom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.PreviewKoin
import com.degree.homedash.controls.previewLightDevice
import com.degree.homedash.shared.model.entity.TriggerDeviceMetadata
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BedroomScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: BedroomViewModel = koinViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    BedroomContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onActivate = vm::activate,
        modifier = modifier,
    )
}

/**
 * Stateless Bedroom UI — projected entity states in, toggle actions out.
 *
 * Each section renders only when it has something in it, so the room can grow from its current pair of
 * lights to fans and climate by adding entries to the repo, with no change here.
 */
@Composable
fun BedroomContent(
    ui: BedroomUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onActivate: (TriggerDeviceMetadata.ServiceCall) -> Unit,
    modifier: Modifier = Modifier,
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
        title = "Bedroom",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        icon = RoomIcons.Bed,
    ) {
        if (ui.triggers.isNotEmpty()) {
            ControlGroup(title = "Scenes", entities = ui.triggers, useCardUis = true, onAction = onAction)
        }
        if (ui.lights.isEmpty() && ui.fans.isEmpty() && ui.climate.isEmpty()) {
            ControlGroup("Devices") {
                Text(
                    "No devices set up for this room yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@DashboardScaffold
        }

        if (ui.lights.isNotEmpty()) {
            ControlGroup(title = "Lights", devices = ui.lights)
        }
        if (ui.fans.isNotEmpty()) {
            ControlGroup(title = "Fans", devices = ui.fans)
        }
        if (ui.climate.isNotEmpty()) {
            ControlGroup(title = "Climate", devices = ui.climate)
        }
    }
}

@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun BedroomScreenPreview() = PreviewKoin {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            BedroomContent(
                ui = BedroomUiState(
                    triggers = emptyList(),
                    lights = listOf(
                        previewLightDevice("West", isOn = true),
                        previewLightDevice("East", isOn = false),
                    ),
                    fans = emptyList(),
                    climate = emptyList(),
                ),
                onBack = {},
                onOpenSettings = {},
                onActivate = {},
            )
        }
    }
}

/** The room before any entities are wired up. */
@Preview(widthDp = 380, heightDp = 260)
@Composable
private fun BedroomEmptyPreview() = PreviewKoin {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            BedroomContent(
                ui = BedroomUiState(emptyList(), emptyList(), emptyList(), emptyList()),
                onBack = {},
                onOpenSettings = {},
                onActivate = {},
            )
        }
    }
}
