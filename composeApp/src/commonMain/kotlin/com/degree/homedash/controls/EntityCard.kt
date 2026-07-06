package com.degree.homedash.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.degree.homedash.ui.Dimens

/**
 * Shared card tile for an entity: [leading] content (icon/canvas) pinned to the top-left corner
 * with the [label] centered. The whole tile is tappable via [onClick]. Offline entities pass
 * [enabled] = false (dimmed, non-interactive) plus [labelItalic]/[labelMuted] styling.
 */
@Composable
internal fun EntityCard(
    label: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
    labelItalic: Boolean = false,
    labelMuted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.height(Dimens.EntityCardHeight),
    ) {
        Box(Modifier.fillMaxSize().padding(Dimens.EntityCardPadding)) {
            Box(Modifier.align(Alignment.TopStart)) { leading() }
            Text(
                text = label,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontStyle = if (labelItalic) FontStyle.Italic else FontStyle.Normal,
                color = if (labelMuted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    Color.Unspecified
                },
            )
        }
    }
}
