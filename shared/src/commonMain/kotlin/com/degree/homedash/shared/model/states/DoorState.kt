package com.degree.homedash.shared.model.states

/**
 * Live state of a door contact sensor. Read-only — [isOpen] is all there is to report.
 */
data class DoorState(
    override val entityId: String,
    override val isOffline: Boolean,
    val isOpen: Boolean,
) : DeviceState
