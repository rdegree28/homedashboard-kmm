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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.controls.HexagonControl
import com.degree.homedash.controls.WorkstationControl
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold

@Composable
fun OfficeScreen(
    repository: HomeAssistantRepo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    useCardUis: Boolean = false,
) {
    val vm: OfficeViewModel = viewModel { OfficeViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    OfficeContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onToggle = vm::toggle,
        onSetFanSpeed = vm::setFanSpeed,
        onSignal = vm::signal,
        useCardUis = useCardUis,
    )
}

/**
 * Stateless Office UI — a projected [OfficeUiState] in, all actions out. Rendered by [OfficeScreen]
 * and previews. When [useCardUis] is true, sections that support it (currently Lights) render as
 * a grid of tappable cards instead of rows.
 */
@Composable
fun OfficeContent(
    ui: OfficeUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggle: (String) -> Unit,
    onSetFanSpeed: (String, Int) -> Unit,
    onSignal: (SignalMode) -> Unit,
    useCardUis: Boolean = false,
) {
    val onAction: (EntityAction) -> Unit = { action ->
        when (action) {
            is EntityAction.Toggle -> onToggle(action.entityId)
            is EntityAction.SetSpeed -> onSetFanSpeed(action.entityId, action.percentage)
            is EntityAction.OpenGraph -> Unit // Office has no graph navigation
        }
    }

    DashboardScaffold(
        title = "Office",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        connection = ui.connection,
    ) {
        ControlGroup(
            title = "Lights",
            entities = listOf(ui.officeLight, ui.smallLight),
            useCardUis = useCardUis,
            onAction = onAction,
        )

        ControlGroup(
            title = "Fans",
            entities = listOf(ui.officeFan, ui.boxFan),
            onAction = onAction,
        )

        ControlGroup("Status") {
            SignalSelector(ui.activeSignal, onSignal)
        }

        ControlGroup(
            title = "Climate",
            entities = listOf(ui.temperature, ui.humidity),
            onAction = onAction,
        )

        ControlGroup(
            title = "Doors",
            entities = listOf(ui.door),
            onAction = onAction,
        )

        ControlGroup("Workstation") {
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

private fun signalColor(mode: SignalMode): Color = when (mode) {
    SignalMode.OFF -> AppColors.SignalOff
    SignalMode.AVAILABLE -> AppColors.StatusGreen
    SignalMode.FOCUSED -> AppColors.StatusAmber
    SignalMode.MEETING -> AppColors.StatusRed
}

/** Drives [OfficeScreenPreview] twice: rows (false) and card UIs (true). */
private class UseCardUisProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreenPreview(
    @PreviewParameter(UseCardUisProvider::class) useCardUis: Boolean,
) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent(
                ui = previewOfficeUiState,
                onBack = {},
                onOpenSettings = {},
                onToggle = {},
                onSetFanSpeed = { _, _ -> },
                onSignal = {},
                useCardUis = useCardUis,
            )
        }
    }
}
