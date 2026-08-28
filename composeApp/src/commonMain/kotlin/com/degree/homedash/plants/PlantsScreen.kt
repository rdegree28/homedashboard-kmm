package com.degree.homedash.plants

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.controls.SoilMoistureControl
import org.koin.compose.viewmodel.koinViewModel
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons

/** The Plants dashboard shows every entity whose id ends with this suffix. */
object PlantEntities {
    const val SOIL_MOISTURE_SUFFIX = "soil_moisture"
}

@Composable
fun PlantsScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    val vm: PlantsViewModel = koinViewModel()
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
    DashboardScaffold(title = "Plants", onBack = onBack, onOpenSettings = onOpenSettings, icon = RoomIcons.Plant) {
        ControlGroup(title = "Soil Moisture", titleOutsideCard = true) {
            if (ui.plants.isEmpty()) {
                Text(
                    "No moisture sensors found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Rendered here rather than through `DeviceControl` because the tap is navigation:
                // the row opens this sensor's history graph, which no device can do for itself.
                ui.plants.forEach { plant ->
                    SoilMoistureControl(
                        ui = plant,
                        onClick = { onOpenGraph(plant.id) }
                    )
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
