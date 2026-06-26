package com.degree.homedash.office

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState
import kotlin.math.roundToInt

private val FanOn = Color(0xFF4C8DFF)

/**
 * A fan entity row (spinning fan icon + toggle). If [onSetSpeed] is provided and the fan is on,
 * a stepped speed slider is shown beneath it (reads the `percentage` attribute).
 */
@Composable
fun FanControl(
    name: String,
    entity: EntityState?,
    onSetSpeed: ((Int) -> Unit)? = null,
    onToggle: () -> Unit,
) {
    val spinDurationMs = fanSpinDurationMs(entity, hasSpeedControl = onSetSpeed != null)
    Column {
        EntityToggleRow(name, entity, FanOn, onToggle) { tint ->
            FanIcon(
                spinning = entity?.isOn == true,
                durationMs = spinDurationMs,
                tint = tint,
                modifier = Modifier.size(26.dp),
            )
        }
        if (onSetSpeed != null && entity?.isOn == true) {
            // Number of speed levels from the fan's reported percentage_step (e.g. 8.33% -> 12).
            val stepPct = entity.attrDouble("percentage_step")
            val levelCount = if (stepPct != null && stepPct > 0.0) (100.0 / stepPct).roundToInt() else 0
            if (levelCount >= 2) {
                FanSpeedSlider(
                    percentage = entity.attrDouble("percentage")?.roundToInt() ?: 0,
                    levelCount = levelCount,
                    onSet = onSetSpeed,
                )
            }
        }
    }
}

/**
 * Speed slider that works in integer levels (0..[levelCount]) so every step is reachable.
 * On release it sends the MIDPOINT percentage of the chosen level's band, which maps cleanly
 * onto that level under HA's ceil-based percentage→speed conversion (no dead/duplicate steps).
 */
@Composable
private fun FanSpeedSlider(percentage: Int, levelCount: Int, onSet: (Int) -> Unit) {
    val currentLevel = (percentage / 100f * levelCount).roundToInt().coerceIn(0, levelCount)
    var level by remember(currentLevel) { mutableStateOf(currentLevel.toFloat()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 42.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Slider(
            value = level,
            onValueChange = { level = it },
            onValueChangeFinished = {
                val k = level.roundToInt()
                if (k >= 1) {
                    onSet(((k - 0.5) * 100.0 / levelCount).roundToInt())
                } else {
                    level = currentLevel.toFloat() // show the 0 stop, but ignore setting 0%
                }
            },
            valueRange = 0f..levelCount.toFloat(),
            steps = (levelCount - 1).coerceAtLeast(0),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${level.roundToInt()} / $levelCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Spin duration (ms per revolution) scaled by fan speed: 750 ms at top level, 2500 ms at level 1,
 * linear in between. Fans with no speed control (or no reported speed) spin at a steady 1000 ms.
 */
private fun fanSpinDurationMs(entity: EntityState?, hasSpeedControl: Boolean): Int {
    val stepPct = entity?.attrDouble("percentage_step")
    val pct = entity?.attrDouble("percentage")
    if (!hasSpeedControl || stepPct == null || stepPct <= 0.0 || pct == null) return 1000
    val levelCount = (100.0 / stepPct).roundToInt()
    if (levelCount < 2) return 1000
    val level = (pct / 100.0 * levelCount).roundToInt().coerceIn(1, levelCount)
    val fraction = (level - 1).toFloat() / (levelCount - 1) // 0 at level 1, 1 at top level
    return (2500f + (750f - 2500f) * fraction).roundToInt()
}

/**
 * Custom fan icon: static outer ring + three swept blades + hub. The blades rotate while [spinning]
 * at one revolution per [durationMs]. Driven per-frame (reading the latest duration each frame) so
 * speed changes apply smoothly — an InfiniteTransition would ignore duration-only changes.
 */
@Composable
private fun FanIcon(spinning: Boolean, durationMs: Int, tint: Color, modifier: Modifier = Modifier) {
    val duration by rememberUpdatedState(durationMs.coerceAtLeast(1))
    val on by rememberUpdatedState(spinning)
    var angle by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameMillis { now ->
                if (last != 0L && on) {
                    angle = (angle + 360f * (now - last) / duration) % 360f
                }
                last = now
            }
        }
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val ringStroke = w * 0.07f
        val ringR = minOf(w, h) / 2f - ringStroke / 2f

        drawCircle(color = tint, radius = ringR, center = Offset(cx, cy), style = Stroke(width = ringStroke))

        val r = ringR * 0.80f
        val blade = Path().apply {
            moveTo(cx, cy)
            cubicTo(
                cx + r * 0.36f, cy - r * 0.06f,
                cx + r * 0.30f, cy - r * 0.66f,
                cx + r * 0.05f, cy - r * 0.95f,
            )
            cubicTo(
                cx - r * 0.30f, cy - r * 0.72f,
                cx - r * 0.16f, cy - r * 0.24f,
                cx, cy,
            )
            close()
        }

        for (i in 0 until 3) {
            rotate(degrees = angle + i * 120f, pivot = Offset(cx, cy)) {
                drawPath(blade, color = tint)
            }
        }

        drawCircle(color = tint, radius = w * 0.08f, center = Offset(cx, cy))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun FanControlPreview() = ControlPreview {
    FanControl("On", previewEntity("on")) {}
    FanControl("With speed", previewFan(percentage = 75), onSetSpeed = {}) {}
    FanControl("Off", previewEntity("off")) {}
    FanControl("Offline", previewEntity("unavailable")) {}
}
