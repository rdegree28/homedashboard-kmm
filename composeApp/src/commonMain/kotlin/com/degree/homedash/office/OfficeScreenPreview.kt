package com.degree.homedash.office

import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import kotlin.math.sin

// Shared sample data for the Office screen preview.

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }

internal val previewOfficeUiState = OfficeUiState(
    connection = ConnectionStatus.Connected,
    officeLight = ToggleUi("Office", isOn = true, offline = false),
    smallLight = ToggleUi("Small", isOn = false, offline = false),
    officeFan = FanUi("Office Fan", isOn = true, offline = false, levelCount = 12, percentage = 75),
    boxFan = FanUi("Box Fan", isOn = false, offline = false, levelCount = 0, percentage = 0),
    activeSignal = "green",
    temperature = SensorUi("Temperature", "75.6 °F"),
    humidity = SensorUi("Humidity", "48.5 %"),
    door = DoorUi("Office Door", "Open", open = true, unavailable = false),
    workstation = ToggleUi("Workstation", isOn = true, offline = false),
    hexagon = ToggleUi("Hexagon Lights", isOn = false, offline = false),
    power = SensorUi("Power", "61.1 W"),
    energy = SensorUi("Total Power Used", "34.8 kWh"),
    powerHistory = previewHistory,
)
