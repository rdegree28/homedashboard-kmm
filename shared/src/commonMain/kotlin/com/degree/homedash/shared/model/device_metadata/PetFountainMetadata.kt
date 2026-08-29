package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.PetFilterState
import com.degree.homedash.shared.model.toReading
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlin.math.roundToInt

/**
 * A water-level sensor (the pet fountain), rendered as a percentage gauge. Read-only, so identity is
 * all it carries — the level comes from the live state.
 */
data class PetFountainMetadata(
    override val entityId: String,
    override val displayName: String,
    /** Non-null when a second entity reports filter life; the screen shows its bar exactly then. */
    val filterHealth: FilterHealth? = null,
) : StatefulDeviceMetadata<PetFilterState> {

    /**
     * The filter's own sensor, plus the cycle length its days count down from.
     *
     * A composite entity, the way [FanMetadata.MistingControl] and [ThermostatMetadata.ExtremeToggle]
     * are: the fountain publishes its filter life as a separate `sensor.*`, not as an attribute of
     * the water level.
     *
     * [maxDays] is hand-declared rather than read from the fountain's `number.*` filter-cycle entity,
     * for the reason [ThermostatMetadata.TargetTemperature] gives: a bar whose full scale can vanish
     * when a device drops offline collapses to nothing.
     */
    data class FilterHealth(val entityId: String, val maxDays: Int)

    /**
     * Assembled from two entities, the way a fan's mister is: the filter's countdown is its own
     * `sensor.*` rather than an attribute of the water level, so its flow is combined in rather than
     * read off [entityId]. A fountain with no filter contributes a constant null instead.
     */
    override fun loadState(repo: HomeAssistantRepo): Flow<PetFilterState> =
        combine(
            repo.entityForDevice(this),
            filterHealth?.let { repo.entityFor(it.entityId) } ?: flowOf(null),
        ) { fountain, filter ->
            PetFilterState(
                entityId = entityId,
                isOffline = fountain == null || fountain.isUnavailable,
                waterLevel = fountain.toReading(),
                // Whole days: the sensor reports fractions, and half a day of filter life is noise.
                filterDaysRemaining = filter.toReading().value?.roundToInt(),
            )
        }
}
