package com.degree.homedash.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.DashboardHeader
import com.degree.homedash.ui.Dimens
import com.degree.homedash.ui.SectionCard

/** Soil-moisture sensor entity ids shown on the Plants dashboard, in display order. */
object PlantEntities {
    val SOIL_MOISTURE = listOf(
        "sensor.louie_moisture_sensor_soil_moisture",
    )
}

@Composable
fun PlantsScreen(
    repository: HomeAssistantRepo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    val vm: PlantsViewModel = viewModel { PlantsViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    PlantsContent(ui = ui, onBack = onBack, onOpenSettings = onOpenSettings, onOpenGraph = onOpenGraph)
}

/** Stateless Plants UI — projected soil-moisture readings in, navigation actions out. */
@Composable
fun PlantsContent(
    ui: PlantsUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
    ) {
        DashboardHeader("Plants", onBack = onBack, onOpenSettings = onOpenSettings)

        SectionCard("Soil Moisture") {
            if (ui.plants.isEmpty()) {
                Text(
                    "No moisture sensors found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ui.plants.forEach { plant ->
                    SoilMoistureControl(plant, onClick = { onOpenGraph(plant.entityId) })
                }
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 600)
@Composable
private fun PlantsScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlantsContent(ui = PlantsUiState(previewPlants), onBack = {}, onOpenSettings = {}, onOpenGraph = {})
        }
    }
}
