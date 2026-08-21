package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Public face of the experimental stack. [ExpHomeAssistantApi] is internal, so this forwards the
 * actions callers need rather than handing out an api.
 */
class ExpHomeAssistantRepo internal constructor(
    internal val api: ExpHomeAssistantApi,
) {

    fun loadEntityStatesForMetadatum(metadataList: List<EntityMetadata>): Flow<Map<EntityMetadata, ExpEntityState>> {
        return api.loadAllStates().map { stateMap ->
            metadataList.mapNotNull { meta ->
                stateMap[meta.entityId]?.let { state -> meta to state }
            }.toMap()
        }
    }

    fun toggleEntity(entity: EntityMetadata) {
        api.toggleEntity(entityId = entity.entityId)
    }
}
