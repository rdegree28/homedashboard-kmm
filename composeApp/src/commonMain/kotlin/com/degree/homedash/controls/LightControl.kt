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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/** A light entity row: a bulb icon (amber when on) + name + toggle. */
@Composable
fun LightControl(
    ui: ToggleUi,
    onToggle: () -> Unit,
) =
    EntityToggleRow(ui, AppColors.LightOn, onToggle) { tint ->
        LightIcon(on = ui.isOn, tint = tint, modifier = Modifier.size(Dimens.RowIconSize))
    }

/** Bulb icon with a soft amber glow that gently breathes while [on]. */
@Composable
private fun LightIcon(
    on: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
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
                        colors = listOf(AppColors.LightOn.copy(alpha = glow), Color.Transparent),
                        center = center,
                        radius = rad,
                    ),
                    radius = rad,
                    center = center,
                )
            }
        }
        Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun LightControlPreview() = ControlPreview {
    LightControl(previewToggle("On", isOn = true)) {}
    LightControl(previewToggle("Off", isOn = false)) {}
    LightControl(previewToggle("Offline", offline = true)) {}
}
