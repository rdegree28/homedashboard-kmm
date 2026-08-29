package com.degree.homedash.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.core.DeviceControl
import com.degree.homedash.shared.model.device_metadata.*
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata

/** An action a rendered [EntityUi] can emit; screens map these to their ViewModel at one boundary. */
sealed interface EntityAction {
    data class Toggle(val entityId: String) : EntityAction
    data class SetSpeed(val entityId: String, val percentage: Int) : EntityAction
    data class SetOscillating(val entityId: String, val oscillating: Boolean) : EntityAction

    /** [entityId] is the mister's own `humidifier.*` entity, not the fan's. */
    data class SetMisting(val entityId: String, val misting: Boolean) : EntityAction

    data class OpenGraph(val entityId: String) : EntityAction

    /** Carries the typed destination rather than an entity id — a nav card's id is synthetic. */
    data class Navigate(val target: NavigationMetadata.NavigationTarget) : EntityAction
}

/**
 * How many grid columns this entity's card should span (out of the grid's 2). Two types go wide:
 * a thermostat always (its stepper and mode pills need the room, and a card that changed width as
 * the mode changed would shuffle the grid under the user's finger), and a fan only while it is
 * showing its speed slider — the same condition `FanControl` uses for `showSlider`; otherwise
 * a fan is a single-column toggle tile like the rest.
 * `ControlGroup` packs rows to a total width of 2 using this, so cards reflow as fans toggle.
 */
fun EntityUi.cardSpan(): Int = when {
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
    onAction: (EntityAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (entity) {
        is EntityUi.Navigation -> {
            val onClick = { onAction(EntityAction.Navigate(entity.metadata.destination)) }
            NavigationTile(entity, onClick, modifier)
        }

        else -> throw IllegalStateException("Unknown control $entity")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityLightCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewLight("On", isOn = true),  {}, Modifier.weight(1f))
        EntityControl(previewLight("Off", isOn = false), {}, Modifier.weight(1f))
        EntityControl(previewLight("Offline", offline = true),  {}, Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityFanCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewFanUi("On", isOn = true, percentage = 75, levelCount = 12), {}, Modifier.weight(1f))
        EntityControl(previewFanUi("Off", isOn = false), {}, Modifier.weight(1f))
        EntityControl(previewFanUi("Offline", offline = true),  {}, Modifier.weight(1f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F, widthDp = 380)
@Composable
private fun EntityThermostatCardPreview() = ControlPreview {
    DeviceControl(previewThermostat("Thermostat"))
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1B1F)
@Composable
private fun EntityClimateCardPreview() = ControlPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EntityControl(previewClimate("Temperature", "72.5 °F", ClimateMetadata.ClimateKind.Temperature), {}, Modifier.weight(1f))
        EntityControl(previewClimate("Humidity", "48 %", ClimateMetadata.ClimateKind.Humidity),  {}, Modifier.weight(1f))
        EntityControl(previewClimate("Dew Point", "50.9 °F", ClimateMetadata.ClimateKind.DewPoint), {}, Modifier.weight(1f))
    }
}
