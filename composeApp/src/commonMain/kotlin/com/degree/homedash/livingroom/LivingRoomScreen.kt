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
import com.degree.homedash.controls.previewTrigger
import com.degree.homedash.shared.model.entity.TriggerEntityMetadata
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
        onToggle = vm::toggle,
        onSetFanSpeed = vm::setFanSpeed,
        onSetOscillating = vm::setOscillating,
        onSetMisting = vm::setMisting,
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
    onToggle: (String) -> Unit,
    onSetFanSpeed: (String, Int) -> Unit,
    onSetOscillating: (String, Boolean) -> Unit,
    onSetMisting: (String, Boolean) -> Unit,
    onActivate: (TriggerEntityMetadata.ServiceCall) -> Unit,
    showLights: Boolean = false,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            is EntityAction.Activate -> onActivate(action.call)
            is EntityAction.OpenGraph -> Unit
            is EntityAction.SetSpeed -> onSetFanSpeed(action.entityId, action.percentage)
            is EntityAction.SetOscillating -> onSetOscillating(action.entityId, action.oscillating)
            is EntityAction.SetMisting -> onSetMisting(action.entityId, action.misting)
            // The thermostat moved to the Home launcher, so nothing here emits these any more.
            is EntityAction.SetTargetTemperature,
            is EntityAction.SetHvacMode,
            is EntityAction.SetThermostatFanMode,
            is EntityAction.SetPresetMode,
            is EntityAction.SetExtremeTemperatures,
            is EntityAction.Navigate,
            -> Unit
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
                expEntities = ui.lights,
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
                ui = LivingRoomUiState(
                    triggers = listOf(previewTrigger("Main Lights")),
                    lights = emptyList(),
                    fans = previewFans,
                    climate = previewClimate,
                ),
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                onSetFanSpeed = { _, _ -> },
                onSetOscillating = { _, _ -> },
                onSetMisting = { _, _ -> },
                onActivate = {},
                showLights = true,
            )
        }
    }
}
