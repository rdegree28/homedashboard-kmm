package com.degree.homedash.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.App
import com.degree.homedash.office.FanUi
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/**
 * Fan card tile. Two shapes, matching [cardSpan]:
 *  - **1-wide** (off / single-speed / offline): a plain [EntityToggleCard] — spinning icon top-left,
 *    centered name, tap to toggle — so it sits among the other cards in the grid.
 *  - **2-wide** (on + multi-level): a full-row card laid out horizontally — icon + name on the left
 *    (tap to toggle) with the stepped [FanSpeedSlider] filling the rest — so the slider has room.
 * Both are [Dimens.EntityCardHeight] tall so they line up with the light/climate cards.
 */
@Composable
internal fun FanControlCard(
    ui: FanUi,
    onSetSpeed: (Int) -> Unit,
    onSetOscillating: (Boolean) -> Unit,
    onSetMisting: (Boolean) -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fanIcon: @Composable (tint: Color) -> Unit = { tint ->
        FanIcon(
            spinning = ui.isOn,
            durationMs = fanSpinDurationMs(
                percentage = ui.percentage,
                levelCount = ui.levelCount,
                hasSpeedControl = ui.levelCount >= 2,
            ),
            tint = tint,
            modifier = Modifier.size(Dimens.RowIconSize),
        )
    }

    if (!(ui.isOn && ui.levelCount >= 2)) {
        EntityToggleCard(
            ui = ToggleUi(ui.name, ui.isOn, ui.offline),
            onTint = AppColors.FanBlue,
            onToggle = onToggle,
            iconContent = fanIcon,
            modifier = modifier,
        )
    } else {
        val isTwoHigh = remember(ui.canOscillate, ui.canMist) { ui.canOscillate || ui.canMist }
        val height = if (isTwoHigh) Dimens.TwoRowEntityCardHeight else Dimens.EntityCardHeight

        HomeDashboardCard(
            onClick = onToggle,
            enabled = true,
            height = height,
            modifier = modifier,
        ) {
            if (isTwoHigh) {
                Column(
                    modifier = Modifier.fillMaxSize().clickable(onClick = onToggle),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        fanIcon(if (ui.misting) AppColors.FanMisting else AppColors.FanBlue)
                        Text(
                            text = ui.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (ui.canOscillate) {
                                FanControlButton(
                                    text = "Oscillate",
                                    icon = Icons.Filled.SwapHoriz,
                                    onToggle = { onSetOscillating(!ui.oscillating) },
                                    isOn = ui.oscillating,
                                    modifier = Modifier
                                )
                            }
                            if (ui.canMist) {
                                FanControlButton(
                                    text = "Mist",
                                    icon = Icons.Filled.WaterDrop,
                                    color = AppColors.FanMisting,
                                    onToggle = { onSetMisting(!ui.misting) },
                                    isOn = ui.misting,
                                    modifier = Modifier
                                )
                            }
                        }
                    }

                    FanSpeedSlider(
                        percentage = ui.percentage,
                        levelCount = ui.levelCount,
                        onSet = onSetSpeed,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable(onClick = onToggle),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        fanIcon(AppColors.FanBlue)
                        Text(
                            text = ui.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                        )
                    }
                    FanSpeedSlider(
                        percentage = ui.percentage,
                        levelCount = ui.levelCount,
                        onSet = onSetSpeed,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun FanControlCardPreview() = ControlPreview {
    // "With speed" is the 2-wide state (on + multi-level): shown full-width, as the grid packs it.
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(previewFan("With speed", isOn = true, percentage = 75, levelCount = 12), {}, {}, {}, {}, Modifier.weight(1f))
    }
    // The oscillation toggle only appears on the wide card, and only for fans that support it.
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(
            previewFan("Oscillating", isOn = true, percentage = 50, levelCount = 12, canOscillate = true, oscillating = true),
            {}, {}, {}, {}, Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(
            previewFan("Can oscillate", isOn = true, percentage = 25, levelCount = 12, canOscillate = true),
            {}, {}, {}, {}, Modifier.weight(1f),
        )
    }
    // The oscillation toggle only appears on the wide card, and only for fans that support it.
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(
            previewFan("Misting", isOn = true, percentage = 50, levelCount = 12, canOscillate = true, oscillating = true, misting = true, canMist = true),
            {}, {}, {}, {}, Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(
            previewFan("Can mist", isOn = true, percentage = 25, levelCount = 12, canOscillate = true, canMist = true),
            {}, {}, {}, {}, Modifier.weight(1f),
        )
    }
    // The slider-less states are 1-wide toggle tiles, so they pair up half-width like other cards.
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(previewFan("On", isOn = true), {}, {}, {}, {}, Modifier.weight(1f))
        FanControlCard(previewFan("Off", isOn = false), {}, {}, {}, {}, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FanControlCard(previewFan("Offline", offline = true), {}, {}, {}, {}, Modifier.weight(1f))
        Spacer(Modifier.weight(1f))
    }
}
