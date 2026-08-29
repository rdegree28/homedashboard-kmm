package com.degree.homedash.office

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.ClimateDeviceUi
import com.degree.homedash.core.device.DeviceUi
import com.degree.homedash.core.device.DoorDeviceUi
import com.degree.homedash.core.device.FanDeviceUi
import com.degree.homedash.core.device.LightDeviceUi
import com.degree.homedash.core.device.OfficeSignalDeviceUi
import com.degree.homedash.core.device.OfficeWorkstationUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class OfficeUiState(
    val lights: List<LightDeviceUi>,
    val fans: List<FanDeviceUi>,
    val climate: List<ClimateDeviceUi>,
    val doors: List<DoorDeviceUi>,
    val signal: OfficeSignalDeviceUi?,
    val workstation: OfficeWorkstationUi?,
)

/**
 * Projects live Home Assistant state into an [OfficeUiState] and exposes the Office actions.
 * The flow is de-duplicated so only changes to *displayed* values recompose the screen.
 */
class OfficeViewModel(
    metadataRepo: EntityMetadataRepo,
    deviceRepo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<OfficeUiState> =
        metadataRepo.loadOfficeEntityMetadataList()
            .loadDeviceUis(deviceRepo)
            .map(::buildOfficeUiState)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EMPTY)

    private companion object {
        val EMPTY = buildOfficeUiState(emptyList())
    }
}

// --- Projection helpers ---

private fun buildOfficeUiState(deviceUis: List<DeviceUi>): OfficeUiState {
    return OfficeUiState(
        lights = deviceUis.filterIsInstance<LightDeviceUi>(),
        fans = deviceUis.filterIsInstance<FanDeviceUi>(),
        climate = deviceUis.filterIsInstance<ClimateDeviceUi>(),
        doors = deviceUis.filterIsInstance<DoorDeviceUi>(),
        signal = deviceUis.filterIsInstance<OfficeSignalDeviceUi>().firstOrNull(),
        workstation = deviceUis.filterIsInstance<OfficeWorkstationUi>().firstOrNull(),
    )
}
