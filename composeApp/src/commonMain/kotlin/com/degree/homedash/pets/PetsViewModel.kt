package com.degree.homedash.pets

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.PetFountainDeviceUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class PetsUiState(
    val items: List<PetFountainDeviceUi>
)

/** Projects the configured Pets sensors (the cat water fountain) into [PetsUiState]. */
class PetsViewModel(
    metadataRepo: EntityMetadataRepo,
    deviceRepo: HomeAssistantRepo,
) : ViewModel() {

    val uiState: StateFlow<PetsUiState> =
        metadataRepo.loadPetsEntityMetadataList()
            .loadDeviceUis(deviceRepo)
            .map { devices -> PetsUiState(items = devices.filterIsInstance<PetFountainDeviceUi>()) }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetsUiState(emptyList()))
}
