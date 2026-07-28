package com.degree.homedash.shared.repo

import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.entity.DoorMetadata
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.model.entity.SoilMoistureMetadata
import com.degree.homedash.shared.model.entity.WaterLevelMetadata

/**
 * Repository for providing EntityMetadata to the UI components.
 *
 * Each method declares one screen's roster: which entities it shows, in render order, with the label
 * for each. Entity ids are spelled out here rather than pulled from the `*Entities` objects in the
 * UI module, which this module cannot see — those objects still own the ids that have no metadata
 * type (scripts, raw sensors, switches).
 */
class EntityMetadataRepo {

    /**
     * The Office lights, fans, climate sensors, and door.
     *
     * Deliberately excludes the rest of the Office dashboard, none of which has an [EntityMetadata]
     * type yet: the workstation switch, the hexagon lights, the traffic signal and its three scripts,
     * the signal-mode sensor, and the power/energy sensors.
     */
    fun loadOfficeEntityMetadataList(): List<EntityMetadata> = listOf(
        LightMetadata("light.office_light", "Office"),
        LightMetadata("light.office_small_light", "Small"),
        FanMetadata("fan.office_fan_office_fan", "Office Fan", FanMetadata.SpeedAdjustment(12)),
        FanMetadata("fan.office_box_fan", "Box Fan"),
        // The misting fan reports no usable percentage_step; 6 is the count the dashboard has always used.
        FanMetadata("fan.misting_fan", "Misting Fan", FanMetadata.SpeedAdjustment(6)),
        ClimateMetadata("sensor.sonoff_snzb_02d_temperature", "Temperature", ClimateMetadata.ClimateKind.Temperature),
        ClimateMetadata("sensor.sonoff_snzb_02d_humidity", "Humidity", ClimateMetadata.ClimateKind.Humidity),
        DoorMetadata("binary_sensor.office_door_sensor", "Office Door"),
    )

    /** The Living Room lights, fans, and climate sensors. */
    fun loadLivingRoomEntityMetadataList(): List<EntityMetadata> = listOf(
        LightMetadata("light.living_room_light_west", "West"),
        LightMetadata("light.living_room_light_east", "East"),
        LightMetadata("light.homework_light", "Homework"),
        LightMetadata("light.dining_ceiling_light", "Dining Ceiling"),
        LightMetadata("light.kitchen_stove_light", "Kitchen Stove"),
        FanMetadata("fan.living_room_fan", "Fan", FanMetadata.SpeedAdjustment(12)),
        // A plain switch, so no speed control despite the name.
        FanMetadata("switch.living_room_acc_1", "Box Fan"),
        ClimateMetadata("sensor.living_room_thermostat_temperature", "Temperature", ClimateMetadata.ClimateKind.Temperature),
        // Unlike Office (humidity card with dew point as a subvalue), the Living Room shows a dew point
        // card computed from the temperature + humidity pair — keyed off the humidity entity id.
        ClimateMetadata("sensor.living_room_thermostat_humidity", "Dew Point", ClimateMetadata.ClimateKind.DewPoint),
    )

    /** The Pets sensors — currently just the cat water fountain. */
    fun loadPetsEntityMetadataList(): List<EntityMetadata> = listOf(
        WaterLevelMetadata("sensor.cat_water_fountain_remaining_water_pct", "Remaining Water"),
    )

    /**
     * The plant soil-moisture sensors, in the order the screen sorts them (by name).
     *
     * A snapshot taken 2026-07-28 of every entity matching the `soil_moisture` id suffix that the
     * Plants screen scans for; names are what that screen derives from each sensor's friendly name.
     * `PlantsViewModel` still does the live scan, so a plant added in Home Assistant shows up there
     * without being listed here — but it won't appear in this roster until someone adds it.
     */
    fun loadPlantsEntityMetadataList(): List<EntityMetadata> = listOf(
        SoilMoistureMetadata("sensor.dotty_moisture_sensor_soil_moisture", "Dotty"),
        SoilMoistureMetadata("sensor.gray_pot_moisture_sensor_soil_moisture", "Gray Pot"),
        SoilMoistureMetadata("sensor.louie_moisture_sensor_soil_moisture", "Louie"),
        SoilMoistureMetadata("sensor.living_room_orange_pot_moisture_sensor_soil_moisture", "Orange Pot"),
    )
}
