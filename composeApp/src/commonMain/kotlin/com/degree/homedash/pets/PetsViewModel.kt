package com.degree.homedash.pets

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
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
    metadataRepo: EntityMetadataRepo = EntityMetadataRepo(),
) : ViewModel() {

    /** The screen's roster; static, so it's read once rather than on every state push. */
    private val entities = metadataRepo.loadPetsEntityMetadataList()

    val uiState: StateFlow<PetsUiState> =
        repo.states
            .map { states -> PetsUiState(entities.toEntityUis(states).filterIsInstance<EntityUi.WaterLevel>()) }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetsUiState(emptyList()))
}
