package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HaProtocolHelper.StatisticsPeriod
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonObject

/** In-memory [ExpHomeAssistantApi] that records the calls the repo makes. */
internal class FakeExpHomeAssistantApi : ExpHomeAssistantApi {
    val states = MutableStateFlow<Map<String, EntityState>>(emptyMap())
    override val connection = MutableStateFlow<HaConnectionStatus>(HaConnectionStatus.Disconnected)

    val serviceCalls = mutableListOf<ServiceCall>()

    override fun loadAllStates(): Flow<Map<String, EntityState>> = states

    override fun connect(config: HaConfig) = Unit
    override fun disconnect() = Unit

    override fun toggleEntity(entityId: String) {
        serviceCalls += ServiceCall(entityId.substringBefore('.'), "toggle", entityId, null)
    }

    override fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?,
    ) {
        serviceCalls += ServiceCall(domain, service, entityId, serviceData)
    }

    val historyCalls = mutableListOf<HistoryCall>()
    val statisticsCalls = mutableListOf<StatisticsCall>()
    var historyResult: List<HistoryPoint> = emptyList()
    var statisticsResult: List<HistoryPoint> = emptyList()

    override suspend fun history(entityId: String, startIso: String, endIso: String): List<HistoryPoint> {
        historyCalls += HistoryCall(entityId, startIso, endIso)
        return historyResult
    }

    override suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: StatisticsPeriod,
    ): List<HistoryPoint> {
        statisticsCalls += StatisticsCall(entityId, startIso, endIso, period)
        return statisticsResult
    }

    data class HistoryCall(val entityId: String, val startIso: String, val endIso: String)

    data class StatisticsCall(
        val entityId: String,
        val startIso: String,
        val endIso: String,
        val period: StatisticsPeriod,
    )

    data class ServiceCall(
        val domain: String,
        val service: String,
        val entityId: String?,
        val serviceData: JsonObject?,
    )
}
