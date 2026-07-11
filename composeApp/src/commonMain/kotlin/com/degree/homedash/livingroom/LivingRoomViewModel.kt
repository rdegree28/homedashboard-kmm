package com.degree.homedash.livingroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.ClimateKind
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.ui.formatNumber
import com.degree.homedash.ui.formatNumberOrSelf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.roundToInt

@Immutable
data class LivingRoomUiState(
    val lights: List<EntityUi.Light>,
    val fans: List<EntityUi.Fan>,
    val climate: List<EntityUi.Climate>,
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
                    climate = listOf(
                        states[LivingRoomEntities.TEMPERATURE].toClimate(LivingRoomEntities.TEMPERATURE, "Temperature", ClimateKind.Temperature),
                            dewPointClimate(states[LivingRoomEntities.TEMPERATURE], states[LivingRoomEntities.HUMIDITY]),
                    ),
                )
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LivingRoomUiState(emptyList(), emptyList(), emptyList()))

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

private fun EntityState?.toClimate(entityId: String, label: String, kind: ClimateKind): EntityUi.Climate {
    val unit = this?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        this == null || this.isUnavailable -> "—"
        else -> "${formatNumberOrSelf(state, decimals = 1)} $unit".trim()
    }
    return EntityUi.Climate(EntityMetadata.Climate(entityId, kind), label = label, valueText = value)
}

/**
 * Dew point derived from the temperature + relative-humidity sensors (Magnus–Tetens). Displayed in the
 * temperature sensor's own unit (°F/°C). Reads the humidity sensor as an input only — it isn't shown.
 * Shows "—" until both sensors report a usable numeric value.
 */
private fun dewPointClimate(tempState: EntityState?, humidityState: EntityState?): EntityUi.Climate {
    val tempUnit = tempState?.attrString("unit_of_measurement").orEmpty()
    val fahrenheit = tempUnit.contains("F", ignoreCase = true)
    val temp = tempState?.state?.toDoubleOrNull()?.takeUnless { tempState.isUnavailable }
    val rh = humidityState?.state?.toDoubleOrNull()?.takeUnless { humidityState.isUnavailable }

    val value = if (temp != null && rh != null && rh > 0.0) {
        val tempC = if (fahrenheit) (temp - 32.0) * 5.0 / 9.0 else temp
        val dewC = dewPointCelsius(tempC, rh)
        val dew = if (fahrenheit) dewC * 9.0 / 5.0 + 32.0 else dewC
        "${formatNumber(dew, decimals = 1)} $tempUnit".trim()
    } else {
        "—"
    }
    return EntityUi.Climate(
        EntityMetadata.Climate(LivingRoomEntities.HUMIDITY, ClimateKind.DewPoint),
        label = "Dew Point",
        valueText = value,
        subvalueText = if (rh != null) "${formatNumber(rh, decimals = 0)}%" else null,
    )
}

/** Magnus–Tetens dew point in °C from a Celsius temperature and relative humidity percentage (0–100). */
private fun dewPointCelsius(tempC: Double, rh: Double): Double {
    val a = 17.62
    val b = 243.12
    val gamma = ln(rh / 100.0) + a * tempC / (b + tempC)
    return b * gamma / (a - gamma)
}
