package com.degree.homedash.core.util

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.roundToInt

/**
 * Spin duration (ms per revolution) scaled by fan speed: 750 ms at top level, 2500 ms at level 1,
 * linear in between. Fans with no speed control (or no reported speed) spin at a steady 1000 ms.
 */
internal fun fanSpinDurationMs(
    percentage: Int,
    levelCount: Int,
    hasSpeedControl: Boolean,
): Int {
    if (!hasSpeedControl || levelCount < 2) return 1000
    val level = (percentage / 100.0 * levelCount).roundToInt().coerceIn(1, levelCount)
    val fraction = (level - 1).toFloat() / (levelCount - 1) // 0 at level 1, 1 at top level
    return (2500f + (750f - 2500f) * fraction).roundToInt()
}

/**
 * Custom fan icon: static outer ring + three swept blades + hub. The blades rotate while [spinning]
 * at one revolution per [durationMs]. Driven per-frame (reading the latest duration each frame) so
 * speed changes apply smoothly — an InfiniteTransition would ignore duration-only changes.
 */
@Composable
internal fun FanIcon(
    spinning: Boolean,
    durationMs: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
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
