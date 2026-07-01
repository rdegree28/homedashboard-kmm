package com.degree.homedash.office

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.degree.homedash.shared.model.EntityState

/**
 * Shared row for toggleable entities: a custom [iconContent] + name + Switch.
 * Offline (entity missing or unavailable): faded icon, italic muted label, disabled toggle.
 */
@Composable
internal fun EntityToggleRow(
    name: String,
    entity: EntityState?,
    onTint: Color,
    onToggle: () -> Unit,
    iconContent: @Composable (tint: Color) -> Unit,
) {
    val isOn = entity?.isOn == true
    val offline = entity == null || entity.isUnavailable

    val baseTint = if (isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (offline) baseTint.copy(alpha = 0.3f) else baseTint

    EntityRow(
        label = name,
        labelItalic = offline,
        labelMuted = offline,
        leading = { iconContent(iconTint) },
        trailing = {
            Switch(
                checked = isOn,
                enabled = !offline,
                onCheckedChange = { onToggle() },
            )
        },
    )
}
