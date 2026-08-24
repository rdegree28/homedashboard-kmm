package com.degree.homedash.shared.model.entity

/*

 File contains some factories for commonly created triggerentities.

 */

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.testCard(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.testcard",
        displayName = "TestCard",
    ) {
        override fun action() = ServiceCall(domain = "", service = "", entityId = "")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.mainLights(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.main_lights",
        displayName = "Main Lights",
    ) {
        override fun action() = ServiceCall.turnOn("scene.living_room_lights_main_lights")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.movieLights(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.movie_lights",
        displayName = "Movie Lights",
    ) {
        override fun action() = ServiceCall.turnOn("scene.living_room_lights_movie_lights")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.bedroomLightsFull(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_full",
        displayName = "Full Lights",
    ) {
        override fun action() = ServiceCall.turnOn("script.bedroom_lights_full")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.bedroomLightsLow(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_low",
        displayName = "Low Lights",
    ) {
        override fun action() = ServiceCall.turnOn("script.bedroom_lights_low")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerDeviceMetadata.Companion.bedroomLightsNight(): TriggerDeviceMetadata {
    return object : TriggerDeviceMetadata(
        entityId = "trigger.bedroom_lights_night",
        displayName = "Night Lights",
    ) {
        override fun action() = ServiceCall.turnOn("script.bedroom_lights_night")
    }
}

