package com.degree.homedash.livingroom

import com.degree.homedash.controls.ClimateDeviceUi
import com.degree.homedash.controls.FanDeviceUi
import com.degree.homedash.controls.LightDeviceUi
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.model.states.ClimateState
import com.degree.homedash.shared.model.states.FanState
import com.degree.homedash.shared.model.states.LightState

// Sample data for the Living Room @Previews (which live next to their composables in this package).

internal val previewLights: List<LightDeviceUi> = listOf(
    LightDeviceUi(
        metadata = LightMetadata(LivingRoomEntities.LIVING_ROOM_LIGHT_WEST, "Ceiling"),
        state = LightState(LivingRoomEntities.LIVING_ROOM_LIGHT_WEST, isOn = true, isOffline = false),
    ),
    LightDeviceUi(
        metadata = LightMetadata(LivingRoomEntities.LIVING_ROOM_LIGHT_EAST, "Lamp"),
        state = LightState(LivingRoomEntities.LIVING_ROOM_LIGHT_EAST, isOn = false, isOffline = false),
    ),
    LightDeviceUi(
        metadata = LightMetadata(LivingRoomEntities.HOMEWORK_LIGHT, "Accent"),
        state = LightState(LivingRoomEntities.HOMEWORK_LIGHT, isOn = false, isOffline = true),
    ),
)

internal val previewFans: List<FanDeviceUi> = listOf(
    FanDeviceUi(
        metadata = FanMetadata(LivingRoomEntities.LIVING_ROOM_FAN, "Fan", FanMetadata.SpeedAdjustment(12)),
        state = FanState(
            entityId = LivingRoomEntities.LIVING_ROOM_FAN,
            isOn = true,
            isOffline = false,
            percentage = 75,
            isOscillating = false,
            isMisting = false,
        ),
    ),
    FanDeviceUi(
        metadata = FanMetadata(LivingRoomEntities.LIVING_ROOM_BOX_FAN, "Ceiling Fan", FanMetadata.SpeedAdjustment(3)),
        state = FanState(
            entityId = LivingRoomEntities.LIVING_ROOM_BOX_FAN,
            isOn = false,
            isOffline = false,
            percentage = 0,
            isOscillating = false,
            isMisting = false,
        ),
    ),
)

internal val previewClimate: List<ClimateDeviceUi> = listOf(
    ClimateDeviceUi(
        metadata = ClimateMetadata(
            LivingRoomEntities.TEMPERATURE,
            "Temperature",
            ClimateMetadata.ClimateKind.Temperature,
        ),
        state = ClimateState(
            entityId = LivingRoomEntities.TEMPERATURE,
            isOffline = false,
            reading = HistoricalEntityReading(72.5, "°F"),
        ),
    ),
    // The dew point card, as its metadata builds it: the computed value with the humidity underneath.
    ClimateDeviceUi(
        metadata = ClimateMetadata(
            LivingRoomEntities.HUMIDITY,
            "Dew Point",
            ClimateMetadata.ClimateKind.DewPoint,
            dewPointSource = ClimateMetadata.DewPointSource(LivingRoomEntities.TEMPERATURE),
        ),
        state = ClimateState(
            entityId = LivingRoomEntities.HUMIDITY,
            isOffline = false,
            reading = HistoricalEntityReading(50.9, "°F"),
            subvalue = HistoricalEntityReading(48.0, "%"),
        ),
    ),
)
