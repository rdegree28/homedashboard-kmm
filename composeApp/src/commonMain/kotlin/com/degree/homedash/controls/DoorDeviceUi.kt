package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.DoorMetadata
import com.degree.homedash.shared.model.states.DoorState

/**
 * Render state for a door row. Read-only: there is nothing to drive, so unlike the toggleable
 * devices this carries no action.
 */
@Immutable
data class DoorDeviceUi(
    private val metadata: DoorMetadata,
    private val state: DoorState,
) : DeviceUi {

    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val open: Boolean get() = state.isOpen
    val unavailable: Boolean get() = state.isOffline

    /** Dashes out while offline rather than claiming the door is closed. */
    val statusText: String = when {
        state.isOffline -> "—"
        state.isOpen -> "Open"
        else -> "Closed"
    }

    override val cardSpan: Int get() = 1
}
