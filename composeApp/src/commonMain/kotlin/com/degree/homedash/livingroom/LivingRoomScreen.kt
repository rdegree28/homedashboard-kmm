package com.degree.homedash.livingroom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold

@Composable
fun LivingRoomScreen(
    repository: HomeAssistantRepo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
    showLights: Boolean = false,
) {
    val vm: LivingRoomViewModel = viewModel { LivingRoomViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    LivingRoomContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onOpenGraph = onOpenGraph,
        onToggle = vm::toggle,
        showLights = showLights,
    )
}

/** Stateless Living Room UI — projected sensor readings in, navigation actions out. */
@Composable
fun LivingRoomContent(
    ui: LivingRoomUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
    onToggle: (String) -> Unit,
    showLights: Boolean = false,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            is EntityAction.OpenGraph -> onOpenGraph(action.entityId)
            is EntityAction.SetSpeed -> Unit
        }
    }

    DashboardScaffold(title = "Living Room", onBack = onBack, onOpenSettings = onOpenSettings) {
        // Lights are gated behind the viewLivingRoomLights feature flag.
        if (showLights) {
            ControlGroup(
                title = "Lights",
                entities = ui.lights,
                useCardUis = true,
                onAction = onAction,
            )
        }

        ControlGroup(
            title = "Cat Water Fountain",
            entities = ui.items,
            onAction = onAction,
            empty = {
                Text(
                    "No water level sensor found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun LivingRoomScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LivingRoomContent(
                ui = LivingRoomUiState(lights = previewLights, items = previewLevels),
                onBack = {},
                onOpenSettings = {},
                onOpenGraph = {},
                onToggle = {},
                showLights = true,
            )
        }
    }
}
