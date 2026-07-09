package com.degree.homedash.plants

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.degree.homedash.controls.EntityAction
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold

/** The Plants dashboard shows every entity whose id ends with this suffix. */
object PlantEntities {
    const val SOIL_MOISTURE_SUFFIX = "soil_moisture"
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
    DashboardScaffold(title = "Plants", onBack = onBack, onOpenSettings = onOpenSettings) {
        ControlGroup(
            title = "Soil Moisture",
            entities = ui.plants,
            onAction = { action ->
                if (action is EntityAction.OpenGraph) onOpenGraph(action.entityId)
            },
            empty = {
                Text(
                    "No moisture sensors found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
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
