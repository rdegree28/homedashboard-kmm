package com.degree.homedash.core.control

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.controls.ControlPreview
import com.degree.homedash.controls.EntityToggleCard
import com.degree.homedash.controls.HomeDashboardCard
import com.degree.homedash.controls.previewFan
import com.degree.homedash.core.device.FanDeviceUi
import com.degree.homedash.core.util.FanIcon
import com.degree.homedash.core.util.PillButton
import com.degree.homedash.core.util.fanSpinDurationMs
import com.degree.homedash.office.FanUi
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import kotlin.math.roundToInt

/**
 * Fan card tile. Two shapes, matching `cardSpan`:
 *  - **1-wide** (off / single-speed / offline): a plain [EntityToggleCard] — spinning icon top-left,
 *    centered name, tap to toggle — so it sits among the other cards in the grid.
 *  - **2-wide** (on + multi-level): a full-row card laid out horizontally — icon + name on the left
 *    (tap to toggle) with the stepped [FanSpeedSlider] filling the rest — so the slider has room.
 * Both are [Dimens.EntityCardHeight] tall so they line up with the light/climate cards.
 */
@Composable
internal fun FanControl(
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

@Composable
internal fun FanControl(
    ui: FanDeviceUi,
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
        val isTwoHigh = remember(ui.hasOscillation, ui.hasMisting) { ui.hasOscillation || ui.hasMisting }
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
                        fanIcon(if (ui.isMisting) AppColors.FanMisting else AppColors.FanBlue)
                        Text(
                            text = ui.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (ui.hasOscillation) {
                                FanControlButton(
                                    text = "Oscillate",
                                    icon = Icons.Filled.SwapHoriz,
                                    onToggle = { onSetOscillating(!ui.isOscillating) },
                                    isOn = ui.isOscillating,
                                    modifier = Modifier
                                )
                            }
                            if (ui.hasMisting) {
                                FanControlButton(
                                    text = "Mist",
                                    icon = Icons.Filled.WaterDrop,
                                    color = AppColors.FanMisting,
                                    onToggle = { onSetMisting(!ui.isMisting) },
                                    isOn = ui.isMisting,
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
private fun FanControlPreview() =
    ControlPreview {
        // "With speed" is the 2-wide state (on + multi-level): shown full-width, as the grid packs it.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "With speed",
                    isOn = true,
                    percentage = 75,
                    levelCount = 12
                ), {}, {}, {}, {}, Modifier.weight(1f)
            )
        }
        // The oscillation toggle only appears on the wide card, and only for fans that support it.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "Oscillating",
                    isOn = true,
                    percentage = 50,
                    levelCount = 12,
                    canOscillate = true,
                    oscillating = true
                ),
                {}, {}, {}, {}, Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "Can oscillate",
                    isOn = true,
                    percentage = 25,
                    levelCount = 12,
                    canOscillate = true
                ),
                {}, {}, {}, {}, Modifier.weight(1f),
            )
        }
        // The oscillation toggle only appears on the wide card, and only for fans that support it.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "Misting",
                    isOn = true,
                    percentage = 50,
                    levelCount = 12,
                    canOscillate = true,
                    oscillating = true,
                    misting = true,
                    canMist = true
                ),
                {}, {}, {}, {}, Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "Can mist",
                    isOn = true,
                    percentage = 25,
                    levelCount = 12,
                    canOscillate = true,
                    canMist = true
                ),
                {}, {}, {}, {}, Modifier.weight(1f),
            )
        }
        // The slider-less states are 1-wide toggle tiles, so they pair up half-width like other cards.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "On",
                    isOn = true
                ), {}, {}, {}, {}, Modifier.weight(1f)
            )
            FanControl(
                previewFan(
                    "Off",
                    isOn = false
                ), {}, {}, {}, {}, Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FanControl(
                previewFan(
                    "Offline",
                    offline = true
                ), {}, {}, {}, {}, Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
    }

/**
 * Speed slider that works in integer levels (0..[levelCount]) so every step is reachable.
 * On release it sends the MIDPOINT percentage of the chosen level's band, which maps cleanly
 * onto that level under HA's ceil-based percentage→speed conversion (no dead/duplicate steps).
 */
@Composable
internal fun FanSpeedSlider(
    percentage: Int,
    levelCount: Int,
    onSet: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLevel = (percentage / 100f * levelCount).roundToInt().coerceIn(0, levelCount)
    var level by remember(currentLevel) { mutableStateOf(currentLevel.toFloat()) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Slider(
            value = level,
            onValueChange = { level = it },
            onValueChangeFinished = {
                val k = level.roundToInt()
                if (k >= 1) {
                    onSet(((k - 0.5) * 100.0 / levelCount).roundToInt())
                } else {
                    level = currentLevel.toFloat() // show the 0 stop, but ignore setting 0%
                }
            },
            valueRange = 0f..levelCount.toFloat(),
            steps = (levelCount - 1).coerceAtLeast(0),
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = AppColors.FanBlue,
                activeTrackColor = AppColors.FanBlue,
                activeTickColor = AppColors.FanBlue.copy(alpha = 0.6f),
            ),
        )
        Text(
            text = "${level.roundToInt()} / $levelCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A fan's labelled toggle pill (oscillate, mist) — [PillButton] with the icon always present. */
@Composable
internal fun FanControlButton(
    onToggle: () -> Unit,
    isOn: Boolean,
    text: String,
    icon: ImageVector,
    color: Color = AppColors.FanBlue,
    modifier: Modifier = Modifier,
) {
    PillButton(
        text = text,
        isOn = isOn,
        onClick = onToggle,
        modifier = modifier,
        icon = icon,
        color = color,
        contentDescription = if (isOn) "Turn off $text" else "Turn on $text",
    )
}
