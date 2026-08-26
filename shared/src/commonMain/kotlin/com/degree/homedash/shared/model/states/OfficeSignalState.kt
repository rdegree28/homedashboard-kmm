package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.entity.OfficeSignalMetadata

/**
 * Live state of the office signal: which mode it is currently showing.
 *
 * [mode] is null when Home Assistant reports something the dashboard doesn't model — including while
 * the sensor is unavailable — so the selector highlights nothing rather than guessing.
 */
data class OfficeSignalState(
    override val entityId: String,
    override val isOffline: Boolean,
    val mode: OfficeSignalMetadata.SignalMode?,
) : DeviceState
