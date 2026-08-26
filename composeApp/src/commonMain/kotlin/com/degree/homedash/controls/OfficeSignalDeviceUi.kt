package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.OfficeSignalMetadata
import com.degree.homedash.shared.model.states.OfficeSignalState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Render state for the office signal selector.
 *
 * Unlike the other devices this isn't a single tile: it offers every [OfficeSignalMetadata.SignalMode] as a
 * pill and highlights whichever the signal currently reports, so it spans the full grid width.
 */
@Immutable
data class OfficeSignalDeviceUi(
    private val metadata: OfficeSignalMetadata,
    private val state: OfficeSignalState,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    /** Null while the signal reports something unmodelled, so no pill reads as active. */
    val activeMode: OfficeSignalMetadata.SignalMode? get() = state.mode
    val offline: Boolean get() = state.isOffline

    fun select(mode: OfficeSignalMetadata.SignalMode, repo: ExpHomeAssistantRepo) =
        metadata.setMode(mode, repo)

    /** Full width: four pills don't fit a half-width tile. */
    override val cardSpan: Int get() = 2
}
