package com.degree.homedash.pets

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.ui.formatNumber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class PetsUiState(
    val items: List<EntityUi.WaterLevel>
)

/** Projects the configured Pets sensors (the cat water fountain) into [PetsUiState]. */
class PetsViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<PetsUiState> =
        repo.states
            .map { states ->
                PetsUiState(listOfNotNull(states[PetsEntities.CAT_WATER_LEVEL]?.toWaterLevel()))
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetsUiState(emptyList()))
}

/** Projects a percentage-level entity into its UI model (shared by the list and the graph screen). */
internal fun EntityState.toWaterLevel(): EntityUi.WaterLevel {
    val pct = state.toDoubleOrNull()?.takeUnless { isUnavailable }
    return EntityUi.WaterLevel(
        metadata = EntityMetadata.WaterLevel(entityId),
        name = feederName(this),
        pct = pct,
        valueText = pct?.let { "${formatNumber(it, decimals = 0)} %" } ?: "—",
    )
}

/** Friendly name with a trailing level/percentage qualifier stripped for a clean title. */
internal fun feederName(entity: EntityState): String {
    val raw = entity.friendlyName ?: entity.entityId.substringAfter('.').replace('_', ' ')
    return "Remaining Water"
}
