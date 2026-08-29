package com.degree.homedash.controls

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.shared.model.entity.ClimateMetadata
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
    modifier: Modifier = Modifier,
) {
    val repo: ExpHomeAssistantRepo = koinInject()

    when (device) {
        is LightDeviceUi -> {
            val icon: @Composable (Color) -> Unit = { tint ->
                LightIcon(
                    on = device.isOn,
                    tint = tint,
                    modifier = Modifier.size(Dimens.RowIconSize),
                    icon = device.icon,
                )
            }
            val ui = ToggleUi(name = device.name, isOn = device.isOn, offline = device.offline)
            EntityToggleCard(
                ui,
                Color(device.tint),
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

        is OfficeSignalDeviceUi -> OfficeSignalSelector(
            activeMode = device.activeMode,
            onSelect = { mode -> device.select(mode, repo) },
            modifier = modifier,
        )

        is DoorDeviceUi -> DoorCard(
            label = device.name,
            statusText = device.statusText,
            open = device.open,
            unavailable = device.unavailable,
            modifier = modifier,
        )

        is ClimateDeviceUi -> {
            val (icon: ImageVector, tint: Color) = when (device.climateKind) {
                ClimateMetadata.ClimateKind.Temperature -> Icons.Filled.Thermostat to AppColors.TempWarm
                ClimateMetadata.ClimateKind.Humidity -> Icons.Filled.WaterDrop to AppColors.Wet
                ClimateMetadata.ClimateKind.DewPoint -> Icons.Filled.Opacity to AppColors.Wet
            }

            ClimateCard(
                label = device.name,
                valueText = device.valueText,
                subvalueText = device.subvalueText,
                icon = icon,
                tint = tint,
                modifier = modifier,
            )
        }

        is ThermostatDeviceUi -> ThermostatControlCard(
            ui = device,
            onSetTarget = { temperature -> device.setTargetTemperature(temperature, repo) },
            onSetHvacMode = { mode -> device.setHvacMode(mode, repo) },
            onSetFanMode = { mode -> device.setFanMode(mode, repo) },
            onSetPresetMode = { mode -> device.setPresetMode(mode, repo) },
            onSetExtreme = { extreme -> device.setExtremeTemperatures(extreme, repo) },
            modifier = modifier,
        )

        // Read-only here: the tap that opens its history graph is navigation, so the Plants screen
        // renders its own tappable rows rather than going through this dispatcher.
        is SoilMoistureDeviceUi -> SoilMoistureControl(ui = device)

        // Likewise — and the Pets screen draws the filter row underneath, which no grid cell can.
        is PetFountainDeviceUi -> WaterLevelControl(ui = device)

        // One-shot action, so the same tile whatever the layout.
        is TriggerDeviceUi -> TriggerCard(
            ui = device,
            onActivate = { device.onActivate(repo) },
            modifier = modifier,
        )

        is OfficeWorkstationUi -> WorkstationControl(
            ui = device,
            onToggle = { device.onToggle(repo) },
        )
    }
}
