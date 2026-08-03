package com.degree.homedash.shared.model.entity

/*

 File contains some factories for commonly created triggerentities.

 */

/** Living Room "Main Lights" scene. */
fun TriggerEntityMetadata.Companion.testCard(): TriggerEntityMetadata {
    return object : TriggerEntityMetadata(
        entityId = "trigger.testcard",
        displayName = "TestCard",
    ) {
        override fun action() = ServiceCall(domain = "", service = "", entityId = "")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerEntityMetadata.Companion.mainLights(): TriggerEntityMetadata {
    return object : TriggerEntityMetadata(
        entityId = "trigger.main_lights",
        displayName = "Main Lights",
    ) {
        override fun action() = ServiceCall.turnOn("scene.living_room_lights_main_lights")
    }
}

/** Living Room "Main Lights" scene. */
fun TriggerEntityMetadata.Companion.movieLights(): TriggerEntityMetadata {
    return object : TriggerEntityMetadata(
        entityId = "trigger.movie_lights",
        displayName = "Movie Lights",
    ) {
        override fun action() = ServiceCall.turnOn("scene.living_room_lights_movie_lights")
    }
}

