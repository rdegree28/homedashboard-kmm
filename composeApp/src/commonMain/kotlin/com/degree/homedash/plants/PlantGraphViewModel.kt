package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.SoilMoistureDeviceUi
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.api.HaConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class PlantGraphUiState(
    val plant: SoilMoistureDeviceUi?,
    val history: List<HistoryPoint>,
    val range: TimeRange,
)

/**
 * Owns the selected [TimeRange] and re-fetches moisture history for [entityId] as it/connection change.
 *
 * Two repos, deliberately: the reading at the top of the screen is a device, projected through
 * [ExpHomeAssistantRepo] like every other card, while the chart's history still comes from
 * [HomeAssistantRepo] — its ranges reach a year back, where the recorder's raw states are long purged
 * and only that repo's long-term-statistics path returns anything.
 */
class PlantGraphViewModel(
    private val repo: HomeAssistantRepo,
    private val entityId: String,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    private val range = MutableStateFlow(TimeRange.WEEK)
    private val history = MutableStateFlow<List<HistoryPoint>>(emptyList())

    private val plant = deviceRepo.loadSoilMoistureUis { it.entityId == entityId }

    val uiState: StateFlow<PlantGraphUiState> =
        combine(plant, range, history) { plant, range, history ->
            PlantGraphUiState(plant = plant.firstOrNull(), history = history, range = range)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantGraphUiState(null, emptyList(), TimeRange.WEEK))

    init {
        viewModelScope.launch {
            combine(repo.connection, range) { connection, range -> connection to range }
                .collect { (connection, range) ->
                    if (connection == HaConnectionStatus.Connected) {
                        runCatching { history.value = repo.history(entityId, hoursBack = range.hoursBack) }
                    }
                }
        }
    }

    fun setRange(range: TimeRange) {
        this.range.value = range
    }
}
