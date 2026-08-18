package com.degree.homedash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.pets.PetsEntities
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.HvacMode
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Severity of a [HomeWarning] — drives the warning card's color, listed lowest urgency first.
 *
 * [Notification] is blue: something wants doing, but nothing is wrong. [Warning] (amber) is a
 * heading-the-wrong-way nudge, and [Critical] (red) means act now.
 */
enum class WarningSeverity { Notification, Warning, Critical }

/** An at-a-glance warning shown at the top of the Home launcher. */
data class HomeWarning(val message: String, val severity: WarningSeverity)

/**
 * Backs the Home launcher: the dashboard cards it offers, plus any warnings drawn from live state
 * (the cat water fountain level and her medication reminders).
 */
class HomeViewModel(
    private val repo: HomeAssistantRepo,
    metadataRepo: EntityMetadataRepo,
) : ViewModel() {

    private val petEntities = metadataRepo.loadPetsEntityMetadataList()
    private val thermostatEntities = metadataRepo.loadHomeThermostatMetadataList()

    /**
     * The whole-house thermostat, shown above the launcher cards. Its own flow rather than a field on
     * a combined UI state: it and [warnings] are projected from unrelated entities and neither has any
     * bearing on the other, so a shared state object would only make each recompose for the other's
     * changes.
     */
    val thermostats: StateFlow<List<EntityUi.Thermostat>> =
        repo.states
            .map { states ->
                thermostatEntities.toEntityUis(states).filterIsInstance<EntityUi.Thermostat>()
            }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * The launcher cards. A plain `val`, not a flow — nothing about them depends on live state, so
     * projecting against an empty state map is the whole story. Feature-flag filtering happens at the
     * screen, which is where the flag set lives.
     */
    val navigation: List<EntityUi.Navigation> =
        metadataRepo.loadHomeScreenMetadataList()
            .toEntityUis(emptyMap())
            .filterIsInstance<EntityUi.Navigation>()

    val warnings: StateFlow<List<HomeWarning>> =
        repo.states
            .map { states ->
                petEntities.toEntityUis(states)
                    .filterIsInstance<EntityUi.WaterLevel>()
                    .mapNotNull(::catWaterWarning) +
                    listOfNotNull(catMedicationWarning(states))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setTargetTemperature(entityId: String, temperature: Double) {
        viewModelScope.launch { repo.setTargetTemperature(entityId, temperature) }
    }

    fun setHvacMode(entityId: String, mode: HvacMode) {
        viewModelScope.launch { repo.setHvacMode(entityId, mode) }
    }

    /** [mode] is one of the thermostat's own `fan_modes`, not a `fan.*` entity speed. */
    fun setThermostatFanMode(entityId: String, mode: String) {
        viewModelScope.launch { repo.setThermostatFanMode(entityId, mode) }
    }

    fun setPresetMode(entityId: String, mode: String) {
        viewModelScope.launch { repo.setPresetMode(entityId, mode) }
    }

    /** [entityId] is the `input_boolean.*` helper, not the thermostat. */
    fun setExtremeTemperatures(entityId: String, extreme: Boolean) {
        viewModelScope.launch { repo.setExtremeTemperatures(entityId, extreme) }
    }
}

/**
 * Warn while either of the cat's medication reminders is active, as one card naming whichever doses
 * are still owed.
 *
 * These are read straight from [states] rather than projected through [EntityUi] like the fountain
 * above: they're plain `input_boolean` helpers with nothing to render — no card, no control, no
 * metadata type — and the launcher only ever asks whether they're on.
 *
 * Blue, not amber: a due dose is a standing to-do rather than something going wrong, so it sits
 * below the fountain's warnings on the urgency scale. Home Assistant clears the helper once the dose
 * is marked given, so the card goes away on its own.
 */
internal fun catMedicationWarning(states: Map<String, EntityState>): HomeWarning? {
    val am = states[PetsEntities.CAT_MEDICATION_AM_REMINDER]?.isOn == true
    val pm = states[PetsEntities.CAT_MEDICATION_PM_REMINDER]?.isOn == true
    val due = when {
        am && pm -> "morning and evening"
        am -> "morning"
        pm -> "evening"
        else -> return null
    }
    return HomeWarning("Callie needs her $due pill", WarningSeverity.Notification)
}

/** Warn once the fountain drops into the amber/red band (mirrors `waterLevelColor`: <10 red, <35 amber). */
private fun catWaterWarning(level: EntityUi.WaterLevel): HomeWarning? {
    val pct = level.pct ?: return null
    if (pct >= 35) return null
    return if (pct < 10) {
        HomeWarning("Cat fountain needs a refill — ${level.valueText}", WarningSeverity.Critical)
    } else {
        HomeWarning("Cat water running low — ${level.valueText}", WarningSeverity.Warning)
    }
}
