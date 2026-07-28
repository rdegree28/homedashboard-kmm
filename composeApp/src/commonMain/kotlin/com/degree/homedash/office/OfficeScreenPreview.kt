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
    officeLight = EntityUi.Light(LightMetadata("light.office"), "Office", isOn = true, offline = false),
    smallLight = EntityUi.Light(LightMetadata("light.small"), "Small", isOn = false, offline = false),
    officeFan = EntityUi.Fan(FanMetadata("fan.office", levelCount = 12), "Office Fan", isOn = true, offline = false, percentage = 75),
    boxFan = EntityUi.Fan(FanMetadata("fan.box", levelCount = 0), "Box Fan", isOn = false, offline = false, percentage = 0),
    mistingFan = EntityUi.Fan(FanMetadata("fan.misting", levelCount = 0), "Misting Fan", isOn = false, offline = false, percentage = 0),
    activeSignal = "green",
    temperature = EntityUi.Climate(ClimateMetadata("sensor.temp", ClimateMetadata.ClimateKind.Temperature), "Temperature", "75.6 °F"),
    humidity = EntityUi.Climate(ClimateMetadata("sensor.humidity", ClimateMetadata.ClimateKind.Humidity), "Humidity", "48.5 %", subvalueText = "Dew pt 50.9 °F"),
    door = EntityUi.Door(DoorMetadata("binary_sensor.office_door"), "Office Door", "Open", open = true, unavailable = false),
    workstation = ToggleUi("Workstation", isOn = true, offline = false),
    hexagon = ToggleUi("Hexagon Lights", isOn = false, offline = false),
    power = SensorUi("Power", "61.1 W"),
    energy = SensorUi("Total Power Used", "34.8 kWh"),
    powerHistory = previewHistory,
)
