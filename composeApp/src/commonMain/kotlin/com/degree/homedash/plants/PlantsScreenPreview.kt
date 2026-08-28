package com.degree.homedash.plants

import com.degree.homedash.controls.SoilMoistureDeviceUi
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.entity.SoilMoistureMetadata
import com.degree.homedash.shared.model.states.SoilMoistureState
import kotlin.math.sin

// Sample data for the Plants @Previews (which live next to their composables in this package).

internal val previewPlants: List<SoilMoistureDeviceUi> = listOf(
    SoilMoistureDeviceUi(
        metadata = SoilMoistureMetadata("sensor.louie_moisture_sensor_soil_moisture", "Louie"),
        state = SoilMoistureState(
            entityId = "sensor.louie_moisture_sensor_soil_moisture",
            isOffline = false,
            reading = HistoricalEntityReading(58.0, "%"),
        ),
    ),
)

// One week of hourly samples so the day axis shows several labels in the preview.
internal val previewMoistureHistory: List<HistoryPoint> =
    List(168) { i -> HistoryPoint(timeSeconds = i * 3600.0, value = 45 + 40 * sin(i * 0.25)) }
