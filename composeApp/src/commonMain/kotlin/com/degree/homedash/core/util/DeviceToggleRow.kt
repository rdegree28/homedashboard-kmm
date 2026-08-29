package com.degree.homedash.core.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Shared row for toggleable devices: a custom [iconContent] + [name] + Switch.
 * [offline]: faded icon, italic muted label, disabled toggle.
 *
 * [tintSwitch] carries [onTint] onto the switch as well, so the row reads as one colour instead of
 * a tinted icon beside the theme's purple. Off and disabled keep the theme's colours either way —
 * there is nothing lit to match.
 */
@Composable
internal fun DeviceToggleRow(
    name: String,
    isOn: Boolean,
    offline: Boolean,
    onTint: Color,
    onToggle: () -> Unit,
    tintSwitch: Boolean = false,
    iconContent: @Composable (tint: Color) -> Unit,
) {
    val baseTint = if (isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (offline) baseTint.copy(alpha = 0.3f) else baseTint

    DeviceRow(
        label = name,
        labelItalic = offline,
        labelMuted = offline,
        leading = { iconContent(iconTint) },
        trailing = {
            Switch(
                checked = isOn,
                enabled = !offline,
                onCheckedChange = { onToggle() },
                colors = if (tintSwitch) switchColorsFor(onTint) else SwitchDefaults.colors(),
            )
        },
    )
}

/**
 * The track in [tint] with the thumb a darkened version of it, so the circle still reads against a
 * light track — the thumb picks up the theme's contrast for free at the default colours, but a
 * banana-yellow track would otherwise carry a near-white circle.
 */
@Composable
private fun switchColorsFor(tint: Color) = SwitchDefaults.colors(
    checkedTrackColor = tint,
    checkedBorderColor = tint,
    checkedThumbColor = lerp(tint, Color.Black, 0.35f),
    checkedIconColor = tint,
)
