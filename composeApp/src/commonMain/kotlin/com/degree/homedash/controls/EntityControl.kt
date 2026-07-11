package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.office.DoorUi
import com.degree.homedash.office.FanUi
import com.degree.homedash.office.SensorUi
import com.degree.homedash.office.ToggleUi
import com.degree.homedash.ui.AppColors
import com.degree.homedash.ui.Dimens

/** An action a rendered [EntityUi] can emit; screens map these to their ViewModel at one boundary. */
sealed interface EntityAction {
    data class Toggle(val entityId: String) : EntityAction
    data class SetSpeed(val entityId: String, val percentage: Int) : EntityAction
    data class OpenGraph(val entityId: String) : EntityAction
}

/** How a control should render. `ControlGroup` picks this per group; individual controls don't. */
enum class ControlLayout { Row, Card }

/** True for entity types that have a card rendering (currently lights, fans, and climate). */
fun EntityUi.hasCard(): Boolean =
    this is EntityUi.Light || this is EntityUi.Fan || this is EntityUi.Climate

/**
 * Central renderer: maps an [EntityUi] to the right control, in the requested [layout], routing user
 * interaction through [onAction]. Lights and fans have a [ControlLayout.Card] form (tap to toggle),
 * climate has a read-only card; every other type renders its row regardless of [layout]. [modifier]
 * applies to the card cell (grid weighting).
 */
@Composable
fun EntityControl(
    entity: EntityUi,
    layout: ControlLayout,
    onAction: (EntityAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (entity) {
        is EntityUi.Light -> {
            val onToggle = { onAction(EntityAction.Toggle(entity.entityId)) }
            val icon: @Composable (Color) -> Unit = { tint ->
                LightIcon(on = entity.isOn, tint = tint, modifier = Modifier.size(Dimens.RowIconSize))
            }
            val ui = ToggleUi(name = entity.name, isOn = entity.isOn, offline = entity.offline)
            when (layout) {
                ControlLayout.Row -> EntityToggleRow(ui, AppColors.LightOn, onToggle, icon)
                ControlLayout.Card -> EntityToggleCard(ui, AppColors.LightOn, onToggle, icon, modifier)
            }
        }

        is EntityUi.Fan -> {
            val onToggle = { onAction(EntityAction.Toggle(entity.entityId)) }
            val fanUi = FanUi(
                name = entity.name,
                isOn = entity.isOn,
                offline = entity.offline,
                levelCount = entity.metadata.levelCount,
                percentage = entity.percentage,
            )
            when (layout) {
                ControlLayout.Row -> FanControl(
                    ui = fanUi,
                    onSetSpeed = { pct -> onAction(EntityAction.SetSpeed(entity.entityId, pct)) },
                    onToggle = onToggle,
                )
                ControlLayout.Card -> {
                    val icon: @Composable (Color) -> Unit = { tint ->
                        FanIcon(
                            spinning = entity.isOn,
                            durationMs = fanSpinDurationMs(
                                percentage = entity.percentage,
                                levelCount = entity.metadata.levelCount,
                                hasSpeedControl = entity.metadata.levelCount >= 2,
                            ),
                            tint = tint,
                            modifier = Modifier.size(Dimens.RowIconSize),
                        )
                    }
                    val ui = ToggleUi(name = entity.name, isOn = entity.isOn, offline = entity.offline)
                    EntityToggleCard(ui, AppColors.Accent, onToggle, icon, modifier)
                }
            }
        }

        is EntityUi.Climate -> {
            val (icon: ImageVector, tint: Color) = when (entity.metadata.kind) {
                ClimateKind.Temperature -> Icons.Filled.Thermostat to AppColors.TempWarm
                ClimateKind.Humidity -> Icons.Filled.WaterDrop to AppColors.Wet
                ClimateKind.DewPoint -> Icons.Filled.Opacity to AppColors.Wet
            }
            when (layout) {
                ControlLayout.Row -> ClimateRow(
                    ui = SensorUi(entity.label, entity.valueText),
                    icon = icon,
                    tint = tint
                )
                ControlLayout.Card -> ClimateCard(
                    label = entity.label,
                    valueText = entity.valueText,
                    subvalueText = entity.subvalueText,
                    icon = icon,
                    tint = tint,
                    modifier = modifier
                )
            }
        }

        is EntityUi.Door -> DoorRow(
            DoorUi(
                label = entity.label,
                statusText = entity.statusText,
                open = entity.open,
                unavailable = entity.unavailable,
            ),
        )

        is EntityUi.SoilMoisture -> SoilMoistureControl(
            ui = entity,
            onClick = { onAction(EntityAction.OpenGraph(entity.entityId)) },
        )

        is EntityUi.WaterLevel -> WaterLevelControl(
            ui = entity,
            onClick = { onAction(EntityAction.OpenGraph(entity.entityId)) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityLightRowPreview() = ControlPreview {
    EntityControl(previewLight("On", isOn = true), ControlLayout.Row, {})
    EntityControl(previewLight("Off", isOn = false), ControlLayout.Row, {})
    EntityControl(previewLight("Offline", offline = true), ControlLayout.Row, {})
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityLightCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewLight("On", isOn = true), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewLight("Off", isOn = false), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewLight("Offline", offline = true), ControlLayout.Card, {}, Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityFanCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewFanUi("On", isOn = true, percentage = 75, levelCount = 12), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewFanUi("Off", isOn = false), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewFanUi("Offline", offline = true), ControlLayout.Card, {}, Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityClimateCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewClimate("Temperature", "72.5 °F", ClimateKind.Temperature), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewClimate("Humidity", "48 %", ClimateKind.Humidity), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewClimate("Dew Point", "50.9 °F", ClimateKind.DewPoint), ControlLayout.Card, {}, Modifier.weight(1f))
    }
}
