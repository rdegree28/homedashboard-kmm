package com.degree.homedash.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.entity.LightMetadata
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A light's glyph, with a soft glow in its own [tint] that gently breathes while [on].
 *
 * [icon] is the token the light's metadata declares (see [LightMetadata.LightIcon]) — the glow and the
 * layout are the same whichever drawing it resolves to, so a hexagon panel reads as a light.
 */
@Composable
fun LightIcon(
    on: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    icon: LightMetadata.LightIcon = LightMetadata.LightIcon.Bulb,
) {
    val transition = rememberInfiniteTransition(label = "light")
    val glow by transition.animateFloat(
        initialValue = if (on) 0.18f else 0f,
        targetValue = if (on) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lightGlow",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        if (on) {
            Canvas(Modifier.matchParentSize()) {
                val rad = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = glow), Color.Transparent),
                        center = center,
                        radius = rad,
                    ),
                    radius = rad,
                    center = center,
                )
            }
        }
        when (icon) {
            LightMetadata.LightIcon.Bulb -> Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(21.dp),
            )

            LightMetadata.LightIcon.Hexagon -> Canvas(Modifier.matchParentSize()) {
                drawHexagonPanels(tint)
            }
        }
    }
}

/** Three hexagon panels — two on top, one below — in the arrangement they hang on the wall. */
private fun DrawScope.drawHexagonPanels(tint: Color) {
    val r = size.width * 0.18f
    val stroke = size.width * 0.05f
    drawPath(hexagonPath(center.x - r * 0.95f, center.y - r * 0.52f, r), color = tint, style = Stroke(stroke))
    drawPath(hexagonPath(center.x + r * 0.95f, center.y - r * 0.52f, r), color = tint, style = Stroke(stroke))
    drawPath(hexagonPath(center.x, center.y + r * 0.90f, r), color = tint, style = Stroke(stroke))
}

/** Flat-top regular hexagon path centered at ([cx], [cy]) with circumradius [r]. */
private fun hexagonPath(
    cx: Float,
    cy: Float,
    r: Float,
): Path {
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
