package com.degree.homedash.shared.model.device_state

import com.degree.homedash.shared.model.HistoricalEntityReading

/**
 * Live state of the pet fountain: how much water is left in it, and how much life is left in its
 * filter.
 *
 * [isOffline] tracks the water-level sensor alone. The filter is a separate entity, so it reports its
 * own absence through [filterDaysRemaining] rather than taking the whole fountain offline.
 */
data class PetFilterState(
    override val entityId: String,
    override val isOffline: Boolean,

    // Water level
    val waterLevel: HistoricalEntityReading = HistoricalEntityReading.Missing,

    // Filter days remaining
    /**
     * Whole days left on the filter, null when the fountain declares no filter or its sensor isn't
     * reporting — which is not the same as 0, and mustn't render as "change it now".
     */
    val filterDaysRemaining: Int? = null,
) : DeviceState
