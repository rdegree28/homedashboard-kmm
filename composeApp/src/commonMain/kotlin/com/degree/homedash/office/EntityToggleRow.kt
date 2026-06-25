package com.degree.homedash.office

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
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
