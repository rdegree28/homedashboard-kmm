package com.degree.homedash.office

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.ui.dewPointText
import com.degree.homedash.ui.readingText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val connection: HaConnectionStatus,
    val lights: List<EntityUi.Light>,
    val fans: List<EntityUi.Fan>,
    val climate: List<EntityUi.Climate>,
    val doors: List<EntityUi.Door>,
    val activeSignal: String?,
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
    metadataRepo: EntityMetadataRepo = EntityMetadataRepo(),
) : ViewModel() {

    private val powerHistory = MutableStateFlow<List<HistoryPoint>>(emptyList())

    /** The screen's roster; static, so it's read once rather than on every state push. */
    private val entities = metadataRepo.loadOfficeEntityMetadataList()

    val uiState: StateFlow<OfficeUiState> =
        combine(repo.states, repo.connection, powerHistory) { states, connection, history ->
            buildOfficeUiState(entities, states, connection, history)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EMPTY)

    init {
        viewModelScope.launch {
            repo.connection.collect { status ->
                if (status == HaConnectionStatus.Connected) {
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
        val EMPTY = buildOfficeUiState(emptyList(), emptyMap(), HaConnectionStatus.Disconnected, emptyList())
    }
}

// --- Projection helpers ---

private fun buildOfficeUiState(
    entities: List<EntityMetadata>,
    states: Map<String, EntityState>,
    connection: HaConnectionStatus,
    powerHistory: List<HistoryPoint>,
): OfficeUiState {
    val uis = entities.toEntityUis(states)
    return OfficeUiState(
        connection = connection,
        lights = uis.filterIsInstance<EntityUi.Light>(),
        fans = uis.filterIsInstance<EntityUi.Fan>(),
        climate = uis.filterIsInstance<EntityUi.Climate>().withDewPointSubvalue(states),
        doors = uis.filterIsInstance<EntityUi.Door>(),
        activeSignal = states[OfficeEntities.SIGNAL_MODE]?.state,
        // The remaining Office controls have no EntityMetadata type, so they stay hand-wired.
        workstation = states[OfficeEntities.WORKSTATION].toToggleUi("Workstation"),
        hexagon = states[OfficeEntities.HEXAGON].toToggleUi("Hexagon Lights"),
        power = states[OfficeEntities.POWER].toSensorUi("Power", decimals = 2, dashWhenUnavailable = false),
        energy = states[OfficeEntities.ENERGY].toSensorUi("Total Power Used", decimals = 2, dashWhenUnavailable = false),
        powerHistory = powerHistory,
    )
}

/**
 * Office shows dew point as a subvalue under the humidity reading (rather than as its own card, the
 * way the Living Room does) — it needs both sensors, so it can't come from a single-entity projection.
 */
private fun List<EntityUi.Climate>.withDewPointSubvalue(states: Map<String, EntityState>): List<EntityUi.Climate> {
    val dewPoint = dewPointText(states[OfficeEntities.TEMPERATURE], states[OfficeEntities.HUMIDITY]) ?: return this
    return map { climate ->
        if (climate.metadata.kind == ClimateMetadata.ClimateKind.Humidity) {
            climate.copy(subvalueText = "Dew pt $dewPoint")
        } else {
            climate
        }
    }
}

private fun EntityState?.toToggleUi(name: String) = ToggleUi(
    name = name,
    isOn = this?.isOn == true,
    offline = this == null || this.isUnavailable,
)

/** Formatted sensor readout. [dashWhenUnavailable] shows "—" for unavailable states. */
private fun EntityState?.toSensorUi(label: String, decimals: Int, dashWhenUnavailable: Boolean): SensorUi =
    SensorUi(label, readingText(decimals, dashWhenUnavailable))
