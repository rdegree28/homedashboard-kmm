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
                Icon(ControlIcons.Thermostat, null, Modifier.size(96.dp), AppColors.TempCool)
            }
        }
    }
}
