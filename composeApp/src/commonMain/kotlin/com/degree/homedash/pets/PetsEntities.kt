package com.degree.homedash.pets

/** Home Assistant entity ids shown on the Pets dashboard. */
object PetsEntities {
    const val CAT_WATER_LEVEL = "sensor.cat_water_fountain_remaining_water_pct"

    /** Days left on the fountain's filter — its own sensor, not an attribute of the level above. */
    const val CAT_FILTER_DAYS = "sensor.cat_water_fountain_remaining_filter_day"

    /**
     * The cat's medication reminders. Home Assistant's automations turn each helper on when that
     * dose comes due and off again once it's marked given, so these read as "this dose is still
     * owed" — the app only ever watches them.
     */
    const val CAT_MEDICATION_AM_REMINDER = "input_boolean.cat_medication_am_reminder_active"
    const val CAT_MEDICATION_PM_REMINDER = "input_boolean.cat_medication_pm_reminder_active"
}
