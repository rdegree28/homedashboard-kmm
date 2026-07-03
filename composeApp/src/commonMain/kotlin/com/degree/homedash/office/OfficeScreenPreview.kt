package com.degree.homedash.office

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
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.network.ConnectionStatus
import kotlin.math.sin

// Shared sample data + scaffolding for the Office previews (which now live next to each composable).

/** Wraps control previews in the app's dark theme + a padded column. */
@Composable
internal fun ControlPreview(content: @Composable ColumnScope.() -> Unit) {
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

internal fun previewToggle(name: String, isOn: Boolean = false, offline: Boolean = false) =
    ToggleUi(name = name, isOn = isOn, offline = offline)

internal fun previewFan(
    name: String,
    isOn: Boolean = false,
    offline: Boolean = false,
    percentage: Int = 0,
    levelCount: Int = 0,
) = FanUi(name = name, isOn = isOn, offline = offline, levelCount = levelCount, percentage = percentage)

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
