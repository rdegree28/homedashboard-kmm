package com.degree.homedash.shared.model.device_state

import com.degree.homedash.shared.model.HistoricalEntityReading

/**
 * Live state of a climate sensor: the [reading] its card shows, and the [subvalue] printed under it
 * for the readings that are derived from a pair of sensors rather than one.
 */
data class ClimateState(
    override val entityId: String,
    override val isOffline: Boolean,
    val reading: HistoricalEntityReading,
    /** The second half of a derived reading — the humidity behind a dew point. Null for a plain sensor. */
    val subvalue: HistoricalEntityReading? = null,
) : DeviceState
