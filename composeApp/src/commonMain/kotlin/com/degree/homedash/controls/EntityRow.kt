package com.degree.homedash.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import com.degree.homedash.ui.Dimens

/**
 * Shared layout for an entity row: [leading] content (icon/canvas) + a weighted [label] + [trailing]
 * content (value text or control). Used by the climate, door, and toggle rows so they line up.
 */
@Composable
internal fun EntityRow(
    label: String,
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    labelItalic: Boolean = false,
    labelMuted: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(Dimens.EntityRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(Dimens.RowLabelGap))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = if (labelItalic) FontStyle.Italic else FontStyle.Normal,
            color = if (labelMuted) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                Color.Unspecified
            },
        )
        trailing()
    }
}
