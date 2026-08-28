package com.degree.homedash.office

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.degree.homedash.controls.PreviewKoin
import com.degree.homedash.controls.DeviceControl
import org.koin.compose.viewmodel.koinViewModel
import com.degree.homedash.ui.ControlGroup
import com.degree.homedash.ui.DashboardScaffold
import com.degree.homedash.ui.icons.RoomIcons

@Composable
fun OfficeScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val vm: OfficeViewModel = koinViewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    OfficeContent(
        ui = ui,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
    )
}

/**
 * Stateless Office UI — a projected [OfficeUiState] in, all actions out. Rendered by [OfficeScreen]
 * and previews. Lights, Fans, and Climate render as card grids; the remaining sections (Status,
 * Doors, Workstation) stay as rows.
 */
@Composable
fun OfficeContent(
    ui: OfficeUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    DashboardScaffold(
        title = "Office",
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        icon = RoomIcons.Desk,
    ) {
        ControlGroup(
            title = "Lights",
            devices = ui.lights,
        )

        ControlGroup(
            title = "Fans",
            devices = ui.fans,
        )

        ControlGroup(
            title = "Signal",
            devices = listOfNotNull(ui.signal),
        )

        ControlGroup(
            title = "Climate",
            devices = ui.climate,
        )

        ControlGroup(
            title = "Doors",
            devices = ui.doors,
        )

        ui.workstation?.let { workstation ->
            ControlGroup("Workstation", titleOutsideCard = true) {
                DeviceControl(workstation)
            }
        }
    }
}


@Preview(widthDp = 380, heightDp = 1700)
@Composable
private fun OfficeScreenPreview() = PreviewKoin {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            OfficeContent(
                ui = previewOfficeUiState,
                onBack = {},
                onOpenSettings = {},
            )
        }
    }
}
