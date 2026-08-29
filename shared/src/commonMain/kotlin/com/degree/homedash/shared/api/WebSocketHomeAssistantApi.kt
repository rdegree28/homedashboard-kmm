package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * WebSocket-backed [HomeAssistantApi]. Delegates live state/connection and service calls to [HaClient],
 * and handles the request/response wire protocol for history via [HaProtocolHelper] (command encoding +
 * response parsing).
 */
internal class WebSocketHomeAssistantApi(
    private val client: HaClient,
) : HomeAssistantApi {

    override val states: StateFlow<Map<String, EntityState>> get() = client.states

    override val connection: StateFlow<HaConnectionStatus> get() = client.connection

    override fun connect(config: HaConfig) = client.start(config)

    override fun disconnect() = client.stop()

    override suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?,
    ) = client.callService(domain, service, entityId, serviceData)

    override suspend fun history(
        entityId: String,
        startIso: String,
        endIso: String,
    ): List<HistoryPoint> {
        val text = client.request { id -> HaProtocolHelper.encodeHistory(id, entityId, startIso, endIso) }
        return HaProtocolHelper.parseHistory(text, entityId)
    }

    override suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: HaProtocolHelper.StatisticsPeriod,
    ): List<HistoryPoint> {
        val text = client.request { id ->
            HaProtocolHelper.encodeStatistics(id, entityId, startIso, endIso, period)
        }
        return HaProtocolHelper.parseStatistics(text, entityId)
    }
}
