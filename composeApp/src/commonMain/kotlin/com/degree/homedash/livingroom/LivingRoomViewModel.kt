package com.degree.homedash.livingroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.ClimateDeviceUi
import com.degree.homedash.controls.DeviceUi
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.FanDeviceUi
import com.degree.homedash.controls.LightDeviceUi
import com.degree.homedash.controls.loadDeviceUis
import com.degree.homedash.shared.model.entity.TriggerDeviceMetadata
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class LivingRoomUiState(
    val triggers: List<EntityUi.Trigger>,
    val lights: List<LightDeviceUi>,
    val fans: List<FanDeviceUi>,
    val climate: List<ClimateDeviceUi>,
)

/** Projects the configured Living Room devices into [LivingRoomUiState]. */
class LivingRoomViewModel(
    private val repo: HomeAssistantRepo,
    metadataRepo: EntityMetadataRepo,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    /** The screen's roster; static, so it's read once rather than on every state push. */
    private val entities = metadataRepo.loadLivingRoomEntityMetadataList()

    /**
     * The scene cards. Static: a trigger fires a service and reports nothing, so unlike the devices
     * below there is no state to project — they are built once and never change.
     */
    private val triggers: List<EntityUi.Trigger> =
        entities.filterIsInstance<TriggerDeviceMetadata>().map(EntityUi::Trigger)

    val uiState: StateFlow<LivingRoomUiState> =
        entities.loadDeviceUis(deviceRepo)
            .map { deviceUis -> buildUiState(triggers, deviceUis) }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildUiState(triggers, emptyList()))

    /** Fires a trigger card's service call — activating a scene, running a script. */
    fun activate(call: TriggerDeviceMetadata.ServiceCall) {
        viewModelScope.launch { repo.callService(call.domain, call.service, call.entityId) }
    }
}

private fun buildUiState(
    triggers: List<EntityUi.Trigger>,
    deviceUis: List<DeviceUi>,
) = LivingRoomUiState(
    triggers = triggers,
    lights = deviceUis.filterIsInstance<LightDeviceUi>(),
    fans = deviceUis.filterIsInstance<FanDeviceUi>(),
    climate = deviceUis.filterIsInstance<ClimateDeviceUi>(),
)
