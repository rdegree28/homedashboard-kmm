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
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// --- UI models: small immutable projections the Office composables render (no raw EntityState). ---

@Immutable
data class ToggleUi(val name: String, val isOn: Boolean, val offline: Boolean)

@Immutable
data class FanUi(
    val name: String,
    val isOn: Boolean,
    val offline: Boolean,
    val levelCount: Int,
    val percentage: Int,
    /** Whether the fan can oscillate at all — drives whether the toggle is offered. */
    val canOscillate: Boolean = false,
    val oscillating: Boolean = false,
    /** Whether the fan can mist at all — drives whether the toggle is offered. */
    val canMist: Boolean = false,
    val misting: Boolean = false,
)

@Immutable
data class SensorUi(val label: String, val valueText: String)

@Immutable
data class DoorUi(val label: String, val statusText: String, val open: Boolean, val unavailable: Boolean)

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
    deviceRepo: ExpHomeAssistantRepo,
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
