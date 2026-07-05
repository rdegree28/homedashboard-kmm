package com.degree.homedash.livingroom

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
import com.degree.homedash.controls.WaterLevelControl
import com.degree.homedash.controls.waterLevelColor
import com.degree.homedash.plants.TimeRange
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.HistoryGraph
import com.degree.homedash.ui.SectionCard
import kotlin.math.roundToInt

@Composable
fun WaterGraphScreen(repository: HomeAssistantRepo, entityId: String, onBack: () -> Unit) {
    val vm: WaterGraphViewModel = viewModel(key = entityId) { WaterGraphViewModel(repository, entityId) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    WaterGraphContent(ui = ui, onRangeChange = vm::setRange, onBack = onBack)
}

/** Stateless water-level graph view: current reading, a range selector, and the history chart. */
@Composable
fun WaterGraphContent(
    ui: WaterGraphUiState,
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
        DashboardHeader(ui.item?.name ?: "Water Level", onBack = onBack)

        SectionCard("Water Level") {
            ui.item?.let { WaterLevelControl(it) }
            Spacer(Modifier.height(8.dp))
            RangeSelector(selected = ui.range, onSelect = onRangeChange)
            Spacer(Modifier.height(8.dp))
            HistoryGraph(
                points = ui.history,
                maxValue = 100.0,
                maxLabel = { "${it.roundToInt()} %" },
                colorForValue = { waterLevelColor(it) },
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
private fun WaterGraphScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            WaterGraphContent(
                ui = WaterGraphUiState(previewLevels.first(), previewLevelHistory, TimeRange.DAY),
                onRangeChange = {},
                onBack = {},
            )
        }
    }
}
