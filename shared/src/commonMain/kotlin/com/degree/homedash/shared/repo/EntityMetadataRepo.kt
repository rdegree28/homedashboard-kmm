package com.degree.homedash.shared.repo

import com.degree.homedash.shared.dao.AuthRepo
import com.degree.homedash.shared.dao.FeatureFlagDao
import com.degree.homedash.shared.model.FeatureFlag
import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.entity.DoorMetadata
import com.degree.homedash.shared.model.entity.DeviceMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.model.entity.NavigationMetadata
import com.degree.homedash.shared.model.entity.NavigationMetadata.CardPhoto
import com.degree.homedash.shared.model.entity.NavigationMetadata.NavigationTarget
import com.degree.homedash.shared.model.entity.NavigationMetadata.RoomIcon
import com.degree.homedash.shared.model.entity.SoilMoistureMetadata
import com.degree.homedash.shared.model.entity.ThermostatMetadata
import com.degree.homedash.shared.model.entity.TriggerDeviceMetadata
import com.degree.homedash.shared.model.entity.WaterLevelMetadata
import com.degree.homedash.shared.model.entity.bedroomLightsFull
import com.degree.homedash.shared.model.entity.bedroomLightsLow
import com.degree.homedash.shared.model.entity.bedroomLightsNight
import com.degree.homedash.shared.model.entity.dellaTowerFan
import com.degree.homedash.shared.model.entity.livingRoomThermostat
import com.degree.homedash.shared.model.entity.mainLights
import com.degree.homedash.shared.model.entity.movieLights

/**
 * Repository for providing DeviceMetadata to the UI components.
 *
 * Each method declares one screen's roster: which entities it shows, in render order, with the label
 * for each. Entity ids are spelled out here rather than pulled from the `*Entities` objects in the
 * UI module, which this module cannot see — those objects still own the ids that have no metadata
 * type (scripts, raw sensors, switches).
 *
 * Rosters are also filtered to what the signed-in user is allowed to see, so screens receive a list
 * they can render as-is without knowing about feature flags.
 */
class EntityMetadataRepo(
    private val featureFlagDao: FeatureFlagDao,
    private val authRepo: AuthRepo,
) {

    /**
     * The Home launcher's dashboard cards, in render order, minus any whose dashboard is gated behind
     * a feature flag the current user doesn't hold.
     *
     * These carry no Home Assistant entity — see [NavigationMetadata]. The list is resolved per call
     * against whoever is signed in at that moment.
     */
    fun loadHomeScreenMetadataList(): List<DeviceMetadata> {
        val flags = currentUserFlags()
        return HOME_SCREEN_CARDS.filter { card ->
            gatingFlag(card.destination)?.let { it in flags } ?: true
        }
    }

    /**
     * The thermostat the Home launcher puts above its cards.
     *
     * It lives here rather than in a room's roster because it drives the whole house: the unit is
     * bolted to a living room wall, but nothing about what it does belongs to that room, and burying
     * it a tap deep in a dashboard the rest of the household can't open made it look like it did.
     *
     * Its own method rather than an entry in [loadHomeScreenMetadataList], which is navigation cards
     * only — those carry no Home Assistant entity and are projected against an empty state map.
     */
    fun loadHomeThermostatMetadataList(): List<DeviceMetadata> = listOf(
        ThermostatMetadata.livingRoomThermostat("climate.living_room_thermostat", "Thermostat"),
    )

    /** Flags for whoever is signed in; none when logged out. */
    private fun currentUserFlags(): Set<FeatureFlag> =
        authRepo.loadCurrentUser().value?.let(featureFlagDao::getFeatureFlagsForUser).orEmpty()

    /**
     * The Office lights, fans, climate sensors, and door.
     *
     * Deliberately excludes the rest of the Office dashboard, none of which has an [DeviceMetadata]
     * type yet: the workstation switch, the hexagon lights, the traffic signal and its three scripts,
     * the signal-mode sensor, and the power/energy sensors.
     */
    fun loadOfficeEntityMetadataList(): List<DeviceMetadata> = listOf(
        LightMetadata("light.office_light", "Office"),
        LightMetadata("light.office_small_light", "Small"),
        FanMetadata.dellaTowerFan("fan.office_fan_office_fan", "Office Fan"),
        FanMetadata("fan.office_box_fan", "Box Fan"),
        // The misting fan reports no usable percentage_step; 6 is the count the dashboard has always used.
        FanMetadata(
            "fan.misting_fan",
            "Misting Fan",
            FanMetadata.SpeedAdjustment(6),
            hasOscillationFeature = true,
            misting = FanMetadata.MistingControl("humidifier.misting_fan_humidifier"),
        ),
        ClimateMetadata("sensor.sonoff_snzb_02d_temperature", "Temperature", ClimateMetadata.ClimateKind.Temperature),
        ClimateMetadata("sensor.sonoff_snzb_02d_humidity", "Humidity", ClimateMetadata.ClimateKind.Humidity),
        DoorMetadata("binary_sensor.office_door_sensor", "Office Door"),
    )

    /**
     * The Living Room lights, fans, and climate sensors.
     *
     * The thermostat is deliberately not here — see [loadHomeThermostatMetadataList]. The two sensor
     * cards below stay: they are the thermostat's own readings, but what they measure really is this
     * room.
     */
    fun loadLivingRoomEntityMetadataList(): List<DeviceMetadata> = listOf(
        TriggerDeviceMetadata.mainLights(),
        TriggerDeviceMetadata.movieLights(),
        LightMetadata("light.living_room_light_west", "West"),
        LightMetadata("light.living_room_light_east", "East"),
        LightMetadata("light.dining_ceiling_light", "Dining Ceiling"),
        LightMetadata("light.kitchen_stove_light", "Kitchen Stove"),
        FanMetadata.dellaTowerFan("fan.living_room_fan", "Fan"),
        // A plain switch, so no speed control despite the name.
        FanMetadata("switch.living_room_acc_1", "Box Fan"),
        ClimateMetadata("sensor.living_room_thermostat_temperature", "Temperature", ClimateMetadata.ClimateKind.Temperature),
        // Unlike Office (humidity card with dew point as a subvalue), the Living Room shows a dew point
        // card computed from the temperature + humidity pair — keyed off the humidity entity id.
        ClimateMetadata("sensor.living_room_thermostat_humidity", "Dew Point", ClimateMetadata.ClimateKind.DewPoint),
    )

    /**
     * The Bedroom entities.
     *
     * Empty for now — the room's card and screen exist, but nothing in Home Assistant is wired up to
     * it yet. Add entries here and `BedroomScreen` can project them the way the other rooms do.
     */
    fun loadBedroomEntityMetadataList(): List<DeviceMetadata> = listOf(
        TriggerDeviceMetadata.bedroomLightsFull(),
        TriggerDeviceMetadata.bedroomLightsLow(),
        TriggerDeviceMetadata.bedroomLightsNight(),
        LightMetadata("light.bedroom_light_west", "West"),
        LightMetadata("light.bedroom_light_east", "East"),
        FanMetadata("fan.bedroom_box_fan", "Box Fan"),
        FanMetadata.dellaTowerFan("fan.bedroom_fan_tower_fan", "Tower Fan"),
    )

    /** The Pets sensors — currently just the cat water fountain: its water level and its filter. */
    fun loadPetsEntityMetadataList(): List<DeviceMetadata> = listOf(
        WaterLevelMetadata(
            "sensor.cat_water_fountain_remaining_water_pct",
            "Remaining Water",
            // 31 days is what the fountain's own `number.cat_water_fountain_filter_cycle` holds.
            filterHealth = WaterLevelMetadata.FilterHealth(
                "sensor.cat_water_fountain_remaining_filter_day",
                maxDays = 31,
            ),
        ),
    )

    /**
     * The plant soil-moisture sensors, in the order the screen sorts them (by name).
     *
     * A snapshot taken 2026-07-28 of every entity matching the `soil_moisture` id suffix that the
     * Plants screen scans for; names are what that screen derives from each sensor's friendly name.
     * `PlantsViewModel` still does the live scan, so a plant added in Home Assistant shows up there
     * without being listed here — but it won't appear in this roster until someone adds it.
     */
    fun loadPlantsEntityMetadataList(): List<DeviceMetadata> = listOf(
        SoilMoistureMetadata("sensor.dotty_moisture_sensor_soil_moisture", "Dotty"),
        SoilMoistureMetadata("sensor.gray_pot_moisture_sensor_soil_moisture", "Gray Pot"),
        SoilMoistureMetadata("sensor.louie_moisture_sensor_soil_moisture", "Louie"),
        SoilMoistureMetadata("sensor.living_room_orange_pot_moisture_sensor_soil_moisture", "Orange Pot"),
    )

    private companion object {

        /** Every launcher card, before feature-flag filtering. */
        val HOME_SCREEN_CARDS: List<NavigationMetadata> = listOf(
            NavigationMetadata(NavigationTarget.Bedroom, "Bedroom", RoomIcon.Bed, 0xFF9575CD),
            NavigationMetadata(NavigationTarget.LivingRoom, "Living Room", RoomIcon.Sofa, 0xFFF0C930),
            NavigationMetadata(NavigationTarget.Office, "Office", RoomIcon.Desk, 0xFF3298CE),
            NavigationMetadata(NavigationTarget.Plants, "Plants", RoomIcon.Plant, 0xFF00FF00),
            NavigationMetadata(NavigationTarget.Pets, "Pets", RoomIcon.Paw, 0xFFC29844, photo = CardPhoto.Callie),
        )

        /** The flag a dashboard sits behind, or null when it's available to everyone. */
        fun gatingFlag(target: NavigationTarget): FeatureFlag? = when (target) {
            NavigationTarget.Office -> FeatureFlag.ViewOfficeScreen
            NavigationTarget.LivingRoom,
            NavigationTarget.Bedroom,
            NavigationTarget.Plants,
            NavigationTarget.Pets,
            -> null
        }
    }
}
