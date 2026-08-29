package com.degree.homedash.shared.model.device_metadata.factories

import com.degree.homedash.shared.model.device_metadata.TriggerDeviceMetadata

/*

 File contains some factories for commonly created triggerentities.

 */

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.mainLights(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.main_lights",
        displayName = "Main Lights",
        targetEntityId = "scene.living_room_lights_main_lights",
    )
}

/** Living Room "Movie Lights" scene. */
fun TriggerDeviceMetadata.Companion.movieLights(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.movie_lights",
        displayName = "Movie Lights",
        targetEntityId = "scene.living_room_lights_movie_lights",
    )
}

/** Bedroom "Full Lights" script. */
fun TriggerDeviceMetadata.Companion.bedroomLightsFull(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_full",
        displayName = "Full Lights",
        targetEntityId = "script.bedroom_lights_full",
    )
}

/** Bedroom "Low Lights" script. */
fun TriggerDeviceMetadata.Companion.bedroomLightsLow(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_low",
        displayName = "Low Lights",
        targetEntityId = "script.bedroom_lights_low",
    )
}

/** Bedroom "Night Lights" script. */
fun TriggerDeviceMetadata.Companion.bedroomLightsNight(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_night",
        displayName = "Night Lights",
        targetEntityId = "script.bedroom_lights_night",
    )
}
