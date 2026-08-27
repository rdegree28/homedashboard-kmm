package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonObject

/** Backs [ExpHomeAssistantRepo.preview]; every call is a no-op. */
internal object PreviewExpHomeAssistantApi : ExpHomeAssistantApi {
    override fun loadAllStates(): Flow<Map<String, EntityState>> = emptyFlow()

    // Emits (empty) rather than never, so a device combining history with its entities still produces a state.
    override fun loadHistoryForEntity(entityId: String, hoursBack: Int): Flow<List<HistoryPoint>> = flowOf(emptyList())

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
