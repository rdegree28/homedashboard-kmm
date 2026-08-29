package com.degree.homedash.core.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * How long one confirmation covers a thermostat for. Long enough to settle on a temperature without
 * being asked twice, short enough that a tablet left on the Living Room screen is guarded again well
 * before anyone walks past it.
 */
val ThermostatConfirmationWindow: Duration = 15.minutes

/**
 * Remembers which thermostats the user has recently confirmed they meant to touch, so the card can
 * ask once and then get out of the way.
 *
 * Per entity rather than globally: confirming that you meant to change the living room thermostat
 * says nothing about a knee brushing the office heater's card a minute later.
 *
 * The window runs from the confirmation and does *not* slide with each subsequent action — an
 * accidental tap inside the window would otherwise extend the very gap it should be caught by.
 *
 * Deliberately not Compose state. Nothing composes off it; it is read inside click handlers, at
 * which point the elapsed time is computed fresh. That also means no timer has to run to expire a
 * window, and no recomposition happens when one does.
 */
class ThermostatTapGuard(
    private val window: Duration = ThermostatConfirmationWindow,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    /** Monotonic, so a clock correction or a DST jump can't widen or shorten a window. */
    private val confirmedAt = mutableMapOf<String, TimeMark>()

    /** True while [entityId]'s confirmation is still in force, so an action can run unprompted. */
    fun isConfirmed(entityId: String): Boolean =
        confirmedAt[entityId]?.elapsedNow()?.let { it < window } == true

    fun confirm(entityId: String) {
        confirmedAt[entityId] = timeSource.markNow()
    }
}

/**
 * The instance the cards use. Process-wide rather than remembered per card, so leaving the screen and
 * coming back inside the window doesn't ask again — the user's answer was about the thermostat, not
 * about that particular visit to the screen.
 *
 * Single-threaded by construction: only tap handlers on the main thread touch it.
 */
internal val AppThermostatTapGuard = ThermostatTapGuard()
