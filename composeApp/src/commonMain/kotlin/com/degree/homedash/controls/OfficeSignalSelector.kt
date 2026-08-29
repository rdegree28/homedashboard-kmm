package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.model.device_metadata.OfficeSignalMetadata
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * The office signal's mode pills: one per [OfficeSignalMetadata.SignalMode], the active one filled
 * with its own colour. Read-write — tapping a pill runs that mode through [onSelect].
 *
 * Carries its own card surface, the way [DoorCard] and [ClimateCard] do, so it sits in a device grid
 * with the group's title above it rather than inside a wrapper. Full width rather than tile-height:
 * the pills set the height.
 */
@Composable
internal fun OfficeSignalSelector(
    activeMode: OfficeSignalMetadata.SignalMode?,
    onSelect: (OfficeSignalMetadata.SignalMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(Dimens.CardCorner),
        color = AppColors.CardBackground,
        shadowElevation = Dimens.CardElevation,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.EntityCardPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OfficeSignalMetadata.SignalMode.entries.forEach { mode ->
                val active = activeMode == mode
                val color = signalColor(mode)
                Button(
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) color else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = mode.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun signalColor(mode: OfficeSignalMetadata.SignalMode): Color = when (mode) {
    OfficeSignalMetadata.SignalMode.OFF -> AppColors.SignalOff
    OfficeSignalMetadata.SignalMode.AVAILABLE -> AppColors.StatusGreen
    OfficeSignalMetadata.SignalMode.FOCUSED -> AppColors.StatusAmber
    OfficeSignalMetadata.SignalMode.MEETING -> AppColors.StatusRed
}

/**
 * One row per mode so each mode's colour is visible — only the active pill is filled — plus the
 * unmatched case, where the signal reports something unmodelled and nothing highlights.
 */
@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun OfficeSignalSelectorPreview() = ControlPreview {
    OfficeSignalMetadata.SignalMode.entries.forEach { mode ->
        OfficeSignalSelector(activeMode = mode, onSelect = {})
    }
    OfficeSignalSelector(activeMode = null, onSelect = {})
}
