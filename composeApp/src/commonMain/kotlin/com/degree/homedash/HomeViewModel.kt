package com.degree.homedash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.toEntityUis
import com.degree.homedash.shared.repo.EntityMetadataRepo
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Severity of a [HomeWarning] — drives the warning card's color. */
enum class WarningSeverity { Warning, Critical }

/** An at-a-glance warning shown at the top of the Home launcher. */
data class HomeWarning(val message: String, val severity: WarningSeverity)

/**
 * Backs the Home launcher: the dashboard cards it offers, plus any warnings drawn from live state
 * (currently the cat water fountain level).
 */
class HomeViewModel(
    private val repo: HomeAssistantRepo,
    metadataRepo: EntityMetadataRepo,
) : ViewModel() {

    private val petEntities = metadataRepo.loadPetsEntityMetadataList()

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
                    .mapNotNull(::catWaterWarning)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
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
