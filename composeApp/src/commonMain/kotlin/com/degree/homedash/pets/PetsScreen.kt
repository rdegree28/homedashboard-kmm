package com.degree.homedash.pets

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.core.control.PetFilterHealthControl
import com.degree.homedash.core.control.WaterLevelControl
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PetsScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    val vm: PetsViewModel = koinViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    PetsContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onOpenGraph = onOpenGraph
    )
}

/** Stateless Pets UI — projected sensor readings in, navigation actions out. */
@Composable
fun PetsContent(
    ui: PetsUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGraph: (String) -> Unit,
) {
    DashboardScaffold(title = "Pets", onBack = onBack, onOpenSettings = onOpenSettings, icon = RoomIcons.Paw) {
        // The content overload rather than the entities one, because a fountain contributes two rows
        // — its water level and its filter — and only the first of them opens a graph.
        ControlGroup(title = "Cat Water Fountain", titleOutsideCard = true) {
            if (ui.items.isEmpty()) {
                Text(
                    "No water level sensor found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ui.items.forEach { item ->
                WaterLevelControl(item, onClick = { onOpenGraph(item.id) })
                // Absent on a fountain that declares no filter, or whose sensor has gone quiet.
                if (item.filterDaysRemaining != null) PetFilterHealthControl(item)
            }
        }
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

/** Both readings near their limits — the amber filter band is only a few days wide in real life. */
@Preview(widthDp = 380, heightDp = 400)
@Composable
private fun PetsScreenLowPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PetsContent(
                ui = PetsUiState(previewLevelsLow),
                onBack = {},
                onOpenSettings = {},
                onOpenGraph = {},
            )
        }
    }
}
