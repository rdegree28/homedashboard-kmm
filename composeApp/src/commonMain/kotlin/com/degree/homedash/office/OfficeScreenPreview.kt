package com.degree.homedash.office

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlin.math.sin

// Sample data for the Office preview (the @Preview lives in androidMain so Android Studio renders it).

private fun previewState(state: String) = EntityState(entityId = "preview", state = state)

internal val previewStates: Map<String, EntityState> = mapOf(
    OfficeEntities.OFFICE_LIGHT to previewState("on"),
    OfficeEntities.SMALL_LIGHT to previewState("off"),
    OfficeEntities.OFFICE_FAN to previewState("on"),
    OfficeEntities.BOX_FAN to previewState("off"),
    OfficeEntities.SIGNAL_MODE to previewState("green"),
    OfficeEntities.TEMPERATURE to previewState("75.6"),
    OfficeEntities.HUMIDITY to previewState("48.5"),
    OfficeEntities.DOOR to previewState("on"),
    OfficeEntities.WORKSTATION to previewState("on"),
    OfficeEntities.HEXAGON to previewState("off"),
    OfficeEntities.POWER to previewState("61.1"),
    OfficeEntities.ENERGY to previewState("34.8"),
)

internal val previewHistory: List<HistoryPoint> =
    List(48) { i -> HistoryPoint(timeSeconds = i.toDouble(), value = (sin(i * 0.4) * 40 + 55).coerceAtLeast(0.0)) }
