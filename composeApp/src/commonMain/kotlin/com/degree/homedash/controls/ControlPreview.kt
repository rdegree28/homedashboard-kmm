package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.degree.homedash.office.FanUi
import com.degree.homedash.office.ToggleUi

// Shared sample data + scaffolding for the control previews (which live next to each composable).

/** Wraps control previews in the app's dark theme + a padded column. */
@Composable
internal fun ControlPreview(content: @Composable ColumnScope.() -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content,
            )
        }
    }
}

internal fun previewToggle(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
) = ToggleUi(name = name, isOn = isOn, offline = offline)

internal fun previewLight(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
) = EntityUi.Light(
    metadata = LightMetadata("light.${name.lowercase()}", name),
    isOn = isOn,
    offline = offline,
)

internal fun previewFan(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
) = FanUi(name = name, isOn = isOn, offline = offline, levelCount = levelCount, percentage = percentage)

internal fun previewFanUi(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
) = EntityUi.Fan(
    metadata = FanMetadata(
        "fan.${name.lowercase()}",
        name,
        FanMetadata.SpeedAdjustment.forLevelCount(levelCount),
    ),
    isOn = isOn,
    offline = offline,
    percentage = percentage,
)

internal fun previewNavigation(
    target: NavigationMetadata.NavigationTarget,
    label: String = target.name,
    icon: NavigationMetadata.RoomIcon = defaultIconFor(target),
    tint: Long = defaultTintFor(target),
) = EntityUi.Navigation(NavigationMetadata(target, label, icon, tint))

/** Mirrors the pairing the repo declares, so previews match the real launcher. */
private fun defaultIconFor(target: NavigationMetadata.NavigationTarget) = when (target) {
    NavigationMetadata.NavigationTarget.Office -> NavigationMetadata.RoomIcon.Desk
    NavigationMetadata.NavigationTarget.Plants -> NavigationMetadata.RoomIcon.Plant
    NavigationMetadata.NavigationTarget.LivingRoom -> NavigationMetadata.RoomIcon.Sofa
    NavigationMetadata.NavigationTarget.Bedroom -> NavigationMetadata.RoomIcon.Bed
    NavigationMetadata.NavigationTarget.Pets -> NavigationMetadata.RoomIcon.Paw
}

/** Likewise for tints — keeps preview cards coloured the way the launcher colours them. */
private fun defaultTintFor(target: NavigationMetadata.NavigationTarget) = when (target) {
    NavigationMetadata.NavigationTarget.Office -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.Plants -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.LivingRoom -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.Bedroom -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.Pets -> 0xFFCCCCCC
}

internal fun previewTrigger(label: String) = EntityUi.Trigger(
    object : TriggerEntityMetadata("trigger.${label.lowercase().replace(' ', '_')}", label) {
        override fun action() = ServiceCall.turnOn("scene.${label.lowercase().replace(' ', '_')}")
    },
)

internal fun previewClimate(
    label: String,
    valueText: String,
    kind: ClimateMetadata.ClimateKind,
) = EntityUi.Climate(
    metadata = ClimateMetadata("sensor.${label.lowercase()}", label, kind = kind),
    valueText = valueText,
)
