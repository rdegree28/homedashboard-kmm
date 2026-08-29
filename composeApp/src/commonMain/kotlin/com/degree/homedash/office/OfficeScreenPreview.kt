package com.degree.homedash.office

import com.degree.homedash.controls.ClimateDeviceUi
import com.degree.homedash.shared.model.device_metadata.*
import com.degree.homedash.controls.FanDeviceUi
import com.degree.homedash.controls.DoorDeviceUi
import com.degree.homedash.controls.LightDeviceUi
import com.degree.homedash.controls.OfficeSignalDeviceUi
import com.degree.homedash.controls.OfficeWorkstationUi
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.device_state.ClimateState
import com.degree.homedash.shared.model.device_state.FanState
import com.degree.homedash.shared.model.device_state.DoorState
import com.degree.homedash.shared.model.device_state.LightState
import com.degree.homedash.shared.model.device_state.OfficeSignalState
import com.degree.homedash.shared.model.device_state.OfficeWorkstationState
import kotlin.math.sin

// Shared sample data for the Office screen preview.

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

internal val previewOfficeUiState = OfficeUiState(
    lights = listOf(
        LightDeviceUi(LightMetadata("light.office", "Office"), state = LightState(entityId = "light.office", isOn = true, isOffline = false)),
        LightDeviceUi(LightMetadata("light.small", "Small"), state = LightState(entityId = "light.small", isOn = false, isOffline = false)),
    ),
    fans = listOf(
        FanDeviceUi(FanMetadata("fan.office", "Office Fan", FanMetadata.SpeedAdjustment(12)),
            FanState("fan.office", true, false, percentage = 8, isOscillating = true, isMisting = false)
        ),
        FanDeviceUi(FanMetadata("fan.box", "Box Fan"),
            FanState("fan.box", true, false, percentage = 1, isOscillating = false, isMisting = false)
        ),
        FanDeviceUi(FanMetadata("fan.misting", "Misting Fan"),
            FanState("fan.misting", true, false, percentage = 1, isOscillating = false, isMisting = true)
        ),
    ),
    climate = listOf(
        ClimateDeviceUi(ClimateMetadata("sensor.temp", "Temperature", ClimateMetadata.ClimateKind.Temperature),
            ClimateState("sensor.temp", false, HistoricalEntityReading(75.6, "°F"))),
        ClimateDeviceUi(ClimateMetadata("sensor.humidity", "Humidity", ClimateMetadata.ClimateKind.Humidity),
            ClimateState("sensor.humidity", false, HistoricalEntityReading(48.5, "%"))),
    ),
    doors = listOf(
        DoorDeviceUi(
            DoorMetadata("binary_sensor.office_door", "Office Door"),
            DoorState(entityId = "binary_sensor.office_door", isOffline = false, isOpen = true),
        ),
    ),
    signal = OfficeSignalDeviceUi(
        OfficeSignalMetadata(
            entityId = "sensor.office_signal_mode",
            displayName = "Signal",
            trafficLight = "light.office_traffic_signal",
            modeScripts = emptyMap(),
        ),
        OfficeSignalState(
            entityId = "sensor.office_signal_mode",
            isOffline = false,
            mode = OfficeSignalMetadata.SignalMode.AVAILABLE,
        ),
    ),
    workstation = OfficeWorkstationUi(
        metadata = OfficeWorkstationMetadata(
            displayName = "Workstation",
            toggleEntityId = "switch.office_workstation",
            currentPowerEntityId = "sensor.office_workstation_power",
            totalPowerEntityId = "sensor.office_workstation_summation_delivered",
        ),
        state = OfficeWorkstationState(
            entityId = "switch.office_workstation",
            isOn = true,
            isOffline = false,
            currentPower = HistoricalEntityReading(61.1, "W"),
            totalPower = HistoricalEntityReading(34.8, "kWh"),
            powerHistory = previewHistory,
        ),
    ),
)
