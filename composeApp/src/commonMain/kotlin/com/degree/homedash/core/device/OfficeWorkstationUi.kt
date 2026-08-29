package com.degree.homedash.core.device

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.device_metadata.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.device_state.OfficeWorkstationState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.ui.readingText

/**
 * Render state for the workstation: the switch row, its two meter readouts, and the samples behind
 * the power chart.
 *
 * A data class so an unchanged workstation compares equal — the screen's state is de-duplicated, and
 * the plug's power sensor reports often enough that identity equality would recompose the whole
 * dashboard every few seconds.
 */
@Immutable
data class OfficeWorkstationUi(
    private val metadata: OfficeWorkstationMetadata,
    private val state: OfficeWorkstationState,
) : DeviceUi {

    override val id: String get() = metadata.entityId

    /** Full width: the chart and its readouts don't fit a half-width tile. */
    override val cardSpan: Int get() = 2

    val name: String get() = metadata.displayName
    val isOn: Boolean get() = state.isOn
    val isOffline: Boolean get() = state.isOffline

    /** Live draw, e.g. "61 W"; "—" while the meter reports nothing usable. */
    val currentPower: String get() = state.currentPower.readingText(decimals = 2)

    /** Energy used since the plug started counting, e.g. "62.01 kWh". */
    val totalPower: String get() = state.totalPower.readingText(decimals = 2)

    /** Empty until the history fetch lands, which the chart draws as "Loading…". */
    val powerHistoryPoints: List<HistoryPoint> get() = state.powerHistory

    fun onToggle(repo: ExpHomeAssistantRepo) = metadata.onToggle(repo)
}
