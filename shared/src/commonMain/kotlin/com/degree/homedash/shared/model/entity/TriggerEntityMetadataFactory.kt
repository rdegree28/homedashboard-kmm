package com.degree.homedash.shared.model.entity

/*

 File contains some factories for commonly created triggerentities.

 */

/** A card wired to nothing — fires a call with no domain, service or target. */
fun TriggerDeviceMetadata.Companion.testCard(): TriggerDeviceMetadata {
    return TriggerDeviceMetadata(
        entityId = "trigger.testcard",
        displayName = "TestCard",
        targetEntityId = "",
        service = "",
        serviceDomain = "",
    )
}

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
