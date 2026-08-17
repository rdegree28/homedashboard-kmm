package com.degree.homedash.pets

import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.model.HistoryPoint

// Sample data for the Pets @Previews (which live next to their composables in this package).

private val previewFountainMetadata = WaterLevelMetadata(
    PetsEntities.CAT_WATER_LEVEL,
    "Remaining Water",
    filterHealth = WaterLevelMetadata.FilterHealth(PetsEntities.CAT_FILTER_DAYS, maxDays = 31),
)

internal val previewLevels: List<EntityUi.WaterLevel> = listOf(
    EntityUi.WaterLevel(
        metadata = previewFountainMetadata,
        pct = 68.0,
        valueText = "68 %",
        filterDaysRemaining = 26,
    ),
)

/** A fountain near both limits, so the preview shows the amber end of both bars beside the green. */
internal val previewLevelsLow: List<EntityUi.WaterLevel> = listOf(
    EntityUi.WaterLevel(
        metadata = previewFountainMetadata,
        pct = 38.0,
        valueText = "38 %",
        filterDaysRemaining = 2,
    ),
)

// A day of samples: a stepped drain (drink, rest, drink, rest…) from full to empty, then a refill.
internal val previewLevelHistory: List<HistoryPoint> =
    List(48) { i ->
        val value = if (i < 44) {
            val cycle = 11 // points per drink-then-rest cycle
            val slope = 6 // points spent draining within a cycle; the rest is a plateau
            val base = 100.0 - (i / cycle) * 25.0 // level at the top of the current step
            val drop = if (i % cycle < slope) (i % cycle) / slope.toDouble() * 25.0 else 25.0
            (base - drop).coerceAtLeast(0.0)
        } else {
            100.0 // refill
        }
        HistoryPoint(timeSeconds = i * 1800.0, value = value)
    }
