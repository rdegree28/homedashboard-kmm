package com.degree.homedash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.core.device.NavigationDeviceUi
import com.degree.homedash.core.device.PetFountainDeviceUi
import com.degree.homedash.core.device.ThermostatDeviceUi
import com.degree.homedash.core.loadDeviceUis
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
    metadataRepo: EntityMetadataRepo,
    deviceRepo: ExpHomeAssistantRepo,
) : ViewModel() {

    private val petEntities = metadataRepo.loadPetsEntityMetadataList()
    private val thermostatEntities = metadataRepo.loadHomeThermostatMetadataList()

    /**
     * The whole-house thermostat, shown above the launcher cards. Its own flow rather than a field on
     * a combined UI state: it and [warnings] are projected from unrelated entities and neither has any
     * bearing on the other, so a shared state object would only make each recompose for the other's
     * changes.
     */
    val thermostats: StateFlow<List<ThermostatDeviceUi>> =
        thermostatEntities.loadDeviceUis(deviceRepo)
            .map { devices -> devices.filterIsInstance<ThermostatDeviceUi>() }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * The launcher cards. A plain `val`, not a flow — a launcher card has no Home Assistant entity
     * behind it, so there is no state to project and nothing to re-emit. Feature-flag filtering
     * happens in the repo, which is where the flag set lives.
     */
    val navigation: List<NavigationDeviceUi> =
        metadataRepo.loadHomeScreenMetadataList()
            .filterIsInstance<NavigationMetadata>()
            .map(::NavigationDeviceUi)

    /**
     * Two sources: the fountain is a device, projected through its own metadata exactly as the Pets
     * screen renders it, while the medication reminders are read straight off raw state (see
     * [catMedicationWarning]).
     */
    val warnings: StateFlow<List<HomeWarning>> =
        combine(petEntities.loadDeviceUis(deviceRepo), deviceRepo.loadEntityStates()) { devices, states ->
            devices.filterIsInstance<PetFountainDeviceUi>().mapNotNull(::catWaterWarning) +
                listOfNotNull(catMedicationWarning(states))
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/**
 * The cat's medication reminders. Home Assistant's automations turn each helper on when that dose
 * comes due and off again once it's marked given, so these read as "this dose is still owed" — the
 * app only ever watches them.
 *
 * Spelled out here rather than in a roster because they have no metadata type: there is no card and
 * no control behind them, and [catMedicationWarning] is the only thing that reads them.
 */
internal object CatMedicationEntities {
    const val AM_REMINDER = "input_boolean.cat_medication_am_reminder_active"
    const val PM_REMINDER = "input_boolean.cat_medication_pm_reminder_active"
}

/**
 * Warn while either of the cat's medication reminders is active, as one card naming whichever doses
 * are still owed.
 *
 * These are read straight from [states] rather than projected as devices like the fountain above:
 * they're plain `input_boolean` helpers with nothing to render — no card, no control, no metadata
 * type — and the launcher only ever asks whether they're on.
 *
 * Blue, not amber: a due dose is a standing to-do rather than something going wrong, so it sits
 * below the fountain's warnings on the urgency scale. Home Assistant clears the helper once the dose
 * is marked given, so the card goes away on its own.
 */
internal fun catMedicationWarning(states: Map<String, EntityState>): HomeWarning? {
    val am = states[CatMedicationEntities.AM_REMINDER]?.isOn == true
    val pm = states[CatMedicationEntities.PM_REMINDER]?.isOn == true
    val due = when {
        am && pm -> "morning and evening"
        am -> "morning"
        pm -> "evening"
        else -> return null
    }
    return HomeWarning("Callie needs her $due pill", WarningSeverity.Notification)
}

/** Warn once the fountain drops into the amber/red band (mirrors `waterLevelColor`: <10 red, <35 amber). */
private fun catWaterWarning(level: PetFountainDeviceUi): HomeWarning? {
    val pct = level.pct ?: return null
    if (pct >= 35) return null
    return if (pct < 10) {
        HomeWarning("Cat fountain needs a refill — ${level.valueText}", WarningSeverity.Critical)
    } else {
        HomeWarning("Cat water running low — ${level.valueText}", WarningSeverity.Warning)
    }
}
