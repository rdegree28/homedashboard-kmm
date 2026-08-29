package com.degree.homedash.shared.model.device_state

import com.degree.homedash.shared.model.HistoricalEntityReading

/**
 * Live state of a plant's soil-moisture sensor: what it is reading right now.
 *
 * [reading] is [HistoricalEntityReading.Missing] while the sensor is unavailable or reporting
 * something non-numeric, which the gauge shows as an empty grey bar rather than 0 %.
 */
data class SoilMoistureState(
    override val entityId: String,
    override val isOffline: Boolean,
    val reading: HistoricalEntityReading = HistoricalEntityReading.Missing,
) : DeviceState
