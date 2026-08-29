package com.degree.homedash.plants

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.SoilMoistureDeviceUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class PlantsUiState(val plants: List<SoilMoistureDeviceUi>)

/**
 * Projects every soil-moisture sensor (entity id ending in [PlantEntities.SOIL_MOISTURE_SUFFIX]) into
 * [PlantsUiState].
 *
 * Unlike the other dashboards this one discovers its entities from live state rather than reading a
 * roster from `EntityMetadataRepo` — the repo's plant list is still incomplete, so switching over
 * would hide every sensor it doesn't name yet. It builds the same metadata on the fly and projects it
 * through the shared device path (see [loadSoilMoistureUis]).
 */
class PlantsViewModel(
    metadataRepo: EntityMetadataRepo,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<PlantsUiState> =
        metadataRepo.loadPlantsEntityMetadataList()
            .loadDeviceUis(deviceRepo)
            .map {
                PlantsUiState(
                    plants = it.filterIsInstance<SoilMoistureDeviceUi>()
                )
            }
            .distinctUntilChanged()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PlantsUiState(emptyList())
            )
}
