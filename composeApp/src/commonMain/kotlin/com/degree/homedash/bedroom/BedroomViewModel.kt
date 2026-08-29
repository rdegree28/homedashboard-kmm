package com.degree.homedash.bedroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.ClimateDeviceUi
import com.degree.homedash.core.device.DeviceUi
import com.degree.homedash.core.device.FanDeviceUi
import com.degree.homedash.core.device.LightDeviceUi
import com.degree.homedash.core.device.TriggerDeviceUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class BedroomUiState(
    val triggers: List<TriggerDeviceUi>,
    val lights: List<LightDeviceUi>,
    val fans: List<FanDeviceUi>,
    val climate: List<ClimateDeviceUi>,
)

/** Projects the configured Bedroom devices into [BedroomUiState]. */
class BedroomViewModel(
    metadataRepo: EntityMetadataRepo,
    deviceRepo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<BedroomUiState> =
        metadataRepo.loadBedroomEntityMetadataList()
            .loadDeviceUis(deviceRepo)
            .map(::buildUiState)
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                buildUiState(emptyList())
            )
}

private fun buildUiState(deviceUis: List<DeviceUi>) = BedroomUiState(
    triggers = deviceUis.filterIsInstance<TriggerDeviceUi>(),
    lights = deviceUis.filterIsInstance<LightDeviceUi>(),
    fans = deviceUis.filterIsInstance<FanDeviceUi>(),
    climate = deviceUis.filterIsInstance<ClimateDeviceUi>(),
)
