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
    previewPlant("sensor.monstera_soil_moisture", "Monstera Soil Moisture", "58"),
    previewPlant("sensor.fiddle_leaf_soil_moisture", "Fiddle Leaf Fig Soil Moisture", "18"),
    previewPlant("sensor.snake_plant_soil_moisture", "Snake Plant Soil Moisture", "29"),
    previewPlant("sensor.pothos_soil_moisture", "Pothos Soil Moisture", "88"),
)
