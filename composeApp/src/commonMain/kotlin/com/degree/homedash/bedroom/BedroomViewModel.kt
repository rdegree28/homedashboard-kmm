package com.degree.homedash.bedroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.shared.model.entity.TriggerEntityMetadata
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class BedroomUiState(
    val triggers: List<EntityUi.Trigger>,
    val lights: List<EntityUi.Light>,
    val fans: List<EntityUi.Fan>,
    val climate: List<EntityUi.Climate>,
)

/** Projects the configured Bedroom entities into [BedroomUiState]. */
class BedroomViewModel(
    private val repo: HomeAssistantRepo,
    metadataRepo: EntityMetadataRepo,
) : ViewModel() {

    /** The screen's roster; static, so it's read once rather than on every state push. */
    private val entities = metadataRepo.loadBedroomEntityMetadataList()

    val uiState: StateFlow<BedroomUiState> =
        repo.states
            .map { states ->
                val uis = entities.toEntityUis(states)
                BedroomUiState(
                    triggers = uis.filterIsInstance<EntityUi.Trigger>(),
                    lights = uis.filterIsInstance<EntityUi.Light>(),
                    fans = uis.filterIsInstance<EntityUi.Fan>(),
                    climate = uis.filterIsInstance<EntityUi.Climate>(),
                )
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EMPTY)

    fun toggle(entityId: String) {
        viewModelScope.launch { repo.toggle(entityId) }
    }

    fun setFanSpeed(entityId: String, percentage: Int) {
        viewModelScope.launch { repo.setFanPercentage(entityId, percentage) }
    }

    fun setOscillating(entityId: String, oscillating: Boolean) {
        viewModelScope.launch { repo.setFanOscillating(entityId, oscillating) }
    }

    /** Fires a trigger card's service call — running a script, activating a scene. */
    fun activate(call: TriggerEntityMetadata.ServiceCall) {
        viewModelScope.launch { repo.callService(call.domain, call.service, call.entityId) }
    }

    private companion object {
        val EMPTY = BedroomUiState(emptyList(), emptyList(), emptyList(), emptyList())
    }
}
