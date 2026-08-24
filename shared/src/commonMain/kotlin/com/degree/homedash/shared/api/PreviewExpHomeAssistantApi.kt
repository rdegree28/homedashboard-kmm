package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.JsonObject

/** Backs [ExpHomeAssistantRepo.preview]; every call is a no-op. */
internal object PreviewExpHomeAssistantApi : ExpHomeAssistantApi {
    override fun loadAllStates(): Flow<ExpStateSnapshot> = emptyFlow()

    override fun toggleEntity(entityId: String) = Unit
    override fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?
    ) {
        // Not implemented
    }
}
