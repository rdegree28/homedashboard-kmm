package com.degree.homedash.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Recurring layout dimensions shared across dashboards. */
object Dimens {
    val TwoRowEntityCardHeight: Dp = 140.dp
    val EntityCardHeight: Dp = 100.dp
    val SmallEntityCardHeight: Dp = 60.dp
    val EntityCardPadding: Dp = 14.dp
    val CardCorner: Dp = 12.dp
    val CardElevation: Dp = 1.dp
    val EntityRowHeight: Dp = 52.dp
    val RowIconSize: Dp = 26.dp
    val RowLabelGap: Dp = 16.dp
    val ChartHeight: Dp = 150.dp
    val SectionSpacing: Dp = 16.dp

    /**
     * Breathing room under the last section of a scrolling dashboard. Deliberately larger than the
     * 16dp side padding: on web there is no system navigation bar to stand in for it, and on mobile
     * it sits on top of the bottom safe-drawing inset applied at the app root.
     */
    val ScrollBottomPadding: Dp = 40.dp
}
