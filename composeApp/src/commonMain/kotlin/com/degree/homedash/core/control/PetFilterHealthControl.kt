package com.degree.homedash.core.control

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.degree.homedash.core.device.PetFountainDeviceUi
import com.degree.homedash.ui.AppColors

/**
 * How much life the fountain's filter has left: the days its own sensor reports, drawn against the
 * full cycle as a bar.
 *
 * Sits under [WaterLevelControl] on the Pets screen and shares its layout — same row, same bar. Not
 * tappable: unlike the water level there's no history worth graphing, since this is a countdown that
 * only ever ticks down and resets.
 */
@Composable
fun PetFilterHealthControl(ui: PetFountainDeviceUi) {
    val maxDays = ui.filterMaxDays
    val days = ui.filterDaysRemaining

    LevelBarRow(
        label = "Filter",
        valueText = filterValueText(days),
        fraction = if (days != null && maxDays != null && maxDays > 0) {
            (days.toFloat() / maxDays.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        },
        barColor = filterColor(days, maxDays),
    )
}

/** "26 days" while there's life left, and something that reads as an instruction once there isn't. */
private fun filterValueText(days: Int?): String = when {
    days == null -> "—"
    days <= 0 -> "Due"
    days == 1 -> "1 day"
    else -> "$days days"
}

/**
 * Filter status color: green above a tenth of the cycle, amber through the last few days, red once
 * it's due. Gray when there's no reading, matching [waterLevelColor].
 *
 * The band is deliberately narrow — a filter that's a week old is no worse than one fitted
 * yesterday, so the bar stays green until replacing it is actually near.
 */
private fun filterColor(daysRemaining: Int?, maxDays: Int?): Color = when {
    daysRemaining == null || maxDays == null || maxDays <= 0 -> AppColors.StatusGray
    daysRemaining.toFloat() / maxDays.toFloat() > 0.1f -> AppColors.StatusGreen
    daysRemaining > 0 -> AppColors.StatusAmber
    else -> AppColors.StatusRed
}
