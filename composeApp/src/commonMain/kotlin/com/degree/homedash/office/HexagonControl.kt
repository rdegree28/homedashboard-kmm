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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A hexagon-lights row: three hexagons (two on top, one below); white + faint glow while on. */
@Composable
fun HexagonControl(
    name: String,
    entity: EntityState?,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, Color.White, onToggle) { tint ->
    HexagonIcon(on = entity?.isOn == true, tint = tint, modifier = Modifier.size(26.dp))
}

/** Three stacked hexagons with the same breathing glow the lights use (white). */
@Composable
private fun HexagonIcon(on: Boolean, tint: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hex")
    val glow by transition.animateFloat(
        initialValue = if (on) 0.18f else 0f,
        targetValue = if (on) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hexGlow",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        if (on) {
            val rad = minOf(w, h) / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = glow), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = rad,
                ),
                radius = rad,
                center = Offset(cx, cy),
            )
        }

        val r = w * 0.18f
        val stroke = w * 0.05f
        drawPath(hexagonPath(cx - r * 0.95f, cy - r * 0.52f, r), color = tint, style = Stroke(stroke))
        drawPath(hexagonPath(cx + r * 0.95f, cy - r * 0.52f, r), color = tint, style = Stroke(stroke))
        drawPath(hexagonPath(cx, cy + r * 0.90f, r), color = tint, style = Stroke(stroke))
    }
}

/** Flat-top regular hexagon path centered at ([cx], [cy]) with circumradius [r]. */
private fun hexagonPath(cx: Float, cy: Float, r: Float): Path {
    val path = Path()
    for (k in 0 until 6) {
        val angle = (PI / 3.0 * k).toFloat()
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun HexagonControlPreview() = ControlPreview {
    HexagonControl("On", previewEntity("on")) {}
    HexagonControl("Off", previewEntity("off")) {}
    HexagonControl("Offline", previewEntity("unavailable")) {}
}
