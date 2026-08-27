package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.toReading
import com.degree.homedash.shared.model.states.OfficeWorkstationState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The office workstation: a switch plus the two meters on its plug.
 *
 * A composite of four Home Assistant reads — the switch, the live power sensor, the running total,
 * and the power sensor's recent history — so [loadState] combines them rather than mapping one
 * entity. Only [toggleEntityId] is the device's identity; the meters are read-only.
 *
 * @param currentPowerEntityId the `sensor.*` reporting the live draw, and the series the chart plots.
 * @param totalPowerEntityId the `sensor.*` reporting energy used since the plug started counting.
 * @param powerHistoryHours how far back the chart reaches. A week, as the Office dashboard has always
 *   drawn — and inside the recorder's retention, so the samples come back whole (see
 *   `ExpHomeAssistantRepo.historyFor`).
 */
data class OfficeWorkstationMetadata(
    override val displayName: String,
    val toggleEntityId: String,

    val currentPowerEntityId: String,
    val totalPowerEntityId: String,
    val powerHistoryHours: Int = 24 * 7,
) : ToggleableDeviceMetadata, StatefulDeviceMetadata<OfficeWorkstationState> {
    override val entityId: String = toggleEntityId

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<OfficeWorkstationState> {
        return combine(
            repo.entityForDevice(this),
            repo.entityFor(currentPowerEntityId),
            repo.entityFor(totalPowerEntityId),
            repo.historyForEntity(currentPowerEntityId, powerHistoryHours),
        ) { workstation, currentPower, totalPower, history ->
            OfficeWorkstationState(
                entityId = entityId,
                isOn = workstation?.isOn == true,
                isOffline = workstation == null || workstation.isUnavailable,
                currentPower = currentPower.toReading(),
                totalPower = totalPower.toReading(),
                powerHistory = history,
            )
        }
    }
}
