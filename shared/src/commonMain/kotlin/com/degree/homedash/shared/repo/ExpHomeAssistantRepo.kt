package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HomeAssistantActionApi
import com.degree.homedash.shared.api.HomeAssistantStateApi
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpHomeAssistantRepo(
    private val actionApi: HomeAssistantActionApi,
    private val stateApi: HomeAssistantStateApi,
) {

    fun loadEntityStatesForMetadatum(metadataList: List<EntityMetadata>): Flow<Map<EntityMetadata, ExpEntityState>> {
        return stateApi.loadAllStates().map { stateMap ->
            metadataList.mapNotNull { meta ->
                stateMap[meta.entityId]?.let { state -> meta to state }
            }.toMap()
        }
    }

}