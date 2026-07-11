package com.degree.homedash.livingroom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold

@Composable
fun LivingRoomScreen(
    modifier: Modifier = Modifier,
    repository: HomeAssistantRepo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    showLights: Boolean = false,
) {
    val vm: LivingRoomViewModel = viewModel { LivingRoomViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    LivingRoomContent(
        modifier = modifier,
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = vm::toggle,
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
    onToggle: (String) -> Unit,
    showLights: Boolean = false,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            is EntityAction.OpenGraph -> Unit
            is EntityAction.SetSpeed -> Unit
        }
    }

    DashboardScaffold(
        modifier = modifier,
        title = "Living Room",
        onBack = onBack,
        onOpenSettings = onOpenSettings
    ) {
        // Living Room controls are gated behind the viewLivingRoomLights feature flag.
        if (showLights) {
            ControlGroup(
                title = "Lights",
                entities = ui.lights,
                useCardUis = true,
                onAction = onAction,
            )

            ControlGroup(
                title = "Fans",
                entities = ui.fans,
                useCardUis = true,
                onAction = onAction,
            )

            ControlGroup(
                title = "Climate",
                entities = ui.climate,
                useCardUis = true,
                onAction = onAction,
            )
        }
    }
}

@Preview(widthDp = 380)
@Composable
private fun LivingRoomScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LivingRoomContent(
                ui = LivingRoomUiState(lights = previewLights, fans = previewFans, climate = previewClimate),
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                showLights = true,
            )
        }
    }
}
