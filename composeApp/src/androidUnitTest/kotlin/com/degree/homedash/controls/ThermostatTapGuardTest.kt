package com.degree.homedash.controls

import com.degree.homedash.core.util.ThermostatTapGuard
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

/**
 * The window is the whole point of the guard and the one part of it that can't be seen by tapping
 * around the card — nobody is going to sit in front of a tablet for fifteen minutes to check it
 * expires.
 */
class ThermostatTapGuardTest {

    private val clock = TestTimeSource()
    private val guard = ThermostatTapGuard(window = 15.minutes, timeSource = clock)

    @Test
    fun `asks before anything has been confirmed`() {
        assertFalse(guard.isConfirmed("climate.living_room_thermostat"))
    }

    @Test
    fun `stops asking for the length of the window`() {
        guard.confirm("climate.living_room_thermostat")

        clock += 14.minutes + 59.seconds
        assertTrue(guard.isConfirmed("climate.living_room_thermostat"))
    }

    @Test
    fun `asks again once the window is up`() {
        guard.confirm("climate.living_room_thermostat")

        clock += 15.minutes
        assertFalse(guard.isConfirmed("climate.living_room_thermostat"))
    }

    /** The window runs from the confirmation; a later action inside it must not push the end out. */
    @Test
    fun `later actions inside the window do not extend it`() {
        guard.confirm("climate.living_room_thermostat")

        clock += 10.minutes
        assertTrue(guard.isConfirmed("climate.living_room_thermostat"))
        clock += 5.minutes
        assertFalse(guard.isConfirmed("climate.living_room_thermostat"))
    }

    /** Confirming one thermostat says nothing about the one on the next card. */
    @Test
    fun `confirmation does not carry to another thermostat`() {
        guard.confirm("climate.living_room_thermostat")

        assertFalse(guard.isConfirmed("climate.office_heater"))
    }

    @Test
    fun `confirming again reopens an expired window`() {
        guard.confirm("climate.living_room_thermostat")
        clock += 20.minutes

        guard.confirm("climate.living_room_thermostat")
        assertTrue(guard.isConfirmed("climate.living_room_thermostat"))
    }
}
