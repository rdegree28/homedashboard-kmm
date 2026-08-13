package com.degree.homedash.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

private val PillCorner = 16.dp

/**
 * The dashboard's pill button: outlined in [color] when off, filled with it when on.
 *
 * [icon] is optional, and [iconSize] defaults to the row size the fan controls use; the thermostat's
 * mode pills pass the smaller [Dimens.PillIconSize] so the glyph doesn't crowd a short label.
 * A disabled pill keeps its shape but stops responding, which is what an offline entity needs.
 */
@Composable
internal fun PillButton(
    text: String?,
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSize: Dp = Dimens.RowIconSize,
    color: Color = AppColors.FanBlue,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val outline = if (enabled) color else color.copy(alpha = 0.35f)
    Row(
        modifier = modifier
            .background(color = if (isOn) outline else Color.Transparent, shape = RoundedCornerShape(PillCorner))
            .clickable(enabled = enabled, onClick = onClick)
            .border(width = 1.dp, color = outline, shape = RoundedCornerShape(PillCorner))
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Null text is an icon-only pill (the thermostat presets), where the glyph is the whole label.
        if (text != null) {
            Text(
                modifier = if (icon != null) Modifier.padding(end = 4.dp) else Modifier,
                text = text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                },
            )
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun PillButtonPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PillButton("Off", isOn = false, onClick = {})
        PillButton("On", isOn = true, onClick = {})
        PillButton("Disabled", isOn = false, onClick = {}, enabled = false)
        PillButton("Icon", isOn = true, onClick = {}, icon = Icons.Filled.SwapHoriz)
    }
}
