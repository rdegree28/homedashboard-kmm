package com.degree.homedash.livingroom

import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi

// Sample data for the Living Room @Previews (which live next to their composables in this package).

internal val previewLights: List<EntityUi.Light> = listOf(
    EntityUi.Light(
        metadata = EntityMetadata.Light(LivingRoomEntities.LIVING_ROOM_LIGHT_WEST),
        name = "Ceiling",
        isOn = true,
        offline = false,
    ),
    EntityUi.Light(
        metadata = EntityMetadata.Light(LivingRoomEntities.LIVING_ROOM_LIGHT_EAST),
        name = "Lamp",
        isOn = false,
        offline = false,
    ),
    EntityUi.Light(
        metadata = EntityMetadata.Light(LivingRoomEntities.HOMEWORK_LIGHT),
        name = "Accent",
        isOn = false,
        offline = true,
    ),
)

internal val previewFans: List<EntityUi.Fan> = listOf(
    EntityUi.Fan(
        metadata = EntityMetadata.Fan(LivingRoomEntities.LIVING_ROOM_FAN, levelCount = 12),
        name = "Fan",
        isOn = true,
        offline = false,
        percentage = 75,
    ),
    EntityUi.Fan(
        metadata = EntityMetadata.Fan(LivingRoomEntities.LIVING_ROOM_BOX_FAN, levelCount = 3),
        name = "Ceiling Fan",
        isOn = false,
        offline = false,
        percentage = 0,
    ),
)
