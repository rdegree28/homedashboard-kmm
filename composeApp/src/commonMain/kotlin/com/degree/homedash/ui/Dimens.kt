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
    /** A thermostat card's header + setpoint stepper, before any selector rows are added. */
    val ThermostatCardHeight: Dp = 132.dp

    /** Each mode/fan/preset pill row a thermostat card adds on top of [ThermostatCardHeight]. */
    val ThermostatSelectorRowHeight: Dp = 40.dp

    /** The −/+ setpoint buttons — sized to stay hittable on the wall tablet. */
    val StepperButtonSize: Dp = 44.dp

    val EntityRowHeight: Dp = 52.dp
    val RowIconSize: Dp = 26.dp
    val RowLabelGap: Dp = 16.dp
    val ChartHeight: Dp = 150.dp
    val SectionSpacing: Dp = 16.dp
}
