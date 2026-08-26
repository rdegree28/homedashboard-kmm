package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.states.OfficeWorkstationState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class OfficeWorkstationMetadata(
    override val displayName: String,
    val toggleEntityId: String,

    val currentPowerEntityId: String,
    val totalPowerEntityId: String,
) : ToggleableDeviceMetadata, StatefulDeviceMetadata<OfficeWorkstationState> {
    override val entityId: String = toggleEntityId

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<OfficeWorkstationState> {
        return repo.entityForDevice(this).map { entity ->
            OfficeWorkstationState(
                entityId = entityId,
                isOn = entity?.isOn == true,
                isOffline = entity == null || entity.isUnavailable,
            )
        }
    }
}