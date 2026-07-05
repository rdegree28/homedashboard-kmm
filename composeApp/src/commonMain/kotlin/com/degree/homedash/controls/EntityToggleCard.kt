package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * Shared row for toggleable entities: a custom [iconContent] + name + Switch.
 * Offline ([ToggleUi.offline]): faded icon, italic muted label, disabled toggle.
 */
@Composable
internal fun EntityToggleCard(
    ui: ToggleUi,
    onTint: Color,
    onToggle: () -> Unit,
    iconContent: @Composable (tint: Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseTint = if (ui.isOn) onTint else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (ui.offline) baseTint.copy(alpha = 0.3f) else baseTint

    EntityCard(
        modifier = modifier,
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

@Preview
@Composable
fun EntityToggleCardPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EntityToggleCard(
                    modifier = Modifier.width(200.dp).height(Dimens.EntityCardHeight),
                    ui = ToggleUi(
                        name = "Entity",
                        isOn = true,
                        offline = false,
                    ),
                    onTint = AppColors.LightOn,
                    onToggle = {

                    },
                    iconContent = { tint ->
                        LightIcon(
                            on = true,
                            tint = tint,
                            modifier = Modifier.size(Dimens.RowIconSize)
                        )
                    }
                )
            }
        }
    }
}