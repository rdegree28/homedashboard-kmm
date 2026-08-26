package com.degree.homedash.office

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.controls.PreviewKoin
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.WorkstationControl
import org.koin.compose.viewmodel.koinViewModel
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons

@Composable
fun OfficeScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val vm: OfficeViewModel = koinViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    OfficeContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = vm::toggle,
    )
}

/**
 * Stateless Office UI — a projected [OfficeUiState] in, all actions out. Rendered by [OfficeScreen]
 * and previews. Lights, Fans, and Climate render as card grids; the remaining sections (Status,
 * Doors, Workstation) stay as rows.
 */
@Composable
fun OfficeContent(
    ui: OfficeUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggle: (String) -> Unit,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            // The office heater has no metadata entry yet, so no thermostat reaches this screen.
            is EntityAction.SetTargetTemperature -> Unit
            is EntityAction.SetHvacMode -> Unit
            is EntityAction.SetThermostatFanMode -> Unit
            is EntityAction.SetPresetMode -> Unit
            is EntityAction.SetExtremeTemperatures -> Unit
            is EntityAction.OpenGraph -> Unit // Office has no graph navigation
            is EntityAction.Activate -> Unit // no scene cards on this screen yet
            is EntityAction.Navigate -> Unit // launcher-only
            else -> throw IllegalStateException("No longer using onAction")
        }
    }

    DashboardScaffold(
        title = "Office",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        connection = ui.connection,
        icon = RoomIcons.Desk,
    ) {
        ControlGroup(
            title = "Lights",
            devices = ui.lights,
        )

        ControlGroup(
            title = "Fans",
            devices = ui.fans,
        )

        ControlGroup(
            title = "Signal",
            devices = listOfNotNull(ui.signal),
        )

        ControlGroup(
            title = "Climate",
            devices = ui.climate,
        )

        ControlGroup(
            title = "Doors",
            devices = ui.doors,
        )

        ui.workstation?.run {
            ControlGroup("Workstation", titleOutsideCard = true) {
                WorkstationControl(ui.workstation) { onToggle(OfficeEntities.WORKSTATION) }
//            Spacer(Modifier.height(8.dp))
//            Text("Power Usage", style = MaterialTheme.typography.titleMedium)
//            PowerGraph(ui.powerHistory)
//            Spacer(Modifier.height(4.dp))
//            StatRow(ui.power)
//            StatRow(ui.energy)
            }
        }
    }
}

@Composable
private fun StatRow(ui: SensorUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(ui.label, style = MaterialTheme.typography.bodyLarge)
        Text(ui.valueText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}


@Preview(widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreenPreview() = PreviewKoin {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent(
                ui = previewOfficeUiState,
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
            )
        }
    }
}
