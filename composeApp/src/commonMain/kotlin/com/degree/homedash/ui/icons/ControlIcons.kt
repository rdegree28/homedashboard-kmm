package com.degree.homedash.ui.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors

/** Hand-drawn glyphs for entity controls, on the same grid and weight as [RoomIcons]. */
object ControlIcons {

    /**
     * A setpoint dial: an open gauge arc with a needle and pivot.
     *
     * Deliberately not a thermometer — the card this sits on *sets* a target, it doesn't report a
     * reading, and the two thermometer-icon sensor cards sit directly beneath it. Deliberately not a
     * closed ring with a hub either: that is `FanIcon`, and Fans is the group immediately above
     * Climate on the Living Room screen. The gap at the bottom and the single needle keep the two
     * apart at the 26dp the rows draw them.
     */
    val Thermostat: ImageVector by lazy {
        iconVector("Thermostat") {
            // Gauge arc: radius 8.5 about (12, 12.5), sweeping 260° clockwise from lower-left over
            // the top to lower-right, leaving a 100° gap at the bottom. Stroked rather than filled
            // (unlike the RoomIcons silhouettes) so the arc stays an even weight all the way round.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.3f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(5.49f, 17.97f)
                // rx, ry, rotation, largeArc (260° > 180°), clockwise, endpoint
                arcTo(8.5f, 8.5f, 0f, true, true, 18.51f, 17.97f)
            }
            // Needle, pointing up and to the right — a dial set somewhere above its minimum.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(12f, 12.5f)
                lineTo(14.6f, 8.0f)
            }
            // Pivot, drawn last so the needle emerges from it cleanly.
            path(fill = SolidColor(Color.Black)) { circle(12f, 12.5f, 2.4f) }
        }
    }

    /**
     * The HVAC mode glyphs — the universal thermostat set, so they read without their labels.
     *
     * Stroked rather than filled (the flame excepted), which keeps them legible at the 18dp the mode
     * pills draw them, where a filled silhouette would close up into a blob.
     */

    /** Power symbol: a ring broken at the top with a bar through the gap. */
    val HvacOff: ImageVector by lazy {
        iconVector("HvacOff") {
            // Radius 7 about (12, 12.5), swept 290° clockwise so the 70° gap lands at the top.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(16.02f, 6.77f)
                arcTo(7f, 7f, 0f, true, true, 7.98f, 6.77f)
            }
            // The bar overshoots the ring's top edge, which is what makes it read as a power symbol
            // rather than a broken circle.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(12f, 4.2f)
                lineTo(12f, 11.6f)
            }
        }
    }

    /** A flame. Filled, with the left side kinked inward so it doesn't read as a plain teardrop. */
    val HvacHeat: ImageVector by lazy {
        iconVector("HvacHeat") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.8f)
                // Right side: out to the widest point, then in to the base.
                curveTo(12f, 7.2f, 17.8f, 9.4f, 17.8f, 14.2f)
                curveTo(17.8f, 17.8f, 15.2f, 20.9f, 12f, 20.9f)
                curveTo(8.8f, 20.9f, 6.2f, 17.8f, 6.2f, 14.2f)
                // Left side back to the tip, pinched in at the shoulder for the flicker.
                curveTo(6.2f, 11.4f, 8.6f, 10.4f, 9.6f, 8.2f)
                curveTo(10.5f, 6.2f, 10.6f, 4.4f, 12f, 2.8f)
                close()
            }
        }
    }

    /** A six-armed snowflake: three arms crossing at the centre, each tipped with a pair of barbs. */
    val HvacCool: ImageVector by lazy {
        iconVector("HvacCool") {
            // Three full-length arms through (12, 12) at 90°, 30° and 150°.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(12f, 4f); lineTo(12f, 20f)
                moveTo(18.93f, 8f); lineTo(5.07f, 16f)
                moveTo(18.93f, 16f); lineTo(5.07f, 8f)
            }
            // Barbs: a shallow V on each of the six arms, thinner than the arms so the star still
            // reads as the dominant shape at small sizes.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(10.09f, 5.79f); lineTo(12f, 7.4f); lineTo(13.92f, 5.79f)
                moveTo(13.92f, 18.21f); lineTo(12f, 16.6f); lineTo(10.09f, 18.21f)
                moveTo(16.42f, 7.24f); lineTo(15.98f, 9.7f); lineTo(18.33f, 10.56f)
                moveTo(7.59f, 16.76f); lineTo(8.02f, 14.3f); lineTo(5.67f, 13.44f)
                moveTo(18.33f, 13.44f); lineTo(15.98f, 14.3f); lineTo(16.42f, 16.76f)
                moveTo(5.67f, 10.56f); lineTo(8.02f, 9.7f); lineTo(7.59f, 7.24f)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun ControlIconsPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = AppColors.CardBackground) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // The sizes it actually renders at, plus a blown-up one for drawing work.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(ControlIcons.Thermostat, null, Modifier.size(20.dp), AppColors.TempCool)
                    Icon(ControlIcons.Thermostat, null, Modifier.size(26.dp), AppColors.TempCool)
                    Icon(ControlIcons.Thermostat, null, Modifier.size(26.dp), AppColors.TempWarm)
                    Icon(ControlIcons.Thermostat, null, Modifier.size(26.dp), AppColors.StatusGray)
                }
                // The mode glyphs at the 18dp the pills draw them, in their own tones.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(ControlIcons.HvacOff, null, Modifier.size(18.dp), AppColors.StatusGray)
                    Icon(ControlIcons.HvacHeat, null, Modifier.size(18.dp), AppColors.TempWarm)
                    Icon(ControlIcons.HvacCool, null, Modifier.size(18.dp), AppColors.TempCool)
                }
                // Blown up for drawing work.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(ControlIcons.Thermostat, null, Modifier.size(80.dp), AppColors.TempCool)
                    Icon(ControlIcons.HvacOff, null, Modifier.size(80.dp), AppColors.StatusGray)
                    Icon(ControlIcons.HvacHeat, null, Modifier.size(80.dp), AppColors.TempWarm)
                    Icon(ControlIcons.HvacCool, null, Modifier.size(80.dp), AppColors.TempCool)
                }
            }
        }
    }
}
