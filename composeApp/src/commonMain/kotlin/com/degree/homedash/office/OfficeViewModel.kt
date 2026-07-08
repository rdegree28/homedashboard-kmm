package com.degree.homedash.office

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.ClimateKind
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import com.degree.homedash.ui.formatNumberOrSelf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
)

@Immutable
data class SensorUi(val label: String, val valueText: String)

@Immutable
data class DoorUi(val label: String, val statusText: String, val open: Boolean, val unavailable: Boolean)

@Immutable
data class OfficeUiState(
    val connection: ConnectionStatus,
    val officeLight: EntityUi.Light,
    val smallLight: EntityUi.Light,
    val officeFan: EntityUi.Fan,
    val boxFan: EntityUi.Fan,
    val activeSignal: String?,
    val temperature: EntityUi.Climate,
    val humidity: EntityUi.Climate,
    val door: EntityUi.Door,
    val workstation: ToggleUi,
    val hexagon: ToggleUi,
    val power: SensorUi,
    val energy: SensorUi,
    val powerHistory: List<HistoryPoint>,
)

/**
 * Projects live Home Assistant state into an [OfficeUiState] and exposes the Office actions.
 * The flow is de-duplicated so only changes to *displayed* values recompose the screen.
 */
class OfficeViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    private val powerHistory = MutableStateFlow<List<HistoryPoint>>(emptyList())

    val uiState: StateFlow<OfficeUiState> =
        combine(repo.states, repo.connection, powerHistory) { states, connection, history ->
            buildOfficeUiState(states, connection, history)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EMPTY)

    init {
        viewModelScope.launch {
            repo.connection.collect { status ->
                if (status == ConnectionStatus.Connected) {
                    runCatching {
                        powerHistory.value = repo.powerHistory(OfficeEntities.POWER, hoursBack = 168)
                    }
                }
            }
        }
    }

    fun toggle(entityId: String) {
        viewModelScope.launch { repo.toggle(entityId) }
    }

    fun setFanSpeed(entityId: String, percentage: Int) {
        viewModelScope.launch { repo.setFanPercentage(entityId, percentage) }
    }

    fun signal(mode: SignalMode) {
        viewModelScope.launch {
            when (mode) {
                SignalMode.OFF -> repo.turnOff(OfficeEntities.TRAFFIC_SIGNAL)
                SignalMode.AVAILABLE -> repo.runScript(OfficeEntities.SCRIPT_GREEN)
                SignalMode.FOCUSED -> repo.runScript(OfficeEntities.SCRIPT_AMBER)
                SignalMode.MEETING -> repo.runScript(OfficeEntities.SCRIPT_RED)
            }
        }
    }

    private companion object {
        val EMPTY = buildOfficeUiState(emptyMap(), ConnectionStatus.Disconnected, emptyList())
    }
}

// --- Projection helpers ---

private fun buildOfficeUiState(
    states: Map<String, EntityState>,
    connection: ConnectionStatus,
    powerHistory: List<HistoryPoint>,
) = OfficeUiState(
    connection = connection,
    officeLight = states[OfficeEntities.OFFICE_LIGHT].toLight(OfficeEntities.OFFICE_LIGHT, "Office"),
    smallLight = states[OfficeEntities.SMALL_LIGHT].toLight(OfficeEntities.SMALL_LIGHT, "Small"),
    officeFan = states[OfficeEntities.OFFICE_FAN].toFan(OfficeEntities.OFFICE_FAN, "Office Fan"),
    boxFan = states[OfficeEntities.BOX_FAN].toFan(OfficeEntities.BOX_FAN, "Box Fan"),
    activeSignal = states[OfficeEntities.SIGNAL_MODE]?.state,
    temperature = states[OfficeEntities.TEMPERATURE].toClimate(OfficeEntities.TEMPERATURE, "Temperature", ClimateKind.Temperature),
    humidity = states[OfficeEntities.HUMIDITY].toClimate(OfficeEntities.HUMIDITY, "Humidity", ClimateKind.Humidity),
    door = states[OfficeEntities.DOOR].toDoor(OfficeEntities.DOOR, "Office Door"),
    workstation = states[OfficeEntities.WORKSTATION].toToggleUi("Workstation"),
    hexagon = states[OfficeEntities.HEXAGON].toToggleUi("Hexagon Lights"),
    power = states[OfficeEntities.POWER].toSensorUi("Power", decimals = 2, dashWhenUnavailable = false),
    energy = states[OfficeEntities.ENERGY].toSensorUi("Total Power Used", decimals = 2, dashWhenUnavailable = false),
    powerHistory = powerHistory,
)

private fun EntityState?.toToggleUi(name: String) = ToggleUi(
    name = name,
    isOn = this?.isOn == true,
    offline = this == null || this.isUnavailable,
)

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

/** Formatted climate readout — always shows "—" when unavailable. */
private fun EntityState?.toClimate(entityId: String, label: String, kind: ClimateKind): EntityUi.Climate {
    val unit = this?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        this == null || this.isUnavailable -> "—"
        else -> "${formatNumberOrSelf(state, decimals = 1)} $unit".trim()
    }
    return EntityUi.Climate(EntityMetadata.Climate(entityId, kind), label = label, valueText = value)
}

/** Formatted sensor readout. [dashWhenUnavailable] shows "—" for unavailable states. */
private fun EntityState?.toSensorUi(label: String, decimals: Int, dashWhenUnavailable: Boolean): SensorUi {
    val unit = this?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        this == null -> "—"
        dashWhenUnavailable && this.isUnavailable -> "—"
        else -> "${formatNumberOrSelf(state, decimals)} $unit".trim()
    }
    return SensorUi(label, value)
}

private fun EntityState?.toDoor(entityId: String, label: String): EntityUi.Door {
    val unavailable = this == null || this.isUnavailable
    val open = this?.state == "on" // device_class opening: on = open
    val status = when {
        unavailable -> "—"
        open -> "Open"
        else -> "Closed"
    }
    return EntityUi.Door(EntityMetadata.Door(entityId), label = label, statusText = status, open = open, unavailable = unavailable)
}
