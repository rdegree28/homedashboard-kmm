package com.degree.homedash.office

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.data.HaRepository
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.network.ConnectionStatus
import kotlinx.coroutines.launch

@Composable
fun OfficeScreen(repository: HaRepository, onOpenSettings: () -> Unit) {
    val states by repository.states.collectAsState()
    val connection by repository.connection.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Office",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onOpenSettings) { Text("Settings") }
        }
        ConnectionBanner(connection)

        SectionCard("Lights") {
            ControlRow("Office", states[OfficeEntities.OFFICE_LIGHT]) {
                scope.launch { repository.toggle(OfficeEntities.OFFICE_LIGHT) }
            }
            ControlRow("Small", states[OfficeEntities.SMALL_LIGHT]) {
                scope.launch { repository.toggle(OfficeEntities.SMALL_LIGHT) }
            }
        }

        SectionCard("Fans") {
            ControlRow("Office Fan", states[OfficeEntities.OFFICE_FAN]) {
                scope.launch { repository.toggle(OfficeEntities.OFFICE_FAN) }
            }
            ControlRow("Box Fan", states[OfficeEntities.BOX_FAN]) {
                scope.launch { repository.toggle(OfficeEntities.BOX_FAN) }
            }
        }

        SectionCard("Status") {
            SignalSelector(states[OfficeEntities.SIGNAL_MODE]?.state) { mode ->
                scope.launch {
                    when (mode) {
                        SignalMode.OFF -> repository.turnOff(OfficeEntities.TRAFFIC_SIGNAL)
                        SignalMode.AVAILABLE -> repository.runScript(OfficeEntities.SCRIPT_GREEN)
                        SignalMode.FOCUSED -> repository.runScript(OfficeEntities.SCRIPT_AMBER)
                        SignalMode.MEETING -> repository.runScript(OfficeEntities.SCRIPT_RED)
                    }
                }
            }
        }

        SectionCard("Workstation") {
            ControlRow("Workstation", states[OfficeEntities.WORKSTATION]) {
                scope.launch { repository.toggle(OfficeEntities.WORKSTATION) }
            }
            ControlRow("Hexagon Lights", states[OfficeEntities.HEXAGON]) {
                scope.launch { repository.toggle(OfficeEntities.HEXAGON) }
            }
            Spacer(Modifier.height(4.dp))
            StatRow("Power", states[OfficeEntities.POWER])
            StatRow("Total Power Used", states[OfficeEntities.ENERGY])
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun ControlRow(name: String, entity: EntityState?, onToggle: () -> Unit) {
    val isOn = entity?.isOn == true
    val available = entity != null && !entity.isUnavailable
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        Switch(
            checked = isOn,
            enabled = available,
            onCheckedChange = { onToggle() },
        )
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
    val value = entity?.state?.let { "${formatNumeric(it)} $unit".trim() } ?: "—"
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
        ConnectionStatus.Connected -> "Connected" to Color(0xFF2EA043)
        ConnectionStatus.Connecting -> "Connecting…" to Color(0xFFD9A406)
        ConnectionStatus.Disconnected -> "Disconnected" to Color(0xFF888888)
        is ConnectionStatus.Error -> "Error: ${status.message ?: "unknown"}" to Color(0xFFD83A3A)
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

/** Round numeric sensor values to 2 decimals (dropping trailing zeros); pass through non-numbers. */
private fun formatNumeric(raw: String): String {
    val d = raw.toDoubleOrNull() ?: return raw
    val rounded = kotlin.math.round(d * 100.0) / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

private fun signalColor(mode: SignalMode): Color = when (mode) {
    SignalMode.OFF -> Color(0xFFB6B6B6)
    SignalMode.AVAILABLE -> Color(0xFF2EA043)
    SignalMode.FOCUSED -> Color(0xFFD9A406)
    SignalMode.MEETING -> Color(0xFFD83A3A)
}
