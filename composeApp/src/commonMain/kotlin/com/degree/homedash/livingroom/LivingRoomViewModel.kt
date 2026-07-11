package com.degree.homedash.livingroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Immutable
data class LivingRoomUiState(
    val lights: List<EntityUi.Light>,
    val fans: List<EntityUi.Fan>,
)

/** Projects the configured Living Room lights into [LivingRoomUiState]. */
class LivingRoomViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<LivingRoomUiState> =
        repo.states
            .map { states ->
                LivingRoomUiState(
                    lights = listOf(
                        states[LivingRoomEntities.LIVING_ROOM_LIGHT_WEST].toLight(LivingRoomEntities.LIVING_ROOM_LIGHT_WEST, "West"),
                        states[LivingRoomEntities.LIVING_ROOM_LIGHT_EAST].toLight(LivingRoomEntities.LIVING_ROOM_LIGHT_EAST, "East"),
                        states[LivingRoomEntities.HOMEWORK_LIGHT].toLight(LivingRoomEntities.HOMEWORK_LIGHT, "Homework"),
                        states[LivingRoomEntities.DINING_LIGHT].toLight(LivingRoomEntities.DINING_LIGHT, "Dining Ceiling"),
                        states[LivingRoomEntities.KITCHEN_STOVE_LIGHT].toLight(LivingRoomEntities.KITCHEN_STOVE_LIGHT, "Kitchen Stove"),
                    ),
                    fans = listOf(
                        states[LivingRoomEntities.LIVING_ROOM_FAN].toFan(LivingRoomEntities.LIVING_ROOM_FAN, "Fan"),
                        states[LivingRoomEntities.LIVING_ROOM_BOX_FAN].toFan(LivingRoomEntities.LIVING_ROOM_BOX_FAN, "Box Fan"),
                    ),
                )
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LivingRoomUiState(emptyList(), emptyList()))

    fun toggle(entityId: String) {
        viewModelScope.launch { repo.toggle(entityId) }
    }
}

private fun EntityState?.toLight(entityId: String, name: String) = EntityUi.Light(
    metadata = EntityMetadata.Light(entityId),
    name = name,
    isOn = this?.isOn == true,
    offline = this == null || this.isUnavailable,
)

private fun EntityState?.toFan(entityId: String, name: String): EntityUi.Fan {
    val stepPct = this?.attrDouble("percentage_step")
    val levelCount = if (stepPct != null && stepPct > 0.0) (100.0 / stepPct).roundToInt() else 0
    return EntityUi.Fan(
        metadata = EntityMetadata.Fan(entityId, levelCount),
        name = name,
        isOn = this?.isOn == true,
        offline = this == null || this.isUnavailable,
        percentage = this?.attrDouble("percentage")?.roundToInt() ?: 0,
    )
}
