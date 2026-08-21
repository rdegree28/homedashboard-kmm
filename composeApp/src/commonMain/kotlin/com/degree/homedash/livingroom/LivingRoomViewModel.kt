package com.degree.homedash.livingroom

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.LightEntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.ui.dewPointText
import com.degree.homedash.ui.formatNumber
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class LivingRoomUiState(
    val triggers: List<EntityUi.Trigger>,
    val lights: List<LightEntityUi>,
    val fans: List<EntityUi.Fan>,
    val climate: List<EntityUi.Climate>,
)

/** Projects the configured Living Room lights into [LivingRoomUiState]. */
class LivingRoomViewModel(
    private val repo: HomeAssistantRepo,
    private val expRepo: ExpHomeAssistantRepo,
    metadataRepo: EntityMetadataRepo,
) : ViewModel() {

    /** The screen's roster; static, so it's read once rather than on every state push. */
    private val entities = metadataRepo.loadLivingRoomEntityMetadataList()

    val uiState: StateFlow<LivingRoomUiState> =
        combine(
            expRepo.loadEntityStatesForMetadata(entities),
            repo.states
        ) { expStateMap, states ->
            val uis = entities.toEntityUis(states)
            val expUis = expStateMap.toEntityUis()
            LivingRoomUiState(
                triggers = uis.filterIsInstance<EntityUi.Trigger>(),
                lights = expUis.filterIsInstance<LightEntityUi>(),
                fans = uis.filterIsInstance<EntityUi.Fan>(),
                climate = uis.filterIsInstance<EntityUi.Climate>().withDewPoint(states),
            )
        }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LivingRoomUiState(emptyList(), emptyList(), emptyList(), emptyList()),
        )


    fun toggle(entityId: String) {
        viewModelScope.launch { repo.toggle(entityId) }
    }

    fun setFanSpeed(entityId: String, percentage: Int) {
        viewModelScope.launch { repo.setFanPercentage(entityId, percentage) }
    }

    fun setOscillating(entityId: String, oscillating: Boolean) {
        viewModelScope.launch { repo.setFanOscillating(entityId, oscillating) }
    }

    /** [entityId] is the mister's own humidifier entity, not the fan's. */
    fun setMisting(entityId: String, misting: Boolean) {
        viewModelScope.launch { repo.setMisting(entityId, misting) }
    }

    /** Fires a trigger card's service call — activating a scene, running a script. */
    fun activate(call: TriggerEntityMetadata.ServiceCall) {
        viewModelScope.launch { repo.callService(call.domain, call.service, call.entityId) }
    }
}

/**
 * Fills in the dew point card, which needs both the temperature and humidity sensors and so can't come
 * from a single-entity projection — the roster's [ClimateMetadata.ClimateKind.DewPoint] entry arrives
 * holding the raw humidity reading, and this swaps in the computed value.
 */
private fun List<EntityUi.Climate>.withDewPoint(states: Map<String, EntityState>): List<EntityUi.Climate> = map { climate ->
    if (climate.metadata.kind == ClimateMetadata.ClimateKind.DewPoint) {
        dewPointClimate(climate.metadata, states[LivingRoomEntities.TEMPERATURE], states[LivingRoomEntities.HUMIDITY])
    } else {
        climate
    }
}

/**
 * Dew point derived from the temperature + relative-humidity sensors ([dewPointText], Magnus–Tetens),
 * shown as a standalone card in the temperature sensor's own unit (°F/°C) with the humidity reading as
 * a subvalue. Shows "—" until both sensors report a usable numeric value.
 */
private fun dewPointClimate(
    metadata: ClimateMetadata,
    tempState: EntityState?,
    humidityState: EntityState?,
): EntityUi.Climate {
    val rh = humidityState?.state?.toDoubleOrNull()?.takeUnless { humidityState.isUnavailable }
    return EntityUi.Climate(
        metadata = metadata,
        valueText = dewPointText(tempState, humidityState) ?: "—",
        subvalueText = if (rh != null) "${formatNumber(rh, decimals = 0)}%" else null,
    )
}
