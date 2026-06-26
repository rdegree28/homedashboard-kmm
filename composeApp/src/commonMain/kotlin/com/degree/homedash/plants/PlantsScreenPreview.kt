package com.degree.homedash.plants

import com.degree.homedash.shared.model.EntityState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// Sample data for the Plants @Preview (which lives next to PlantsContent in PlantsScreen.kt).

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
