package com.degree.homedash.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.degree.homedash.controls.SoilMoistureControl
import com.degree.homedash.controls.moistureColor
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.HistoryGraph
import com.degree.homedash.ui.ControlGroup
import kotlin.math.roundToInt

/** Selectable history windows for the moisture graph. */
enum class TimeRange(val label: String, val hoursBack: Int) {
    DAY("24h", 24),
    WEEK("7d", 24 * 7),
    MONTH("30d", 24 * 30),
    YEAR("1y", 24 * 365),
}

@Composable
fun PlantGraphScreen(repository: HomeAssistantRepo, entityId: String, onBack: () -> Unit) {
    val vm: PlantGraphViewModel = viewModel(key = entityId) { PlantGraphViewModel(repository, entityId) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    PlantGraphContent(ui = ui, onRangeChange = vm::setRange, onBack = onBack)
}

/** Stateless moisture graph view: current reading, a range selector, and the history chart. */
@Composable
fun PlantGraphContent(
    ui: PlantGraphUiState,
    onRangeChange: (TimeRange) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
    ) {
        DashboardHeader(ui.plant?.name ?: "Plant", onBack = onBack)

        ControlGroup("Soil Moisture") {
            ui.plant?.let { SoilMoistureControl(it) }
            Spacer(Modifier.height(8.dp))
            RangeSelector(selected = ui.range, onSelect = onRangeChange)
            Spacer(Modifier.height(8.dp))
            HistoryGraph(
                points = ui.history,
                maxValue = 100.0,
                maxLabel = { "${it.roundToInt()} %" },
                colorForValue = { moistureColor(it) },
                showTimeAxis = true,
            )
        }
    }
}

@Composable
private fun RangeSelector(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimeRange.entries.forEach { r ->
            val active = r == selected
            Button(
                onClick = { onSelect(r) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(r.label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 540)
@Composable
private fun PlantGraphScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlantGraphContent(
                ui = PlantGraphUiState(previewPlants.first(), previewMoistureHistory, TimeRange.WEEK),
                onRangeChange = {},
                onBack = {},
            )
        }
    }
}
