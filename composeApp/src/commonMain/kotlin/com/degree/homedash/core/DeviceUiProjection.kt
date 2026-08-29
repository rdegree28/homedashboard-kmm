package com.degree.homedash.core

import com.degree.homedash.core.device.ClimateDeviceUi
import com.degree.homedash.core.device.DeviceUi
import com.degree.homedash.core.device.DoorDeviceUi
import com.degree.homedash.core.device.FanDeviceUi
import com.degree.homedash.core.device.LightDeviceUi
import com.degree.homedash.core.device.OfficeSignalDeviceUi
import com.degree.homedash.core.device.OfficeWorkstationUi
import com.degree.homedash.core.device.PetFountainDeviceUi
import com.degree.homedash.core.device.SoilMoistureDeviceUi
import com.degree.homedash.core.device.ThermostatDeviceUi
import com.degree.homedash.core.device.TriggerDeviceUi
import com.degree.homedash.shared.model.device_metadata.ClimateMetadata
import com.degree.homedash.shared.model.device_metadata.DeviceMetadata
import com.degree.homedash.shared.model.device_metadata.DoorMetadata
import com.degree.homedash.shared.model.device_metadata.FanMetadata
import com.degree.homedash.shared.model.device_metadata.LightMetadata
import com.degree.homedash.shared.model.device_metadata.NavigationMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeSignalMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.device_metadata.PetFountainMetadata
import com.degree.homedash.shared.model.device_metadata.SoilMoistureMetadata
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.device_metadata.TriggerDeviceMetadata
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
 * Null only for [NavigationMetadata], which the launcher builds itself; every other metadata type
 * has a device control, and leaving the `when` exhaustive means a new one can't be forgotten here.
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

    is OfficeSignalMetadata -> {
        val metadata = this
        loadState(repo).map { state -> OfficeSignalDeviceUi(metadata = metadata, state = state) }
    }

    is OfficeWorkstationMetadata -> {
        val metadata = this
        loadState(repo).map { state -> OfficeWorkstationUi(metadata = metadata, state = state) }
    }

    is ThermostatMetadata -> {
        val metadata = this
        loadState(repo).map { state -> ThermostatDeviceUi(metadata = metadata, state = state) }
    }

    is PetFountainMetadata -> {
        val metadata = this
        loadState(repo).map { state -> PetFountainDeviceUi(metadata = metadata, state = state) }
    }

    is SoilMoistureMetadata -> {
        val metadata = this
        loadState(repo).map { state -> SoilMoistureDeviceUi(metadata = metadata, state = state) }
    }

    // Stateless: a trigger fires a service and reports nothing, so its card never changes. A constant
    // flow rather than no flow, so the roster's `combine` still sees it.
    is TriggerDeviceMetadata -> flowOf(TriggerDeviceUi(metadata = this))

    // Built directly by `HomeViewModel`, not projected here: a launcher card has no Home Assistant
    // entity to read, and the roster it belongs to is the launcher's own.
    is NavigationMetadata -> null
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
