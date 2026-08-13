package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.shared.model.entity.NavigationMetadata
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
    data class SetOscillating(val entityId: String, val oscillating: Boolean) : EntityAction

    /** [entityId] is the mister's own `humidifier.*` entity, not the fan's. */
    data class SetMisting(val entityId: String, val misting: Boolean) : EntityAction

    data class SetTargetTemperature(val entityId: String, val temperature: Double) : EntityAction
    data class SetHvacMode(val entityId: String, val mode: HvacMode) : EntityAction

    /** [mode] is one of the vendor strings the thermostat's metadata declares, not a `fan.*` speed. */
    data class SetThermostatFanMode(val entityId: String, val mode: String) : EntityAction
    data class SetPresetMode(val entityId: String, val mode: String) : EntityAction

    /** [entityId] is the `input_boolean.*` helper the thermostat names, not the thermostat itself. */
    data class SetExtremeTemperatures(val entityId: String, val extreme: Boolean) : EntityAction

    data class OpenGraph(val entityId: String) : EntityAction

    /** Carries the typed destination rather than an entity id — a nav card's id is synthetic. */
    data class Navigate(val target: NavigationMetadata.NavigationTarget) : EntityAction

    /** Fire a scene/script/automation. Carries the whole call, since a trigger's id is synthetic. */
    data class Activate(val call: TriggerEntityMetadata.ServiceCall) : EntityAction
}

/** How a control should render. `ControlGroup` picks this per group; individual controls don't. */
enum class ControlLayout { Row, Card }

/** True for entity types that have a card rendering (lights, fans, climate, and launcher tiles). */
fun EntityUi.hasCard(): Boolean =
    this is EntityUi.Light || this is EntityUi.Fan || this is EntityUi.Climate ||
        this is EntityUi.Thermostat || this is EntityUi.Navigation || this is EntityUi.Trigger

/**
 * How many grid columns this entity's card should span (out of the grid's 2). Two types go wide:
 * a thermostat always (its stepper and mode pills need the room, and a card that changed width as
 * the mode changed would shuffle the grid under the user's finger), and a fan only while it is
 * showing its speed slider — the same condition `FanControlCard` uses for `showSlider`; otherwise
 * a fan is a single-column toggle tile like the rest.
 * `ControlGroup` packs rows to a total width of 2 using this, so cards reflow as fans toggle.
 */
fun EntityUi.cardSpan(): Int = when {
    this is EntityUi.Thermostat -> 2
    // Oscillation counts too: without it, an oscillating fan with no speed control would never get
    // the wide card its toggle lives on, leaving the control unreachable.
    this is EntityUi.Fan && isOn &&
        (metadata.speedAdjustment != null || metadata.hasOscillationFeature) -> 2
    else -> 1
}

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
            val ui = ToggleUi(name = entity.displayName, isOn = entity.isOn, offline = entity.offline)
            when (layout) {
                ControlLayout.Row -> EntityToggleRow(ui, AppColors.LightOn, onToggle, icon)
                ControlLayout.Card -> EntityToggleCard(ui, AppColors.LightOn, onToggle, icon, modifier)
            }
        }

        is EntityUi.Fan -> {
            val onToggle = { onAction(EntityAction.Toggle(entity.entityId)) }
            val fanUi = FanUi(
                name = entity.displayName,
                isOn = entity.isOn,
                offline = entity.offline,
                levelCount = entity.metadata.speedAdjustment?.levelCount ?: 0,
                percentage = entity.percentage,
                canOscillate = entity.metadata.hasOscillationFeature,
                oscillating = entity.oscillating,
                canMist = entity.metadata.misting != null,
                misting = entity.misting,
            )
            val onSetSpeed = { pct: Int -> onAction(EntityAction.SetSpeed(entity.entityId, pct)) }
            val onSetOscillating = { on: Boolean ->
                onAction(EntityAction.SetOscillating(entity.entityId, on))
            }
            // Targets the mister entity, so a fan without one can't emit this at all.
            val onSetMisting = { on: Boolean ->
                entity.metadata.misting?.let { onAction(EntityAction.SetMisting(it.entityId, on)) }
                Unit
            }
            when (layout) {
                ControlLayout.Row -> FanControl(
                    ui = fanUi,
                    onSetSpeed = onSetSpeed,
                    onToggle = onToggle,
                )
                ControlLayout.Card -> FanControlCard(
                    ui = fanUi,
                    onSetSpeed = onSetSpeed,
                    onSetOscillating = onSetOscillating,
                    onSetMisting = onSetMisting,
                    onToggle = onToggle,
                    modifier = modifier,
                )
            }
        }

        is EntityUi.Climate -> {
            val (icon: ImageVector, tint: Color) = when (entity.metadata.kind) {
                ClimateMetadata.ClimateKind.Temperature -> Icons.Filled.Thermostat to AppColors.TempWarm
                ClimateMetadata.ClimateKind.Humidity -> Icons.Filled.WaterDrop to AppColors.Wet
                ClimateMetadata.ClimateKind.DewPoint -> Icons.Filled.Opacity to AppColors.Wet
            }
            when (layout) {
                ControlLayout.Row -> ClimateRow(
                    ui = SensorUi(entity.displayName, entity.valueText),
                    icon = icon,
                    tint = tint
                )
                ControlLayout.Card -> ClimateCard(
                    label = entity.displayName,
                    valueText = entity.valueText,
                    subvalueText = entity.subvalueText,
                    icon = icon,
                    tint = tint,
                    modifier = modifier
                )
            }
        }

        // The card is the only form a thermostat has, so both layouts render it — the controls need
        // more room than an entity row gives, and every group that shows one uses cards anyway.
        is EntityUi.Thermostat -> ThermostatControlCard(
            ui = entity,
            onSetTarget = { temp -> onAction(EntityAction.SetTargetTemperature(entity.entityId, temp)) },
            onSetHvacMode = { mode -> onAction(EntityAction.SetHvacMode(entity.entityId, mode)) },
            onSetFanMode = { mode -> onAction(EntityAction.SetThermostatFanMode(entity.entityId, mode)) },
            onSetPresetMode = { mode -> onAction(EntityAction.SetPresetMode(entity.entityId, mode)) },
            // Targets the helper entity, so a thermostat without one can't emit this at all.
            onSetExtreme = { on ->
                entity.metadata.extremeToggle?.let {
                    onAction(EntityAction.SetExtremeTemperatures(it.entityId, on))
                }
                Unit
            },
            modifier = modifier,
        )

        is EntityUi.Door -> DoorRow(
            DoorUi(
                label = entity.displayName,
                statusText = entity.statusText,
                open = entity.open,
                unavailable = entity.unavailable,
            ),
        )

        is EntityUi.Navigation -> {
            val onClick = { onAction(EntityAction.Navigate(entity.metadata.destination)) }
            when (layout) {
                ControlLayout.Row -> NavigationCard(entity, onClick, modifier)
                ControlLayout.Card -> NavigationTile(entity, onClick, modifier)
            }
        }

        // One-shot action, so the same tile in either layout.
        is EntityUi.Trigger -> TriggerCard(
            ui = entity,
            onActivate = { onAction(EntityAction.Activate(entity.metadata.action())) },
            modifier = modifier,
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

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun EntityThermostatCardPreview() = ControlPreview {
    EntityControl(previewThermostat("Thermostat"), ControlLayout.Card, {})
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityClimateCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewClimate("Temperature", "72.5 °F", ClimateMetadata.ClimateKind.Temperature), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewClimate("Humidity", "48 %", ClimateMetadata.ClimateKind.Humidity), ControlLayout.Card, {}, Modifier.weight(1f))
        EntityControl(previewClimate("Dew Point", "50.9 °F", ClimateMetadata.ClimateKind.DewPoint), ControlLayout.Card, {}, Modifier.weight(1f))
    }
}
