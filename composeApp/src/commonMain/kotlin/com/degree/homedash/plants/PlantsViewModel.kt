package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.plantName
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.ui.formatNumber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class PlantUi(val entityId: String, val name: String, val pct: Double?, val valueText: String)

@Immutable
data class PlantsUiState(val plants: List<PlantUi>)

/** Projects the configured soil-moisture sensors into [PlantsUiState]. */
class PlantsViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<PlantsUiState> =
        repo.states
            .map { states -> PlantsUiState(PlantEntities.SOIL_MOISTURE.mapNotNull { states[it]?.toPlantUi() }) }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantsUiState(emptyList()))
}

/** Projects a soil-moisture entity into its UI model (shared by the list and the graph screen). */
internal fun EntityState.toPlantUi(): PlantUi {
    val pct = state.toDoubleOrNull()?.takeUnless { isUnavailable }
    return PlantUi(
        entityId = entityId,
        name = plantName(this),
        pct = pct,
        valueText = pct?.let { "${formatNumber(it, decimals = 1)} %" } ?: "—",
    )
}
