package com.degree.homedash.controls

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens
import org.koin.compose.koinInject

/**
 * Central renderer for the [DeviceUi] stack: maps a device to its control in the requested [layout].
 * [modifier] applies to the card cell (grid weighting).
 *
 * Unlike [EntityControl] there is no `onAction` here — a [DeviceUi] carries its own behavior, so this
 * only has to supply the repo to run it through. [ExpHomeAssistantRepo] is resolved from Koin at this
 * one dispatch site rather than threaded down from the screen.
 */
@Composable
fun DeviceControl(
    device: DeviceUi,
    layout: ControlLayout,
    modifier: Modifier = Modifier,
) {
    val repo: ExpHomeAssistantRepo = koinInject()

    when (device) {
        is LightDeviceUi -> {
            val icon: @Composable (Color) -> Unit = { tint ->
                LightIcon(on = device.isOn, tint = tint, modifier = Modifier.size(Dimens.RowIconSize))
            }
            val ui = ToggleUi(name = device.name, isOn = device.isOn, offline = device.offline)
            EntityToggleCard(
                ui,
                AppColors.LightOn,
                { device.onToggle(repo) },
                icon,
                modifier,
            )
        }

        is FanDeviceUi -> {
            FanControlCard(
                ui = device,
                onSetSpeed = { device.setFanSpeed(it, repo) },
                onSetOscillating = { device.toggleOscillation(repo) },
                onSetMisting = { device.toggleMisting(repo) },
                onToggle = { device.onToggle(repo) },
                modifier = modifier,
            )
        }
    }
}
