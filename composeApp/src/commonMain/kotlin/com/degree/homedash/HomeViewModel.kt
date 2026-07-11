package com.degree.homedash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.pets.PetsEntities
import com.degree.homedash.pets.toWaterLevel
import com.degree.homedash.shared.data.HomeAssistantRepo
import com.degree.homedash.shared.model.EntityState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Severity of a [HomeWarning] — drives the warning card's color. */
enum class WarningSeverity { Warning, Critical }

/** An at-a-glance warning shown at the top of the Home launcher. */
data class HomeWarning(val message: String, val severity: WarningSeverity)

/** Surfaces launcher warnings from live state (currently the cat water fountain level). */
class HomeViewModel(
    private val repo: HomeAssistantRepo,
) : ViewModel() {

    val warnings: StateFlow<List<HomeWarning>> =
        repo.states
            .map { states ->
                buildList { catWaterWarning(states[PetsEntities.CAT_WATER_LEVEL])?.let(::add) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** Warn once the fountain drops into the amber/red band (mirrors `waterLevelColor`: <10 red, <35 amber). */
private fun catWaterWarning(state: EntityState?): HomeWarning? {
    val level = state?.toWaterLevel() ?: return null
    val pct = level.pct ?: return null
    if (pct >= 35) return null
    return if (pct < 10) {
        HomeWarning("Cat fountain needs a refill — ${level.valueText}", WarningSeverity.Critical)
    } else {
        HomeWarning("Cat water running low — ${level.valueText}", WarningSeverity.Warning)
    }
}
