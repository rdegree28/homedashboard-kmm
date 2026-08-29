package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.device_metadata.LightMetadata
import com.degree.homedash.shared.model.device_state.LightState
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
    private val metadata: LightMetadata,
    private val state: LightState,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val isOn: Boolean get() = state.isOn
    val offline: Boolean get() = state.isOffline

    /** The glyph and the on-colour this fixture declares — a bulb in amber unless it says otherwise. */
    val icon: LightMetadata.LightIcon get() = metadata.icon
    val tint: Long get() = metadata.tint

    fun onToggle(repo: ExpHomeAssistantRepo) = metadata.onToggle(repo)

    override val cardSpan: Int
        get() = 1
}
