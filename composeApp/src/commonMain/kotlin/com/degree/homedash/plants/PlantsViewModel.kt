package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
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
data class PlantsUiState(val plants: List<EntityUi.SoilMoisture>)

/** Projects every soil-moisture sensor (entity id ending in [PlantEntities.SOIL_MOISTURE_SUFFIX]) into [PlantsUiState]. */
class PlantsViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<PlantsUiState> =
        repo.states
            .map { states ->
                PlantsUiState(
                    states.values
                        .filter { it.entityId.endsWith(PlantEntities.SOIL_MOISTURE_SUFFIX) }
                        .map { it.toSoilMoisture() }
                        .sortedBy { it.name },
                )
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantsUiState(emptyList()))
}

/** Projects a soil-moisture entity into its UI model (shared by the list and the graph screen). */
internal fun EntityState.toSoilMoisture(): EntityUi.SoilMoisture {
    val pct = state.toDoubleOrNull()?.takeUnless { isUnavailable }
    return EntityUi.SoilMoisture(
        metadata = EntityMetadata.SoilMoisture(entityId),
        name = plantName(this),
        pct = pct,
        valueText = pct?.let { "${formatNumber(it, decimals = 1)} %" } ?: "—",
    )
}
