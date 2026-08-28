package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.SoilMoistureMetadata
import com.degree.homedash.shared.model.states.SoilMoistureState
import com.degree.homedash.ui.readingText

/**
 * Render state for a plant's moisture gauge: the static descriptor plus the live level.
 *
 * Read-only — the row's tap opens the history graph, which is navigation rather than a device action,
 * so unlike the other device UIs this one carries no behavior.
 */
@Immutable
data class SoilMoistureDeviceUi(
    private val metadata: SoilMoistureMetadata,
    private val state: SoilMoistureState,
) : DeviceUi {

    override val id: String get() = metadata.entityId

    /** Full width: the fill bar spans the row. */
    override val cardSpan: Int get() = 2

    val name: String get() = metadata.displayName

    /** 0–100, or null when the sensor reports nothing usable — the bar then reads empty and grey. */
    val pct: Double? get() = state.reading.value

    val valueText: String get() = state.reading.readingText(decimals = 1)
}
