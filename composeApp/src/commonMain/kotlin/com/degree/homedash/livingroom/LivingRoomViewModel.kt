package com.degree.homedash.livingroom

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
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class LivingRoomUiState(
    val triggers: List<TriggerDeviceUi>,
    val lights: List<LightDeviceUi>,
    val fans: List<FanDeviceUi>,
    val climate: List<ClimateDeviceUi>,
)

/** Projects the configured Living Room devices into [LivingRoomUiState]. */
class LivingRoomViewModel(
    metadataRepo: EntityMetadataRepo,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<LivingRoomUiState> =
        metadataRepo.loadLivingRoomEntityMetadataList()
            .loadDeviceUis(deviceRepo)
            .map(::buildUiState)
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildUiState(emptyList()))
}

private fun buildUiState(deviceUis: List<DeviceUi>) = LivingRoomUiState(
    triggers = deviceUis.filterIsInstance<TriggerDeviceUi>(),
    lights = deviceUis.filterIsInstance<LightDeviceUi>(),
    fans = deviceUis.filterIsInstance<FanDeviceUi>(),
    climate = deviceUis.filterIsInstance<ClimateDeviceUi>(),
)
