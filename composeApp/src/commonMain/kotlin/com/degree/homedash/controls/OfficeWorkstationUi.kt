package com.degree.homedash.controls

import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.entity.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.states.OfficeWorkstationState

class OfficeWorkstationUi(
    private val metadata: OfficeWorkstationMetadata,
    private val state: OfficeWorkstationState,
) : DeviceUi {

    override val id: String = metadata.entityId
    override val cardSpan: Int = 2

    val name = metadata.displayName
    val isOn = state.isOn
    val isOffline = state.isOffline

    val totalPower: String = "10.0"
    val currentPower: String = "20.0"
    val powerHistoryPoints: List<HistoryPoint> = emptyList()
}