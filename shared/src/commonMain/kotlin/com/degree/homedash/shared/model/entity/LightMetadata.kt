package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.states.LightState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A light.
 */
data class LightMetadata(
    override val entityId: String,
    override val displayName: String,
) : ToggleableDeviceMetadata, StatefulDeviceMetadata<LightState> {

    override fun loadState(
        repo: ExpHomeAssistantRepo,
    ): Flow<LightState> {
        return repo.entityForDevice(this).map { entity ->
            LightState(
                entityId = entityId,
                isOn = entity?.isOn == true,
                isOffline = entity == null || entity.isUnavailable,
            )
        }
    }
}
