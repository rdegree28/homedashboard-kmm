package com.degree.homedash.office

import com.degree.homedash.shared.model.entity.*
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.api.HaConnectionStatus
import kotlin.math.sin

// Shared sample data for the Office screen preview.

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

internal val previewOfficeUiState = OfficeUiState(
    connection = HaConnectionStatus.Connected,
    lights = listOf(
        EntityUi.Light(LightMetadata("light.office", "Office"), isOn = true, offline = false),
        EntityUi.Light(LightMetadata("light.small", "Small"), isOn = false, offline = false),
    ),
    fans = listOf(
        EntityUi.Fan(FanMetadata("fan.office", "Office Fan", FanMetadata.SpeedAdjustment(12)), isOn = true, offline = false, percentage = 75),
        EntityUi.Fan(FanMetadata("fan.box", "Box Fan"), isOn = false, offline = false, percentage = 0),
        EntityUi.Fan(FanMetadata("fan.misting", "Misting Fan"), isOn = false, offline = false, percentage = 0),
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
