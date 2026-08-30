package com.degree.homedash.shared.model

import com.degree.homedash.shared.model.device_metadata.TriggerDeviceMetadata
import com.degree.homedash.shared.repo.FakeExpHomeAssistantApi
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A trigger card describes its own service call. Getting the domain wrong is a card that looks fine
 * and does nothing, which nothing else in the app would catch.
 */
class TriggerDeviceMetadataTest {

    @Test
    fun aSceneCardTurnsOnItsSceneWithoutBeingTold() {
        val scene = TriggerDeviceMetadata(
            entityId = "trigger.movie_lights",
            displayName = "Movie Lights",
            targetEntityId = "scene.living_room_movie_lights",
        )

        // turn_on activates scenes, scripts and automations alike, and the domain is the target's own.
        assertEquals("turn_on", scene.service)
        assertEquals("scene", scene.serviceDomain)
    }

    @Test
    fun aScriptCardDerivesTheScriptDomain() {
        val script = TriggerDeviceMetadata(
            entityId = "trigger.bedroom_full",
            displayName = "Full Lights",
            targetEntityId = "script.bedroom_lights_full",
        )

        assertEquals("script", script.serviceDomain)
    }

    @Test
    fun aTriggerCanNameSomeOtherServiceOutright() {
        val odd = TriggerDeviceMetadata(
            entityId = "trigger.odd",
            displayName = "Odd",
            targetEntityId = "script.odd",
            service = "toggle",
            serviceDomain = "homeassistant",
        )

        assertEquals("toggle", odd.service)
        assertEquals("homeassistant", odd.serviceDomain)
    }

    @Test
    fun pressingItFiresTheCallAtTheTargetNotTheCard() {
        val api = FakeExpHomeAssistantApi()
        val scene = TriggerDeviceMetadata(
            entityId = "trigger.movie_lights",
            displayName = "Movie Lights",
            targetEntityId = "scene.living_room_movie_lights",
        )

        scene.onActivate(HomeAssistantRepo(api))

        // The synthetic trigger.* id is a list key, never something Home Assistant is asked about.
        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall("scene", "turn_on", "scene.living_room_movie_lights", null),
            api.serviceCalls.single(),
        )
    }
}
