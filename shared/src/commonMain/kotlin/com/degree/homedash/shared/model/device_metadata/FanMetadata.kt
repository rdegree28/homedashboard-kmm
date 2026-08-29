package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.FanState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlin.math.roundToInt

/**
 * A fan.
 *
 * @param speedAdjustment the fan's discrete speeds, or null when it is on/off only — controls show a
 *   speed slider exactly when this is non-null.
 */
data class FanMetadata(
    override val entityId: String,
    override val displayName: String,
    val speedAdjustment: SpeedAdjustment? = null,
    val hasOscillationFeature: Boolean = false,
    val misting: MistingControl? = null,
) : ToggleableDeviceMetadata, StatefulDeviceMetadata<FanState> {

    /**
     * The only state assembled from two entities: the mister is its own `humidifier.*` rather than a
     * fan attribute (see [MistingControl]), so its flow is combined in rather than read off [entityId].
     * A fan with no mister contributes a constant null instead.
     */
    override fun loadState(repo: ExpHomeAssistantRepo): Flow<FanState> =
        combine(
            repo.entityForDevice(this),
            misting?.let { repo.entityFor(it.entityId) } ?: flowOf(null),
        ) { entity, mister ->
            FanState(
                entityId = entityId,
                isOn = entity?.isOn == true,
                isOffline = entity == null || entity.isUnavailable,
                percentage = entity?.attrDouble("percentage")?.roundToInt() ?: 0,
                isOscillating = entity?.attrBoolean("oscillating") == true,
                isMisting = mister?.isOn == true,
            )
        }

    /**
     * A fan's built-in mister, or null when it has none.
     *
     * Home Assistant models the mister as a *separate* entity rather than a fan feature — the misting
     * fan exposes `humidifier.misting_fan_humidifier` alongside `fan.misting_fan` — so the id has to be
     * carried explicitly. Everything about the card's mister button reads from [entityId], not the fan's.
     */
    data class MistingControl(
        val entityId: String,
    )

    /**
     * How many discrete speeds a fan has. Derived from Home Assistant's `percentage_step`
     * (12 steps for a typical ceiling fan); controls step the slider in whole levels so every
     * speed is reachable.
     */
    data class SpeedAdjustment(
        val levelCount: Int,
    ) {
        companion object {
            /**
             * [levelCount] steps, or null when the fan can't usefully be sped up or down — fewer than
             * two levels means the only reachable speeds are off and full.
             */
            fun forLevelCount(levelCount: Int): SpeedAdjustment? =
                if (levelCount >= 2) SpeedAdjustment(levelCount) else null
        }
    }

    fun setFanSpeed(newFanPercentage: Int, repo: ExpHomeAssistantRepo) {
        repo.setFanPercentage(metadata = this, percentage = newFanPercentage)
    }

    fun setOscillationState(newOscillationState: Boolean, repo: ExpHomeAssistantRepo) {
        repo.setFanOscillating(metadata = this, oscillating = newOscillationState)
    }

    fun setMistingState(newMistingState: Boolean, repo: ExpHomeAssistantRepo) {
        repo.setMisting(metadata = this, misting = newMistingState)
    }

    // Defined for factories
    companion object
}
