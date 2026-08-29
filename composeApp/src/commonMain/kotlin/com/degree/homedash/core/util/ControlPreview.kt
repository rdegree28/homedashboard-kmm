package com.degree.homedash.core.util

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
import com.degree.homedash.core.device.DeviceUi
import com.degree.homedash.core.device.FanDeviceUi
import com.degree.homedash.core.device.LightDeviceUi
import com.degree.homedash.core.device.NavigationDeviceUi
import com.degree.homedash.core.device.ThermostatDeviceUi
import com.degree.homedash.core.device.TriggerDeviceUi
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.device_metadata.*
import com.degree.homedash.shared.model.device_metadata.factories.livingRoomThermostat
import com.degree.homedash.shared.model.device_state.FanState
import com.degree.homedash.shared.model.device_state.LightState
import com.degree.homedash.shared.model.device_state.ThermostatState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlin.math.sin
import org.koin.compose.KoinApplicationPreview
import org.koin.dsl.module

// Shared sample data + scaffolding for the control previews (which live next to each composable).

/** Two days of half-hourly samples, for previews of the controls that chart history. */
internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

/**
 * Supplies the Koin graph a preview needs.
 *
 * A [DeviceUi] carries its own behavior and reaches for an
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

/** A light device, for previews of screens that render [LightDeviceUi]. */
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

/** A fan device, for previews of the fan card's four shapes. */
internal fun previewFanDevice(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
    canOscillate: Boolean = false,
    oscillating: Boolean = false,
    canMist: Boolean = false,
    misting: Boolean = false,
): FanDeviceUi {
    val entityId = "fan.${name.lowercase().replace(' ', '_')}"
    return FanDeviceUi(
        metadata = FanMetadata(
            entityId = entityId,
            displayName = name,
            speedAdjustment = FanMetadata.SpeedAdjustment.forLevelCount(levelCount),
            hasOscillationFeature = canOscillate,
            misting = if (canMist) FanMetadata.MistingControl("humidifier.${name.lowercase()}") else null,
        ),
        state = FanState(
            entityId = entityId,
            isOn = isOn,
            isOffline = offline,
            percentage = percentage,
            isOscillating = oscillating,
            isMisting = misting,
        ),
    )
}

internal fun previewNavigation(
    target: NavigationMetadata.NavigationTarget,
    label: String = target.name,
    icon: NavigationMetadata.RoomIcon = defaultIconFor(target),
    tint: Long = defaultTintFor(target),
    photo: NavigationMetadata.CardPhoto? = defaultPhotoFor(target),
) = NavigationDeviceUi(NavigationMetadata(target, label, icon, tint, photo))

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
): ThermostatDeviceUi {
    val roster = ThermostatMetadata.livingRoomThermostat(
        entityId = "climate.${name.lowercase().replace(' ', '_')}",
        displayName = name,
    )
    val metadata = roster.copy(
        hvacModes = hvacModes ?: roster.hvacModes,
        fanModes = fanModes ?: roster.fanModes,
        presetModes = presetModes ?: roster.presetModes,
        temperaturePresets = temperaturePresets ?: roster.temperaturePresets,
        extremeToggle = if (hasExtremeToggle) roster.extremeToggle else null,
    )
    return ThermostatDeviceUi(
        metadata = metadata,
        state = ThermostatState(
            entityId = metadata.entityId,
            isOffline = offline,
            hvacMode = if (offline) null else mode,
            hvacAction = if (offline) null else action,
            targetTemperature = if (offline) null else target,
            currentTemperature = if (offline) null else current,
            currentHumidity = if (offline) null else humidity,
            fanMode = if (offline) null else fanMode,
            presetMode = if (offline) null else presetMode,
            extremeActive = extremeActive,
        ),
    )
}
