package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.ToggleableEntityMetadata
import com.degree.homedash.shared.model.states.LightState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Render state for a light card: the static descriptor plus the live state, both plain values.
 *
 * The toggle is the metadata's, since flipping a light needs its identity rather than its current
 * value; the repo to run it through is handed to [onToggle] at the call site rather than held, so
 * nothing about the card's identity depends on a DI object.
 */
@Immutable
data class LightDeviceUi(
    private val metadata: ToggleableEntityMetadata,
    private val state: LightState,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val isOn: Boolean get() = state.isOn
    val offline: Boolean get() = state.isOffline

    fun onToggle(repo: ExpHomeAssistantRepo) = metadata.onToggle(repo)

    override val cardSpan: Int
        get() = 1
}
