package com.degree.homedash.pets

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

@Composable
fun PetsScreen(
    repository: HomeAssistantRepo,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    val vm: PetsViewModel = viewModel { PetsViewModel(repository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    PetsContent(ui = ui, onBack = onBack, onOpenSettings = onOpenSettings, onOpenGraph = onOpenGraph)
}

/** Stateless Pets UI — projected sensor readings in, navigation actions out. */
@Composable
fun PetsContent(
    ui: PetsUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    DashboardScaffold(title = "Pets", onBack = onBack, onOpenSettings = onOpenSettings) {
        ControlGroup(
            title = "Cat Water Fountain",
            entities = ui.items,
            onAction = { action ->
                if (action is EntityAction.OpenGraph) onOpenGraph(action.entityId)
            },
            empty = {
                Text(
                    "No water level sensor found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun PetsScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PetsContent(
                ui = PetsUiState(previewLevels),
                onBack = {},
                onOpenSettings = {},
                onOpenGraph = {},
            )
        }
    }
}
