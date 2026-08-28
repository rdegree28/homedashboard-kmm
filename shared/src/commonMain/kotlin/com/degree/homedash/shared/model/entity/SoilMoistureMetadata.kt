package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.states.SoilMoistureState
import com.degree.homedash.shared.model.toReading
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A plant soil-moisture sensor, rendered as a percentage gauge. Read-only, so identity is all it
 * carries — the level comes from the live state.
 */
data class SoilMoistureMetadata(
    override val entityId: String,
    override val displayName: String,
) : StatefulDeviceMetadata<SoilMoistureState> {

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<SoilMoistureState> =
        repo.entityForDevice(this).map { entity ->
            SoilMoistureState(
                entityId = entityId,
                isOffline = entity == null || entity.isUnavailable,
                reading = entity.toReading(),
            )
        }
}
