package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * A pressable tile that fires a scene, script or automation.
 *
 * Reuses [EntityCard] so it sits at the same size and weight as the light and fan tiles beside it.
 * There is no on/off state to show — pressing it is a one-shot action — so the tint is fixed rather
 * than reflecting anything.
 */
@Composable
fun TriggerCard(
    ui: EntityUi.Trigger,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EntityCard(
        modifier = modifier,
        label = ui.displayName,
        onClick = onActivate,
        leading = {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = AppColors.Accent,
                modifier = Modifier.size(Dimens.RowIconSize),
            )
        },
    )
}

@Preview(widthDp = 380)
@Composable
private fun TriggerCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) {
            TriggerCard(previewTrigger("Main Lights"), onActivate = {}, modifier = Modifier.fillMaxWidth())
        }
        Box(Modifier.weight(1f)) {
            TriggerCard(previewTrigger("Movie Night"), onActivate = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
