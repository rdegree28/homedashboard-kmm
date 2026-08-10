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
import androidx.compose.ui.graphics.PathFillType
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

    /**
     * The temperature-preset glyphs. These pills carry no label, so each has to stand alone —
     * house / moon / leaf is the conventional set, which is most of why it reads.
     */

    /** Comfort: a house — the normal, at-home setting. */
    val PresetComfort: ImageVector by lazy {
        iconVector("PresetComfort") {
            path(fill = SolidColor(Color.Black)) {
                // Roof: a broad triangle, eaves overhanging the walls either side.
                moveTo(12f, 3.4f)
                lineTo(22f, 11.6f)
                lineTo(20.2f, 13.8f)
                lineTo(12f, 7.1f)
                lineTo(3.8f, 13.8f)
                lineTo(2f, 11.6f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                // Walls, with a doorway cut out of the base rather than drawn over it, so the
                // silhouette still reads as a house when it's filled with a single colour.
                moveTo(5.2f, 12.9f)
                lineTo(12f, 7.3f)
                lineTo(18.8f, 12.9f)
                lineTo(18.8f, 20.6f)
                lineTo(14.1f, 20.6f)
                lineTo(14.1f, 15.2f)
                lineTo(9.9f, 15.2f)
                lineTo(9.9f, 20.6f)
                lineTo(5.2f, 20.6f)
                close()
            }
        }
    }

    /**
     * Sleep: a crescent moon, drawn as two arcs between the same pair of horns — a deep one out to
     * the left for the outer edge, a shallower one back for the inner.
     *
     * Not two circles in an even-odd path: even-odd is a symmetric difference, not a subtraction, so
     * the part of the biting circle hanging outside the disc survives and the result is a blob
     * rather than a crescent.
     */
    val PresetSleep: ImageVector by lazy {
        iconVector("PresetSleep") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14.5f, 3.2f)
                // Outer edge: radius 9, bulging left, down to the lower horn.
                arcTo(9f, 9f, 0f, false, false, 10.2f, 20.6f)
                // Inner edge: radius 11, so it cuts back across more gently than the outer edge.
                arcTo(11f, 11f, 0f, false, true, 14.5f, 3.2f)
                close()
            }
        }
    }

    /**
     * Economy: a leaf with a stem and central vein.
     *
     * Stroked rather than filled, which is what leaves room for the vein — on a filled blade the
     * vein would have to be a hole, and at 18dp a hairline hole closes up.
     */
    val PresetEconomy: ImageVector by lazy {
        iconVector("PresetEconomy") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
            ) {
                // Blade: a pointed oval leaning up-right, tip at top-right, base at bottom-left.
                moveTo(19.8f, 4.2f)
                curveTo(8.8f, 3.4f, 4.6f, 9.6f, 5.8f, 14.6f)
                curveTo(6.8f, 18.8f, 12.4f, 20.6f, 16f, 17.4f)
                curveTo(19.4f, 14.4f, 20.4f, 9.4f, 19.8f, 4.2f)
                close()
            }
            // Stem running out past the base, with the vein continuing the same line up the blade.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(3.4f, 20.8f)
                lineTo(15.2f, 9f)
            }
        }
    }

    /**
     * Extreme: a lightning bolt with a slash cut through it — cutting power, not boosting it.
     *
     * The slash carries the whole meaning: a bare bolt reads as "boost", the opposite of what this
     * does. The bolt and a wide diagonal bar share one even-odd path so their overlap punches a real
     * gap, and the thinner slash then sits inside that gap instead of merging into the bolt.
     */
    val PresetExtreme: ImageVector by lazy {
        iconVector("PresetExtreme") {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                moveTo(13.6f, 2.2f)
                lineTo(5.4f, 13.2f)
                lineTo(10.8f, 13.2f)
                lineTo(10.4f, 21.8f)
                lineTo(18.6f, 10.6f)
                lineTo(13.2f, 10.6f)
                close()
                // The gap: a 4.4-wide bar along the slash, overshooting both ends so it cuts clean.
                // Runs "\", across the bolt rather than along it — a slash on the other diagonal
                // lies almost parallel to the bolt and shreds it into slivers instead of cutting it.
                moveTo(1.53f, 4.65f)
                lineTo(4.65f, 1.53f)
                lineTo(22.47f, 19.35f)
                lineTo(19.35f, 22.47f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(4.5f, 4.5f)
                lineTo(19.5f, 19.5f)
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
                // The preset glyphs at 18dp, where they carry no label to lean on.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(ControlIcons.PresetComfort, null, Modifier.size(18.dp), AppColors.TempWarm)
                    Icon(ControlIcons.PresetSleep, null, Modifier.size(18.dp), AppColors.PlayBlue)
                    Icon(ControlIcons.PresetEconomy, null, Modifier.size(18.dp), AppColors.Healthy)
                    Icon(ControlIcons.PresetExtreme, null, Modifier.size(18.dp), AppColors.StatusAmber)
                }
                // Blown up for drawing work.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(ControlIcons.Thermostat, null, Modifier.size(72.dp), AppColors.TempCool)
                    Icon(ControlIcons.PresetComfort, null, Modifier.size(72.dp), AppColors.TempWarm)
                    Icon(ControlIcons.PresetSleep, null, Modifier.size(72.dp), AppColors.PlayBlue)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(ControlIcons.PresetEconomy, null, Modifier.size(72.dp), AppColors.Healthy)
                    Icon(ControlIcons.PresetExtreme, null, Modifier.size(72.dp), AppColors.StatusAmber)
                    Icon(ControlIcons.HvacCool, null, Modifier.size(72.dp), AppColors.TempCool)
                }
            }
        }
    }
}
