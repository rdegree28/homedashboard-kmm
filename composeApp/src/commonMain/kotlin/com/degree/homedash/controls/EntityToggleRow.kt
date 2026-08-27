package com.degree.homedash.controls

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.degree.homedash.office.ToggleUi

/**
 * Shared row for toggleable entities: a custom [iconContent] + name + Switch.
 * Offline ([ToggleUi.offline]): faded icon, italic muted label, disabled toggle.
 *
 * [tintSwitch] carries [onTint] onto the switch as well, so the row reads as one colour instead of
 * a tinted icon beside the theme's purple. Off and disabled keep the theme's colours either way —
 * there is nothing lit to match.
 */
@Composable
internal fun EntityToggleRow(
    ui: ToggleUi,
    onTint: Color,
    onToggle: () -> Unit,
    tintSwitch: Boolean = false,
    iconContent: @Composable (tint: Color) -> Unit,
) {
    val baseTint = if (ui.isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (ui.offline) baseTint.copy(alpha = 0.3f) else baseTint

    EntityRow(
        label = ui.name,
        labelItalic = ui.offline,
        labelMuted = ui.offline,
        leading = { iconContent(iconTint) },
        trailing = {
            Switch(
                checked = ui.isOn,
                enabled = !ui.offline,
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
