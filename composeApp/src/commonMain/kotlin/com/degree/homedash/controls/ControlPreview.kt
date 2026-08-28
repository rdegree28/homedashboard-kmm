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
import com.degree.homedash.shared.model.states.LightState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import org.koin.compose.KoinApplicationPreview
import org.koin.dsl.module

// Shared sample data + scaffolding for the control previews (which live next to each composable).

/**
 * Supplies the Koin graph a preview needs.
 *
 * A [com.degree.homedash.controls.DeviceUi] carries its own behavior and reaches for an
 * [ExpHomeAssistantRepo] to run it through — `DeviceControl` resolves one with `koinInject()`. The app
 * provides that from its graph, but previews render outside it, so they register an inert repo
 * instead. Without this, any preview containing a device card throws instead of rendering.
 */
@Composable
internal fun PreviewKoin(content: @Composable () -> Unit) {
    KoinApplicationPreview(
        application = { modules(module { single { ExpHomeAssistantRepo.preview() } }) },
        content = content,
    )
}

/** Wraps control previews in the app's dark theme + a padded column. */
@Composable
internal fun ControlPreview(content: @Composable ColumnScope.() -> Unit) {
    PreviewKoin {
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

/** The device-stack counterpart of [previewLight], for previews of screens that render [LightDeviceUi]. */
internal fun previewLightDevice(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
): LightDeviceUi {
    val entityId = "light.${name.lowercase()}"
    return LightDeviceUi(
        metadata = LightMetadata(entityId, name),
        state = LightState(entityId = entityId, isOn = isOn, isOffline = offline),
    )
}

internal fun previewFan(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
    canOscillate: Boolean = false,
    oscillating: Boolean = false,
    canMist: Boolean = false,
    misting: Boolean = false,
) = FanUi(
    name = name,
    isOn = isOn,
    offline = offline,
    levelCount = levelCount,
    percentage = percentage,
    canOscillate = canOscillate,
    oscillating = oscillating,
    canMist = canMist,
    misting = misting,
)

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
    photo: NavigationMetadata.CardPhoto? = defaultPhotoFor(target),
) = EntityUi.Navigation(NavigationMetadata(target, label, icon, tint, photo))

/** Mirrors the pairing the repo declares, so previews match the real launcher. */
private fun defaultIconFor(target: NavigationMetadata.NavigationTarget) = when (target) {
    NavigationMetadata.NavigationTarget.Office -> NavigationMetadata.RoomIcon.Desk
    NavigationMetadata.NavigationTarget.Plants -> NavigationMetadata.RoomIcon.Plant
    NavigationMetadata.NavigationTarget.LivingRoom -> NavigationMetadata.RoomIcon.Sofa
    NavigationMetadata.NavigationTarget.Bedroom -> NavigationMetadata.RoomIcon.Bed
    NavigationMetadata.NavigationTarget.Pets -> NavigationMetadata.RoomIcon.Paw
}

/** Likewise for photos — only the Pets card ships one today. */
private fun defaultPhotoFor(target: NavigationMetadata.NavigationTarget) = when (target) {
    NavigationMetadata.NavigationTarget.Office,
    NavigationMetadata.NavigationTarget.Plants,
    NavigationMetadata.NavigationTarget.LivingRoom,
    NavigationMetadata.NavigationTarget.Bedroom,
    -> null
    NavigationMetadata.NavigationTarget.Pets -> NavigationMetadata.CardPhoto.Callie
}

/** Likewise for tints — keeps preview cards coloured the way the launcher colours them. */
private fun defaultTintFor(target: NavigationMetadata.NavigationTarget) = when (target) {
    NavigationMetadata.NavigationTarget.Office -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.Plants -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.LivingRoom -> 0xFFCCCCCC
    NavigationMetadata.NavigationTarget.Bedroom -> 0xFFCCCCCC
    // The real gold, unlike its neutral neighbours: this card's photo is ringed in its tint, so a
    // placeholder grey would show a ring the launcher never draws.
    NavigationMetadata.NavigationTarget.Pets -> 0xFFC29844
}

internal fun previewTrigger(label: String) = TriggerDeviceUi(
    TriggerDeviceMetadata(
        entityId = "trigger.${label.lowercase().replace(' ', '_')}",
        displayName = label,
        targetEntityId = "scene.${label.lowercase().replace(' ', '_')}",
    ),
)

/** Defaults mirror the live Living Room thermostat, so previews match what the app really shows. */
/**
 * Built from the real Living Room roster entry rather than a hand-assembled [ThermostatMetadata], so
 * a preview can't quietly disagree with what the app ships — every null override below inherits
 * whatever the factory declares, including capabilities added to it later.
 */
internal fun previewThermostat(
    name: String,
    offline: Boolean = false,
    mode: HvacMode? = HvacMode.Cool,
    action: HvacAction? = HvacAction.Idle,
    target: Double? = 72.0,
    current: Double? = 70.0,
    humidity: Double? = 59.0,
    extremeActive: Boolean = false,
    hvacModes: List<HvacMode>? = null,
    fanModes: List<String>? = null,
    fanMode: String? = null,
    presetModes: List<String>? = null,
    presetMode: String? = null,
    temperaturePresets: List<TemperaturePreset>? = null,
    // A flag rather than a nullable override, since null already means "inherit from the roster".
    hasExtremeToggle: Boolean = true,
): EntityUi.Thermostat {
    val roster = ThermostatMetadata.livingRoomThermostat(
        entityId = "climate.${name.lowercase().replace(' ', '_')}",
        displayName = name,
    )
    return EntityUi.Thermostat(
        metadata = roster.copy(
            hvacModes = hvacModes ?: roster.hvacModes,
            fanModes = fanModes ?: roster.fanModes,
            presetModes = presetModes ?: roster.presetModes,
            temperaturePresets = temperaturePresets ?: roster.temperaturePresets,
            extremeToggle = if (hasExtremeToggle) roster.extremeToggle else null,
        ),
        offline = offline,
        hvacMode = if (offline) null else mode,
        hvacAction = if (offline) null else action,
        targetTemperature = if (offline) null else target,
        currentTemperature = if (offline) null else current,
        currentHumidity = if (offline) null else humidity,
        fanMode = if (offline) null else fanMode,
        presetMode = if (offline) null else presetMode,
        extremeActive = extremeActive,
    )
}

internal fun previewClimate(
    label: String,
    valueText: String,
    kind: ClimateMetadata.ClimateKind,
) = EntityUi.Climate(
    metadata = ClimateMetadata("sensor.${label.lowercase()}", label, kind = kind),
    valueText = valueText,
)
