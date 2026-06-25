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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.EntityState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

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

/** A read-only sensor readout: icon + label + formatted value (e.g. "76.3 °F"). */
@Composable
fun ClimateRow(label: String, entity: EntityState?, icon: ImageVector, tint: Color) {
    val unit = entity?.attrString("unit_of_measurement").orEmpty()
    val value = when {
        entity == null || entity.isUnavailable -> "—"
        else -> {
            val v = entity.state.toDoubleOrNull()
            val num = if (v != null) {
                val r = round(v * 10.0) / 10.0
                if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
            } else {
                entity.state
            }
            if (unit.isNotEmpty()) "$num $unit" else num
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** A read-only door row: white door icon (hollow when open, solid when closed) + Open/Closed. */
@Composable
fun DoorRow(label: String, entity: EntityState?) {
    val unavailable = entity == null || entity.isUnavailable
    val open = entity?.state == "on" // device_class opening: on = open
    val status = when {
        unavailable -> "—"
        open -> "Open"
        else -> "Closed"
    }
    val tint = if (unavailable) Color.White.copy(alpha = 0.3f) else Color.White

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DoorIcon(open = open, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            text = status,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Door icon: hollow outline when [open], solid slab when closed. */
@Composable
private fun DoorIcon(open: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val left = w * 0.30f
        val top = h * 0.15f
        val right = w * 0.70f
        val bottom = h * 0.85f
        val corner = CornerRadius(w * 0.06f)
        val knob = Offset(right - w * 0.10f, (top + bottom) / 2f)
        val knobR = w * 0.045f

        if (open) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = corner,
                style = Stroke(width = w * 0.08f),
            )
            drawCircle(color = tint, radius = knobR, center = knob)
        } else {
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = corner,
            )
            drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = knobR, center = knob)
        }
    }
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
        // Two on top, one centered below.
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
