package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.SoilMoistureDeviceUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
 * [entityId] arrives as the destination's navigation argument — the row the user tapped on the Plants
 * list — and is looked up in the same roster that list renders, so this screen shows the sensor under
 * the name and metadata the rest of the app uses rather than a second description of it.
 */
class PlantGraphViewModel(
    private val entityId: String,
    metadataRepo: EntityMetadataRepo,
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    private val range = MutableStateFlow(TimeRange.WEEK)
    private val history = MutableStateFlow<List<HistoryPoint>>(emptyList())

    /**
     * The one rostered sensor this screen was opened for. Projected as a one-entry roster so it goes
     * through the same path the list does; an id the roster doesn't name yields no device, and the
     * screen renders its chart with no reading above it rather than failing.
     */
    private val plant: Flow<SoilMoistureDeviceUi?> =
        metadataRepo.loadPlantsEntityMetadataList()
            .filter { it.entityId == entityId }
            .loadDeviceUis(repo)
            .map { devices -> devices.filterIsInstance<SoilMoistureDeviceUi>().firstOrNull() }

    val uiState: StateFlow<PlantGraphUiState> =
        combine(plant, range, history) { plant, range, history ->
            PlantGraphUiState(plant = plant, history = history, range = range)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantGraphUiState(null, emptyList(), TimeRange.WEEK))

    init {
        viewModelScope.launch {
            combine(repo.connection, range) { connection, range -> connection to range }
                .collect { (connection, range) ->
                    if (connection == HaConnectionStatus.Connected) {
                        runCatching { history.value = repo.getHistoryForEntity(entityId, hoursBack = range.hoursBack) }
                    }
                }
        }
    }

    fun setRange(range: TimeRange) {
        this.range.value = range
    }
}
