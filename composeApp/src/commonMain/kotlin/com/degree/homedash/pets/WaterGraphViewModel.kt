package com.degree.homedash.pets

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.PetFountainDeviceUi
import com.degree.homedash.plants.TimeRange
import com.degree.homedash.shared.model.entity.PetFountainMetadata
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.api.HaConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class WaterGraphUiState(
    val item: PetFountainDeviceUi?,
    val history: List<HistoryPoint>,
    val range: TimeRange,
)

/**
 * Owns the selected [TimeRange] and re-fetches level history for [entityId] as it/connection change.
 *
 * Two repos, deliberately: the reading at the top of the screen is a device, projected through
 * [ExpHomeAssistantRepo] like every other card, while the chart's history still comes from
 * [HomeAssistantRepo] — its ranges reach a year back, where the recorder's raw states are long purged
 * and only that repo's long-term-statistics path returns anything.
 */
class WaterGraphViewModel(
    private val repo: HomeAssistantRepo,
    private val entityId: String,
    metadataRepo: EntityMetadataRepo,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    /** The graphed entity's descriptor, so this screen's title matches the Pets list it was opened from. */
    private val metadata: PetFountainMetadata =
        metadataRepo.loadPetsEntityMetadataList()
            .filterIsInstance<PetFountainMetadata>()
            .firstOrNull { it.entityId == entityId }
            ?: PetFountainMetadata(entityId, "Water Level")

    private val range = MutableStateFlow(TimeRange.DAY)
    private val history = MutableStateFlow<List<HistoryPoint>>(emptyList())

    /** The fountain itself, projected through its own metadata the way the Pets list renders it. */
    private val item = metadata.loadState(deviceRepo)
        .map { state -> PetFountainDeviceUi(metadata = metadata, state = state) }

    val uiState: StateFlow<WaterGraphUiState> =
        combine(item, range, history) { item, range, history ->
            WaterGraphUiState(item = item, history = history, range = range)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WaterGraphUiState(null, emptyList(), TimeRange.DAY))

    init {
        viewModelScope.launch {
            combine(repo.connection, range) { connection, range -> connection to range }
                .collect { (connection, range) ->
                    if (connection == HaConnectionStatus.Connected) {
                        runCatching {
                            history.value = repo.history(entityId, hoursBack = range.hoursBack).dropTransientDips()
                        }
                    }
                }
        }
    }

    fun setRange(range: TimeRange) {
        this.range.value = range
    }
}

/**
 * Drops single-sample downward spikes — transient sensor glitches (e.g. a momentary read of ~0)
 * that dip far below *both* neighbors and immediately recover. Endpoints, gradual drains, genuine
 * approaches to empty, and refills (upward jumps) are all preserved.
 *
 * NOTE / known limitation: only catches *single-sample* spikes. A run of two-or-more consecutive
 * glitch readings would survive (each has a glitched neighbor, so neither dips below *both*). The
 * observed fountain glitches are isolated drops-to-0, so this is sufficient for now. If glitches
 * start slipping through, either lower [threshold] or widen the neighbor comparison to look past
 * adjacent spikes. Keeping an eye on the live data to see if tuning is needed.
 */
internal fun List<HistoryPoint>.dropTransientDips(threshold: Double = 25.0): List<HistoryPoint> {
    if (size < 3) return this
    return filterIndexed { i, p ->
        if (i == 0 || i == lastIndex) return@filterIndexed true
        val prev = this[i - 1].value
        val next = this[i + 1].value
        !(p.value < prev - threshold && p.value < next - threshold)
    }
}
