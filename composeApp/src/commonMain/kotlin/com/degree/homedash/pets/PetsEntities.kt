package com.degree.homedash.pets

/** Home Assistant entity ids shown on the Pets dashboard. */
object PetsEntities {
    const val CAT_WATER_LEVEL = "sensor.cat_water_fountain_remaining_water_pct"

    /** Days left on the fountain's filter — its own sensor, not an attribute of the level above. */
    const val CAT_FILTER_DAYS = "sensor.cat_water_fountain_remaining_filter_day"
}
