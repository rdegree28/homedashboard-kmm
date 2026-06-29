package com.degree.homedash.plants

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.sin

// Sample data for the Plants @Previews (which live next to their composables in this package).

private fun previewPlant(id: String, name: String, moisture: String) = EntityState(
    entityId = id,
    state = moisture,
    attributes = JsonObject(
        mapOf(
            "friendly_name" to JsonPrimitive(name),
            "device_class" to JsonPrimitive("moisture"),
            "unit_of_measurement" to JsonPrimitive("%"),
        ),
    ),
)

internal val previewPlants: List<EntityState> = listOf(
    previewPlant("sensor.louie_moisture_sensor_soil_moisture", "Louie Soil Moisture", "58"),
)

// One week of hourly samples so the day axis shows several labels in the preview.
internal val previewMoistureHistory: List<HistoryPoint> =
    List(168) { i -> HistoryPoint(timeSeconds = i * 3600.0, value = 45 + 40 * sin(i * 0.25)) }
