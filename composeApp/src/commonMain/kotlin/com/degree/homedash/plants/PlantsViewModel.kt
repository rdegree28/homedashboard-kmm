package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.displayName
import com.degree.homedash.controls.plantName
import com.degree.homedash.controls.toEntityUi
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class PlantsUiState(val plants: List<EntityUi.SoilMoisture>)

/**
 * Projects every soil-moisture sensor (entity id ending in [PlantEntities.SOIL_MOISTURE_SUFFIX]) into
 * [PlantsUiState].
 *
 * Unlike the other dashboards this one discovers its entities from live state rather than reading a
 * roster from `EntityMetadataRepo` — the repo's plant list is still incomplete, so switching over
 * would hide every sensor it doesn't name yet. It builds the same metadata on the fly and projects
 * through the shared [toEntityUi] path.
 */
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
                        .sortedBy { it.displayName },
                )
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantsUiState(emptyList()))
}

/** Projects a discovered soil-moisture entity into its UI model (shared by the list and the graph screen). */
internal fun EntityState.toSoilMoisture(): EntityUi.SoilMoisture =
    SoilMoistureMetadata(entityId, plantName(this)).toEntityUi(this) as EntityUi.SoilMoisture
