package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.states.FanState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Render state for a fan card: the static descriptor plus the live state, both plain values.
 *
 * The actions are the metadata's, since driving a fan needs its identity rather than its current
 * value; the repo to run them through is handed in at the call site rather than held, so nothing
 * about the card's identity depends on a DI object.
 */
@Immutable
data class FanDeviceUi(
    private val metadata: FanMetadata,
    private val state: FanState,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val isOn: Boolean get() = state.isOn
    val offline: Boolean get() = state.isOffline

    val levelCount: Int = metadata.speedAdjustment?.levelCount ?: 1
    val percentage: Int = state.percentage
    /** Whether the fan can oscillate at all — drives whether the toggle is offered. */
    val hasOscillation: Boolean = metadata.hasOscillationFeature
    val isOscillating: Boolean = state.isOscillating
    /** Whether the fan can mist at all — drives whether the toggle is offered. */
    val hasMisting: Boolean = metadata.misting != null
    val isMisting: Boolean = state.isMisting

    fun onToggle(repo: ExpHomeAssistantRepo) = metadata.onToggle(repo)

    fun toggleOscillation(repo: ExpHomeAssistantRepo) = metadata.setOscillationState(
        newOscillationState = !isOscillating,
        repo = repo,
    )

    fun toggleMisting(repo: ExpHomeAssistantRepo) = metadata.setMistingState(
        newMistingState = !isMisting,
        repo = repo,
    )

    fun setFanSpeed(percentage: Int, repo: ExpHomeAssistantRepo) = metadata.setFanSpeed(
        newFanPercentage = percentage,
        repo = repo,
    )

    override val cardSpan: Int get() = if (isOn && (metadata.speedAdjustment != null || hasOscillation || hasMisting)) 2 else 1
}
