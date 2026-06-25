package com.degree.homedash.office

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState

private val AmberOn = Color(0xFFFFC107)
private val FanOn = Color(0xFF4C8DFF)
private val BananaOn = Color(0xFFFFE135)

/** A light entity row: a bulb icon (amber when on) + name + toggle. */
@Composable
fun LightControl(
    name: String,
    entity: EntityState?,
    icon: ImageVector,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, AmberOn, onToggle) { tint ->
    LightIcon(on = entity?.isOn == true, icon = icon, tint = tint, modifier = Modifier.size(26.dp))
}

/** Bulb icon with a soft amber glow that gently breathes while [on]. */
@Composable
private fun LightIcon(on: Boolean, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bulb")
    val glow by transition.animateFloat(
        initialValue = if (on) 0.18f else 0f,
        targetValue = if (on) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bulbGlow",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        if (on) {
            Canvas(Modifier.matchParentSize()) {
                val rad = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AmberOn.copy(alpha = glow), Color.Transparent),
                        center = center,
                        radius = rad,
                    ),
                    radius = rad,
                    center = center,
                )
            }
        }
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}

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

        // Static outer ring (the fan housing).
        drawCircle(color = tint, radius = ringR, center = Offset(cx, cy), style = Stroke(width = ringStroke))

        // One swept blade pointing up from the hub.
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

        // Three blades, rotating together.
        for (i in 0 until 3) {
            rotate(degrees = angle + i * 120f, pivot = Offset(cx, cy)) {
                drawPath(blade, color = tint)
            }
        }

        // Center hub.
        drawCircle(color = tint, radius = w * 0.08f, center = Offset(cx, cy))
    }
}

/** A workstation row: a laptop icon with code scrolling on its screen while on (banana yellow). */
@Composable
fun WorkstationControl(
    name: String,
    entity: EntityState?,
    onToggle: () -> Unit,
) = EntityToggleRow(name, entity, BananaOn, onToggle) { tint ->
    WorkstationIcon(on = entity?.isOn == true, tint = tint, modifier = Modifier.size(26.dp))
}

/** Custom laptop: screen + keyboard deck, with "code" lines scrolling up the screen while [on]. */
@Composable
private fun WorkstationIcon(on: Boolean, tint: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "ws")
    val scroll by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (on) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wsScroll",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.055f

        // Screen (monitor).
        val sx0 = w * 0.18f
        val sy0 = h * 0.20f
        val sw = w * 0.64f
        val sh = h * 0.38f

        // Keyboard deck — trapezoid wider than the screen.
        val deck = Path().apply {
            moveTo(w * 0.08f, h * 0.76f)
            lineTo(w * 0.92f, h * 0.76f)
            lineTo(sx0 + sw, sy0 + sh)
            lineTo(sx0, sy0 + sh)
            close()
        }
        drawPath(deck, color = tint)

        // Scrolling code lines inside the screen while on.
        if (on) {
            val inset = stroke
            val left = sx0 + inset
            val right = sx0 + sw - inset
            val top = sy0 + inset
            val bottom = sy0 + sh - inset
            clipRect(left = left, top = top, right = right, bottom = bottom) {
                val widths = floatArrayOf(0.85f, 0.5f, 0.95f, 0.6f, 0.75f)
                val gap = (bottom - top) / 3f
                val shift = scroll * gap
                var i = 0
                var y = bottom + gap - shift
                while (y > top - gap) {
                    val lineLen = (right - left) * widths[i % widths.size]
                    drawLine(
                        color = tint,
                        start = Offset(left, y),
                        end = Offset(left + lineLen, y),
                        strokeWidth = h * 0.05f,
                    )
                    y -= gap
                    i++
                }
            }
        }

        // Screen outline on top.
        drawRoundRect(
            color = tint,
            topLeft = Offset(sx0, sy0),
            size = Size(sw, sh),
            cornerRadius = CornerRadius(w * 0.04f),
            style = Stroke(width = stroke),
        )
    }
}

@Composable
private fun EntityToggleRow(
    name: String,
    entity: EntityState?,
    onTint: Color,
    onToggle: () -> Unit,
    iconContent: @Composable (tint: Color) -> Unit,
) {
    val isOn = entity?.isOn == true
    // "Offline" = entity missing or reporting unavailable/unknown.
    val offline = entity == null || entity.isUnavailable

    val baseTint = if (isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (offline) baseTint.copy(alpha = 0.3f) else baseTint

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconContent(iconTint)
        Spacer(Modifier.width(16.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = if (offline) FontStyle.Italic else FontStyle.Normal,
            color = if (offline) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                Color.Unspecified
            },
        )
        Switch(
            checked = isOn,
            enabled = !offline,
            onCheckedChange = { onToggle() },
        )
    }
}
