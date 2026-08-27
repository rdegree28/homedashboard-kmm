package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.HistoricalEntityReading

/**
 * Live state of the office workstation: the switch itself, plus what its plug is metering — the draw
 * right now, the running total, and the recent history the power chart draws.
 *
 * [isOffline] tracks the switch alone. The meters are separate entities, so each carries its own
 * "nothing usable to show" in [HistoricalEntityReading.value] rather than dragging the whole device offline.
 */
data class OfficeWorkstationState(
    override val entityId: String,
    override val isOffline: Boolean,
    override val isOn: Boolean,
    val currentPower: HistoricalEntityReading = HistoricalEntityReading.Missing,
    val totalPower: HistoricalEntityReading = HistoricalEntityReading.Missing,
    /** Samples of [currentPower], oldest first. Empty until the fetch lands — the chart says as much. */
    val powerHistory: List<HistoryPoint> = emptyList(),
) : ToggleableDeviceState, DeviceState
