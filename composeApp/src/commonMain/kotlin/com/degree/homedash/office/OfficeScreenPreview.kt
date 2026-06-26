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
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

internal fun previewEntity(state: String) = EntityState(entityId = "preview", state = state)

/** A sample fan entity carrying speed attributes so the speed slider renders in previews. */
internal fun previewFan(percentage: Int, stepPercent: Double = 8.333) = EntityState(
    entityId = "preview",
    state = "on",
    attributes = JsonObject(
        mapOf(
            "percentage" to JsonPrimitive(percentage),
            "percentage_step" to JsonPrimitive(stepPercent),
        ),
    ),
)

internal val previewStates: Map<String, EntityState> = mapOf(
    OfficeEntities.OFFICE_LIGHT to previewEntity("on"),
    OfficeEntities.SMALL_LIGHT to previewEntity("off"),
    OfficeEntities.OFFICE_FAN to previewEntity("on"),
    OfficeEntities.BOX_FAN to previewEntity("off"),
    OfficeEntities.SIGNAL_MODE to previewEntity("green"),
    OfficeEntities.TEMPERATURE to previewEntity("75.6"),
    OfficeEntities.HUMIDITY to previewEntity("48.5"),
    OfficeEntities.DOOR to previewEntity("on"),
    OfficeEntities.WORKSTATION to previewEntity("on"),
    OfficeEntities.HEXAGON to previewEntity("off"),
    OfficeEntities.POWER to previewEntity("61.1"),
    OfficeEntities.ENERGY to previewEntity("34.8"),
)

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }
