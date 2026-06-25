package com.degree.homedash.office

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState

private val FanOn = Color(0xFF4C8DFF)

/** A fan entity row: a hand-drawn fan whose blades spin while the fan is on. */
@Composable
fun FanControl(
    name: String,
    entity: EntityState?,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, FanOn, onToggle) { tint ->
    FanIcon(spinning = entity?.isOn == true, tint = tint, modifier = Modifier.size(26.dp))
}

/** Custom fan icon: static outer ring + three swept blades (rotating while [spinning]) + hub. */
@Composable
private fun FanIcon(spinning: Boolean, tint: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "fan")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (spinning) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fanAngle",
    )
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
