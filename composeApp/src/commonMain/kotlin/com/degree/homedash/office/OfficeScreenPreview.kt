package com.degree.homedash.office

import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.controls.FanDeviceUi
import com.degree.homedash.controls.LightDeviceUi
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.model.states.FanState
import com.degree.homedash.shared.model.states.LightState
import kotlin.math.sin

// Shared sample data for the Office screen preview.

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

internal val previewOfficeUiState = OfficeUiState(
    connection = HaConnectionStatus.Connected,
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
        EntityUi.Climate(ClimateMetadata("sensor.temp", "Temperature", ClimateMetadata.ClimateKind.Temperature), "75.6 °F"),
        EntityUi.Climate(ClimateMetadata("sensor.humidity", "Humidity", ClimateMetadata.ClimateKind.Humidity), "48.5 %", subvalueText = "Dew pt 50.9 °F"),
    ),
    doors = listOf(
        EntityUi.Door(DoorMetadata("binary_sensor.office_door", "Office Door"), "Open", open = true, unavailable = false),
    ),
    activeSignal = "green",
    workstation = ToggleUi("Workstation", isOn = true, offline = false),
    hexagon = ToggleUi("Hexagon Lights", isOn = false, offline = false),
    power = SensorUi("Power", "61.1 W"),
    energy = SensorUi("Total Power Used", "34.8 kWh"),
    powerHistory = previewHistory,
)
