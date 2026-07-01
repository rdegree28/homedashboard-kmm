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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.SectionCard
import com.degree.homedash.ui.formatNumberOrSelf
import kotlinx.coroutines.launch

@Composable
fun OfficeScreen(repository: HaRepository, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val states by repository.states.collectAsState()
    val connection by repository.connection.collectAsState()
    val scope = rememberCoroutineScope()

    var powerHistory by remember { mutableStateOf<List<HistoryPoint>>(emptyList()) }
    LaunchedEffect(connection) {
        if (connection == ConnectionStatus.Connected) {
            runCatching { powerHistory = repository.powerHistory(OfficeEntities.POWER, hoursBack = 168) }
        }
    }

    OfficeContent(
        states = states,
        connection = connection,
        powerHistory = powerHistory,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = { entityId -> scope.launch { repository.toggle(entityId) } },
        onSetFanSpeed = { id, pct -> scope.launch { repository.setFanPercentage(id, pct) } },
        onSignal = { mode ->
            scope.launch {
                when (mode) {
                    SignalMode.OFF -> repository.turnOff(OfficeEntities.TRAFFIC_SIGNAL)
                    SignalMode.AVAILABLE -> repository.runScript(OfficeEntities.SCRIPT_GREEN)
                    SignalMode.FOCUSED -> repository.runScript(OfficeEntities.SCRIPT_AMBER)
                    SignalMode.MEETING -> repository.runScript(OfficeEntities.SCRIPT_RED)
                }
            }
        },
    )
}

/** Stateless Office UI — all data in, all actions out. Rendered by [OfficeScreen] and previews. */
@Composable
fun OfficeContent(
    states: Map<String, EntityState>,
    connection: ConnectionStatus,
    powerHistory: List<HistoryPoint>,
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
        ConnectionBanner(connection)

        SectionCard("Lights") {
            LightControl("Office", states[OfficeEntities.OFFICE_LIGHT], Icons.Filled.Lightbulb) {
                onToggle(OfficeEntities.OFFICE_LIGHT)
            }
            LightControl("Small", states[OfficeEntities.SMALL_LIGHT], Icons.Filled.Lightbulb) {
                onToggle(OfficeEntities.SMALL_LIGHT)
            }
        }

        SectionCard("Fans") {
            FanControl(
                "Office Fan",
                states[OfficeEntities.OFFICE_FAN],
                onSetSpeed = { pct -> onSetFanSpeed(OfficeEntities.OFFICE_FAN, pct) },
            ) {
                onToggle(OfficeEntities.OFFICE_FAN)
            }
            FanControl("Box Fan", states[OfficeEntities.BOX_FAN]) {
                onToggle(OfficeEntities.BOX_FAN)
            }
        }

        SectionCard("Status") {
            SignalSelector(states[OfficeEntities.SIGNAL_MODE]?.state, onSignal)
        }

        SectionCard("Climate") {
            ClimateRow(
                "Temperature",
                states[OfficeEntities.TEMPERATURE],
                Icons.Filled.Thermostat,
                AppColors.TempWarm,
            )
            ClimateRow(
                "Humidity",
                states[OfficeEntities.HUMIDITY],
                Icons.Filled.WaterDrop,
                AppColors.Wet,
            )
        }

        SectionCard("Doors") {
            DoorRow("Office Door", states[OfficeEntities.DOOR])
        }

        SectionCard("Workstation") {
            WorkstationControl("Workstation", states[OfficeEntities.WORKSTATION]) {
                onToggle(OfficeEntities.WORKSTATION)
            }
            HexagonControl("Hexagon Lights", states[OfficeEntities.HEXAGON]) {
                onToggle(OfficeEntities.HEXAGON)
            }
            Spacer(Modifier.height(8.dp))
            Text("Power Usage", style = MaterialTheme.typography.titleMedium)
            PowerGraph(powerHistory)
            Spacer(Modifier.height(4.dp))
            StatRow("Power", states[OfficeEntities.POWER])
            StatRow("Total Power Used", states[OfficeEntities.ENERGY])
        }
    }
}

@Composable
private fun SignalSelector(currentState: String?, onSelect: (SignalMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SignalMode.entries.forEach { mode ->
            val active = currentState == mode.stateValue
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
private fun StatRow(label: String, entity: EntityState?) {
    val unit = entity?.attrString("unit_of_measurement").orEmpty()
    val value = entity?.state?.let { "${formatNumberOrSelf(it, decimals = 2)} $unit".trim() } ?: "—"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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
                states = previewStates,
                connection = ConnectionStatus.Connected,
                powerHistory = previewHistory,
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                onSetFanSpeed = { _, _ -> },
                onSignal = {},
            )
        }
    }
}
