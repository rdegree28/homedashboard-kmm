package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.entity.DeviceMetadata
import com.degree.homedash.shared.model.entity.DoorMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.LightMetadata
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * This device's control, re-derived whenever the entities behind it change.
 *
 * Projecting inside the branch is what keeps this cast-free: `this` is already the concrete metadata,
 * so `loadState` resolves through the type parameter to the matching state type, and the metadata is
 * captured rather than paired back up afterwards. The UI never sees an `EntityState` either — Home
 * Assistant's raw representation stays inside `:shared`.
 *
 * Null for metadata that has no device control yet — those types are still on the [EntityUi] path.
 */
fun DeviceMetadata.loadUi(repo: ExpHomeAssistantRepo): Flow<DeviceUi>? = when (this) {
    is LightMetadata -> {
        val metadata = this
        loadState(repo).map { state -> LightDeviceUi(metadata = metadata, state = state) }
    }

    is FanMetadata -> {
        val metadata = this
        loadState(repo).map { state -> FanDeviceUi(metadata = metadata, state = state) }
    }

    is ClimateMetadata -> {
        val metadata = this
        loadState(repo).map { state -> ClimateDeviceUi(metadata = metadata, state = state) }
    }

    is DoorMetadata -> {
        val metadata = this
        loadState(repo).map { state -> DoorDeviceUi(metadata = metadata, state = state) }
    }

    else -> null
//
//    is FanMetadata -> EntityUi.Fan(
//        metadata = this,
//        isOn = state?.isOn == true,
//        offline = state.isOffline(),
//        percentage = state?.attrDouble("percentage")?.roundToInt() ?: 0,
//        oscillating = state?.attrBoolean("oscillating") == true,
//        // Read off the mister's own entity, not the fan's.
//        misting = misting?.let { allStates[it.entityId]?.isOn == true } == true,
//    )
//
//    is ClimateMetadata -> EntityUi.Climate(
//        metadata = this,
//        valueText = state.readingText(decimals = 1),
//    )
//
//    is ThermostatMetadata -> {
//        val offline = state.isOffline()
//        EntityUi.Thermostat(
//            metadata = this,
//            offline = offline,
//            // An unavailable entity's state is literally "unavailable", so this already lands on null.
//            hvacMode = HvacMode.fromHa(state?.state),
//            // The attributes are guarded anyway: some integrations keep stale ones around while
//            // the device is gone, which would show a live-looking readout for an offline unit.
//            hvacAction = if (offline) null else HvacAction.fromHa(state?.attrString("hvac_action")),
//            // Null in heat_cool, where HA publishes target_temp_low/high instead — the stepper reads
//            // "—" and disables rather than pretending to drive a setpoint that isn't there.
//            targetTemperature = if (offline) null else state?.attrDouble("temperature"),
//            currentTemperature = if (offline) null else state?.attrDouble("current_temperature"),
//            currentHumidity = if (offline) null else state?.attrDouble("current_humidity"),
//            fanMode = if (offline) null else state?.attrString("fan_mode"),
//            presetMode = if (offline) null else state?.attrString("preset_mode"),
//            // Its own input_boolean, so it survives the thermostat being unavailable — the preset
//            // pills still need to show which setpoints they would write.
//            extremeActive = extremeToggle?.let { allStates[it.entityId]?.isOn == true } == true,
//        )
//    }
//
//    is DoorMetadata -> {
//        val open = state?.state == "on" // device_class opening: on = open
//        EntityUi.Door(
//            metadata = this,
//            statusText = when {
//                state.isOffline() -> "—"
//                open -> "Open"
//                else -> "Closed"
//            },
//            open = open,
//            unavailable = state.isOffline(),
//        )
//    }
//
//    is SoilMoistureMetadata -> {
//        val pct = state.percentOrNull()
//        EntityUi.SoilMoisture(
//            metadata = this,
//            pct = pct,
//            valueText = pct?.let { "${formatNumber(it, decimals = 1)} %" } ?: "—",
//        )
//    }
//
//    is WaterLevelMetadata -> {
//        val pct = state.percentOrNull()
//        EntityUi.WaterLevel(
//            metadata = this,
//            pct = pct,
//            valueText = pct?.let { "${formatNumber(it, decimals = 0)} %" } ?: "—",
//            // Read off the filter's own sensor, not the water level's — the same second-entity
//            // arrangement as a fan's mister.
//            filterDaysRemaining = filterHealth?.let { allStates[it.entityId].daysOrNull() },
//        )
//    }
//
//    // Deliberately ignores [state]: a launcher card has no Home Assistant entity behind it.
//    is NavigationMetadata -> EntityUi.Navigation(this)
//
//    // Likewise stateless — a trigger fires a service, it doesn't report anything.
//    is TriggerDeviceMetadata -> EntityUi.Trigger(this)
}

/**
 * A whole screen's controls, each re-emitting only when its own device changes.
 *
 * The empty case is guarded because `combine` over no flows never emits — without it a roster with no
 * stateful devices would stall the caller's own `combine` and leave the screen blank.
 */
fun List<DeviceMetadata>.loadDeviceUis(repo: ExpHomeAssistantRepo): Flow<List<DeviceUi>> {
    val flows = mapNotNull { metadata -> metadata.loadUi(repo) }
    if (flows.isEmpty()) return flowOf(emptyList())
    return combine(flows) { uis -> uis.toList() }
}
