package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.states.DoorState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A door contact sensor (HA `binary_sensor` with device class `opening`). Read-only, so identity is
 * all it carries — open/closed comes from the live state.
 */
data class DoorMetadata(
    override val entityId: String,
    override val displayName: String,
) : StatefulDeviceMetadata<DoorState> {

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<DoorState> =
        repo.entityForDevice(this).map { entity ->
            DoorState(
                entityId = entityId,
                isOffline = entity == null || entity.isUnavailable,
                // device_class `opening`: on = open.
                isOpen = entity?.isOn == true,
            )
        }
}
