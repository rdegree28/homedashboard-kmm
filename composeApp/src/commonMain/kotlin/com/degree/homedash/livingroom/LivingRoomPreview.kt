package com.degree.homedash.livingroom

import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi

// Sample data for the Living Room @Previews (which live next to their composables in this package).

internal val previewLights: List<EntityUi.Light> = listOf(
    EntityUi.Light(
        metadata = LightMetadata(LivingRoomEntities.LIVING_ROOM_LIGHT_WEST, "Ceiling"),
        isOn = true,
        offline = false,
    ),
    EntityUi.Light(
        metadata = LightMetadata(LivingRoomEntities.LIVING_ROOM_LIGHT_EAST, "Lamp"),
        isOn = false,
        offline = false,
    ),
    EntityUi.Light(
        metadata = LightMetadata(LivingRoomEntities.HOMEWORK_LIGHT, "Accent"),
        isOn = false,
        offline = true,
    ),
)

internal val previewFans: List<EntityUi.Fan> = listOf(
    EntityUi.Fan(
        metadata = FanMetadata(LivingRoomEntities.LIVING_ROOM_FAN, "Fan", FanMetadata.SpeedAdjustment(12)),
        isOn = true,
        offline = false,
        percentage = 75,
    ),
    EntityUi.Fan(
        metadata = FanMetadata(LivingRoomEntities.LIVING_ROOM_BOX_FAN, "Ceiling Fan", FanMetadata.SpeedAdjustment(3)),
        isOn = false,
        offline = false,
        percentage = 0,
    ),
)

internal val previewThermostats: List<EntityUi.Thermostat> = listOf(
    EntityUi.Thermostat(
        metadata = ThermostatMetadata.livingRoomThermostat(LivingRoomEntities.THERMOSTAT, "Thermostat"),
        offline = false,
        hvacMode = HvacMode.Cool,
        hvacAction = HvacAction.Idle,
        targetTemperature = 72.0,
        currentTemperature = 70.0,
        currentHumidity = 59.0,
        fanMode = "off",
        presetMode = "none",
    ),
)

internal val previewClimate: List<EntityUi.Climate> = listOf(
    EntityUi.Climate(
        metadata = ClimateMetadata(LivingRoomEntities.TEMPERATURE, "Temperature", ClimateMetadata.ClimateKind.Temperature),
        valueText = "72.5 °F",
    ),
    EntityUi.Climate(
        metadata = ClimateMetadata(LivingRoomEntities.HUMIDITY, "Dew Point", ClimateMetadata.ClimateKind.DewPoint),
        valueText = "50.9 °F (48%)",
    ),
)
