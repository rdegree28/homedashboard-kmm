package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.device_metadata.PetFountainMetadata
import com.degree.homedash.shared.model.device_state.PetFilterState
import com.degree.homedash.ui.readingText

/**
 * Render state for the pet fountain: its water level, and the life left in its filter.
 *
 * One device, two rows on screen — [WaterLevelControl] draws the level and [PetFilterHealthControl]
 * the filter, since they count different things. Read-only: the level row's tap opens its history
 * graph, which is navigation rather than a device action.
 */
@Immutable
data class PetFountainDeviceUi(
    private val metadata: PetFountainMetadata,
    private val state: PetFilterState,
) : DeviceUi {

    override val id: String get() = metadata.entityId

    /** Full width: the fill bars span the row. */
    override val cardSpan: Int get() = 2

    val name: String get() = metadata.displayName

    /** 0–100, or null when the sensor reports nothing usable — the bar then reads empty and grey. */
    val pct: Double? get() = state.waterLevel.value

    val valueText: String get() = state.waterLevel.readingText(decimals = 0)

    /**
     * Days left on the filter, off the separate sensor [PetFountainMetadata.FilterHealth] names. null
     * when the fountain declares no filter, or its sensor isn't reporting — which is not the same as
     * 0, and mustn't render as "change it now".
     */
    val filterDaysRemaining: Int? get() = state.filterDaysRemaining

    /** The cycle those days count down from, so the filter bar has a full scale to draw against. */
    val filterMaxDays: Int? get() = metadata.filterHealth?.maxDays
}
