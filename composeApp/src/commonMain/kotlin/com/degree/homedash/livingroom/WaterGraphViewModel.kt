package com.degree.homedash.livingroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val item: WaterLevelUi?,
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
            WaterGraphUiState(item = states[entityId]?.toLevelUi(), history = history, range = range)
        }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WaterGraphUiState(null, emptyList(), TimeRange.DAY))

    init {
        viewModelScope.launch {
            combine(repo.connection, range) { connection, range -> connection to range }
                .collect { (connection, range) ->
                    if (connection == ConnectionStatus.Connected) {
                        runCatching { history.value = repo.powerHistory(entityId, hoursBack = range.hoursBack) }
                    }
                }
        }
    }

    fun setRange(range: TimeRange) {
        this.range.value = range
    }
}
