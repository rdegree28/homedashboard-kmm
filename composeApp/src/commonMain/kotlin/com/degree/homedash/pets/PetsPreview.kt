package com.degree.homedash.pets

import com.degree.homedash.core.device.PetFountainDeviceUi
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.device_metadata.PetFountainMetadata
import com.degree.homedash.shared.model.device_state.PetFilterState

// Sample data for the Pets @Previews (which live next to their composables in this package).

private val previewFountainMetadata = PetFountainMetadata(
    PetsEntities.CAT_WATER_LEVEL,
    "Remaining Water",
    filterHealth = PetFountainMetadata.FilterHealth(PetsEntities.CAT_FILTER_DAYS, maxDays = 31),
)

internal val previewLevels: List<PetFountainDeviceUi> = listOf(previewFountain(pct = 68.0, filterDays = 26))

/** A fountain near both limits, so the preview shows the amber end of both bars beside the green. */
internal val previewLevelsLow: List<PetFountainDeviceUi> = listOf(previewFountain(pct = 38.0, filterDays = 2))

private fun previewFountain(pct: Double, filterDays: Int) = PetFountainDeviceUi(
    metadata = previewFountainMetadata,
    state = PetFilterState(
        entityId = PetsEntities.CAT_WATER_LEVEL,
        isOffline = false,
        waterLevel = HistoricalEntityReading(pct, "%"),
        filterDaysRemaining = filterDays,
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
