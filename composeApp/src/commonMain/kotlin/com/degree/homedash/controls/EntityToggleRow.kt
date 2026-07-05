package com.degree.homedash.controls

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.degree.homedash.office.ToggleUi

/**
 * Shared row for toggleable entities: a custom [iconContent] + name + Switch.
 * Offline ([ToggleUi.offline]): faded icon, italic muted label, disabled toggle.
 */
@Composable
internal fun EntityToggleRow(
    ui: ToggleUi,
    onTint: Color,
    onToggle: () -> Unit,
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
            )
        },
    )
}
