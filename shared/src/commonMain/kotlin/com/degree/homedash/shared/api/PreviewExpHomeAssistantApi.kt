package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.JsonObject

/** Backs [ExpHomeAssistantRepo.preview]; every call is a no-op. */
internal object PreviewExpHomeAssistantApi : ExpHomeAssistantApi {
    override fun loadAllStates(): Flow<Map<String, EntityState>> = emptyFlow()

    // Never connects, so a preview shows whatever its hand-built state says rather than a live one.
    override val connection: StateFlow<HaConnectionStatus> = MutableStateFlow(HaConnectionStatus.Disconnected)

    override fun connect(config: HaConfig) = Unit
    override fun disconnect() = Unit

    override suspend fun history(entityId: String, startIso: String, endIso: String): List<HistoryPoint> = emptyList()

    override suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: HaProtocolHelper.StatisticsPeriod,
    ): List<HistoryPoint> = emptyList()

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
