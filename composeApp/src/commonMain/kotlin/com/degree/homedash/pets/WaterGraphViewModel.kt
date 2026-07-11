package com.degree.homedash.pets

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.plants.TimeRange
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class WaterGraphUiState(
    val item: EntityUi.WaterLevel?,
    val history: List<HistoryPoint>,
    val range: TimeRange,
)

/** Owns the selected [TimeRange] and re-fetches level history for [entityId] as it/connection change. */
class WaterGraphViewModel(
    private val repo: HomeAssistantRepo,
    private val entityId: String,
) : ViewModel() {

    private val range = MutableStateFlow(TimeRange.DAY)
    private val history = MutableStateFlow<List<HistoryPoint>>(emptyList())

    val uiState: StateFlow<WaterGraphUiState> =
        combine(repo.states, range, history) { states, range, history ->
            WaterGraphUiState(item = states[entityId]?.toWaterLevel(), history = history, range = range)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WaterGraphUiState(null, emptyList(), TimeRange.DAY))

    init {
        viewModelScope.launch {
            combine(repo.connection, range) { connection, range -> connection to range }
                .collect { (connection, range) ->
                    if (connection == ConnectionStatus.Connected) {
                        runCatching {
                            history.value = repo.powerHistory(entityId, hoursBack = range.hoursBack).dropTransientDips()
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
