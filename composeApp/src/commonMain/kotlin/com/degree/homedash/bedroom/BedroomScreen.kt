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
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.previewLight
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
        onToggle = vm::toggle,
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
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            is EntityAction.SetSpeed -> Unit
            is EntityAction.OpenGraph -> Unit
            is EntityAction.Navigate -> Unit
        }
    }

    DashboardScaffold(
        modifier = modifier,
        title = "Bedroom",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        icon = RoomIcons.Bed,
    ) {
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
            ControlGroup(title = "Lights", entities = ui.lights, useCardUis = true, onAction = onAction)
        }
        if (ui.fans.isNotEmpty()) {
            ControlGroup(title = "Fans", entities = ui.fans, useCardUis = true, onAction = onAction)
        }
        if (ui.climate.isNotEmpty()) {
            ControlGroup(title = "Climate", entities = ui.climate, useCardUis = true, onAction = onAction)
        }
    }
}

@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun BedroomScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            BedroomContent(
                ui = BedroomUiState(
                    lights = listOf(
                        previewLight("West", isOn = true),
                        previewLight("East", isOn = false),
                    ),
                    fans = emptyList(),
                    climate = emptyList(),
                ),
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
            )
        }
    }
}

/** The room before any entities are wired up. */
@Preview(widthDp = 380, heightDp = 260)
@Composable
private fun BedroomEmptyPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            BedroomContent(
                ui = BedroomUiState(emptyList(), emptyList(), emptyList()),
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
            )
        }
    }
}
