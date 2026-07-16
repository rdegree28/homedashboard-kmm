package com.degree.homedash.office

import com.degree.homedash.controls.ClimateKind
import com.degree.homedash.controls.EntityMetadata
import com.degree.homedash.controls.EntityUi
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import kotlin.math.sin

// Shared sample data for the Office screen preview.

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

internal val previewOfficeUiState = OfficeUiState(
    connection = ConnectionStatus.Connected,
    officeLight = EntityUi.Light(EntityMetadata.Light("light.office"), "Office", isOn = true, offline = false),
    smallLight = EntityUi.Light(EntityMetadata.Light("light.small"), "Small", isOn = false, offline = false),
    officeFan = EntityUi.Fan(EntityMetadata.Fan("fan.office", levelCount = 12), "Office Fan", isOn = true, offline = false, percentage = 75),
    boxFan = EntityUi.Fan(EntityMetadata.Fan("fan.box", levelCount = 0), "Box Fan", isOn = false, offline = false, percentage = 0),
    mistingFan = EntityUi.Fan(EntityMetadata.Fan("fan.misting", levelCount = 0), "Misting Fan", isOn = false, offline = false, percentage = 0),
    activeSignal = "green",
    temperature = EntityUi.Climate(EntityMetadata.Climate("sensor.temp", ClimateKind.Temperature), "Temperature", "75.6 °F"),
    humidity = EntityUi.Climate(EntityMetadata.Climate("sensor.humidity", ClimateKind.Humidity), "Humidity", "48.5 %", subvalueText = "Dew pt 50.9 °F"),
    door = EntityUi.Door(EntityMetadata.Door("binary_sensor.office_door"), "Office Door", "Open", open = true, unavailable = false),
    workstation = ToggleUi("Workstation", isOn = true, offline = false),
    hexagon = ToggleUi("Hexagon Lights", isOn = false, offline = false),
    power = SensorUi("Power", "61.1 W"),
    energy = SensorUi("Total Power Used", "34.8 kWh"),
    powerHistory = previewHistory,
)
