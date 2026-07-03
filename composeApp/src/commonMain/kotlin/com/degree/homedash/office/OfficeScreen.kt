package com.degree.homedash.office

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.network.ConnectionStatus
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.SectionCard

@Composable
fun OfficeScreen(repository: HomeAssistantRepo, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val vm: OfficeViewModel = viewModel { OfficeViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    OfficeContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = vm::toggle,
        onSetFanSpeed = vm::setFanSpeed,
        onSignal = vm::signal,
    )
}

/** Stateless Office UI — a projected [OfficeUiState] in, all actions out. Rendered by [OfficeScreen] and previews. */
@Composable
fun OfficeContent(
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
            LightControl(ui.officeLight) { onToggle(OfficeEntities.OFFICE_LIGHT) }
            LightControl(ui.smallLight) { onToggle(OfficeEntities.SMALL_LIGHT) }
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
            ClimateRow(ui.temperature, Icons.Filled.Thermostat, AppColors.TempWarm)
            ClimateRow(ui.humidity, Icons.Filled.WaterDrop, AppColors.Wet)
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

@Composable
private fun SignalSelector(activeSignal: String?, onSelect: (SignalMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SignalMode.entries.forEach { mode ->
            val active = activeSignal == mode.stateValue
            val color = signalColor(mode)
            Button(
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f).height(52.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) color else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = mode.label,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                )
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

@Composable
private fun ConnectionBanner(status: ConnectionStatus) {
    val (text, color) = when (status) {
        ConnectionStatus.Connected -> "Connected" to AppColors.StatusGreen
        ConnectionStatus.Connecting -> "Connecting…" to AppColors.StatusAmber
        ConnectionStatus.Disconnected -> "Disconnected" to AppColors.StatusGray
        is ConnectionStatus.Error -> "Error: ${status.message ?: "unknown"}" to AppColors.StatusRed
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun signalColor(mode: SignalMode): Color = when (mode) {
    SignalMode.OFF -> AppColors.SignalOff
    SignalMode.AVAILABLE -> AppColors.StatusGreen
    SignalMode.FOCUSED -> AppColors.StatusAmber
    SignalMode.MEETING -> AppColors.StatusRed
}

@Preview(widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent(
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
