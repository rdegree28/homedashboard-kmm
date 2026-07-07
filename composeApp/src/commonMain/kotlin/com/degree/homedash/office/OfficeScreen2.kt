package com.degree.homedash.office

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.controls.ClimateRow
import com.degree.homedash.controls.DoorRow
import com.degree.homedash.controls.FanControl
import com.degree.homedash.controls.HexagonControl
import com.degree.homedash.controls.LightControl
import com.degree.homedash.controls.LightControlType
import com.degree.homedash.controls.WorkstationControl
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.SectionCard

/**
 * Variant of [OfficeScreen] that renders the Lights section as a grid of tappable cards
 * (see [LightCardGrid]) instead of rows. All other sections match [OfficeContent].
 */
@Composable
fun OfficeScreen2(repository: HomeAssistantRepo, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val vm: OfficeViewModel = viewModel { OfficeViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    OfficeContent2(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = vm::toggle,
        onSetFanSpeed = vm::setFanSpeed,
        onSignal = vm::signal,
    )
}

/** Stateless Office UI with card-grid lights — mirrors [OfficeContent] except for the Lights section. */
@Composable
fun OfficeContent2(
    ui: OfficeUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggle: (String) -> Unit,
    onSetFanSpeed: (String, Int) -> Unit,
    onSignal: (SignalMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
    ) {
        DashboardHeader("Office", onBack = onBack, onOpenSettings = onOpenSettings)
        ConnectionBanner(ui.connection)

        SectionCard("Lights") {
            LightCardGrid(
                lights = listOf(
                    OfficeEntities.OFFICE_LIGHT to ui.officeLight,
                    OfficeEntities.SMALL_LIGHT to ui.smallLight,
                ),
                onToggle = onToggle,
            )
        }

        SectionCard("Fans") {
            FanControl(
                ui.officeFan,
                onSetSpeed = { pct -> onSetFanSpeed(OfficeEntities.OFFICE_FAN, pct) },
            ) {
                onToggle(OfficeEntities.OFFICE_FAN)
            }
            FanControl(ui.boxFan) {
                onToggle(OfficeEntities.BOX_FAN)
            }
        }

        SectionCard("Status") {
            SignalSelector(ui.activeSignal, onSignal)
        }

        SectionCard("Climate") {
            ClimateRow(ui = ui.temperature, icon = Icons.Filled.Thermostat, tint = AppColors.TempWarm)
            ClimateRow(ui = ui.humidity, icon = Icons.Filled.WaterDrop, tint = AppColors.Wet)
        }

        SectionCard("Doors") {
            DoorRow(ui.door)
        }

        SectionCard("Workstation") {
            WorkstationControl(ui.workstation) { onToggle(OfficeEntities.WORKSTATION) }
            HexagonControl(ui.hexagon) { onToggle(OfficeEntities.HEXAGON) }
            Spacer(Modifier.height(8.dp))
            Text("Power Usage", style = MaterialTheme.typography.titleMedium)
            PowerGraph(ui.powerHistory)
            Spacer(Modifier.height(4.dp))
            StatRow(ui.power)
            StatRow(ui.energy)
        }
    }
}

/**
 * Lays out [lights] (entity id → [ToggleUi]) as equal-width [LightControlType.Card] tiles in a grid
 * of [columns]. Incomplete final rows are padded so cards keep a uniform width.
 */
@Composable
private fun LightCardGrid(
    lights: List<Pair<String, ToggleUi>>,
    onToggle: (String) -> Unit,
    columns: Int = 2,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lights.chunked(columns).forEach { rowLights ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLights.forEach { (id, lightUi) ->
                    LightControl(lightUi, LightControlType.Card, Modifier.weight(1f)) { onToggle(id) }
                }
                repeat(columns - rowLights.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreen2Preview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent2(
                ui = previewOfficeUiState,
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                onSetFanSpeed = { _, _ -> },
                onSignal = {},
            )
        }
    }
}
