package com.degree.homedash

import com.degree.homedash.pets.PetsEntities
import com.degree.homedash.shared.model.EntityState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The launcher's medication card is driven entirely by two Home Assistant helpers, and which dose it
 * names is the part that can't be checked by looking at the screen — seeing the evening wording
 * would mean waiting for the evening, with the morning dose already given.
 */
class CatMedicationWarningTest {

    private fun states(am: Boolean, pm: Boolean): Map<String, EntityState> = mapOf(
        PetsEntities.CAT_MEDICATION_AM_REMINDER to
            EntityState(PetsEntities.CAT_MEDICATION_AM_REMINDER, if (am) "on" else "off"),
        PetsEntities.CAT_MEDICATION_PM_REMINDER to
            EntityState(PetsEntities.CAT_MEDICATION_PM_REMINDER, if (pm) "on" else "off"),
    )

    @Test
    fun `no card while neither dose is due`() {
        assertNull(catMedicationWarning(states(am = false, pm = false)))
    }

    @Test
    fun `names the morning dose`() {
        assertEquals("Cat needs her morning pill", catMedicationWarning(states(am = true, pm = false))?.message)
    }

    @Test
    fun `names the evening dose`() {
        assertEquals("Cat needs her evening pill", catMedicationWarning(states(am = false, pm = true))?.message)
    }

    @Test
    fun `both overdue doses share one card`() {
        val warning = catMedicationWarning(states(am = true, pm = true))

        assertEquals("Cat needs her morning and evening pill", warning?.message)
    }

    @Test
    fun `a due dose is a notification, not a warning`() {
        // Nothing is wrong when a dose is due — it just needs doing, so this stays off the amber/red scale.
        assertEquals(WarningSeverity.Notification, catMedicationWarning(states(am = true, pm = false))?.severity)
    }

    @Test
    fun `missing helpers read as nothing due`() {
        // The app shouldn't invent a reminder if the helpers are renamed or removed in HA.
        assertNull(catMedicationWarning(emptyMap()))
    }
}
