package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

/**
 * [ExpHomeAssistantApi] over the live WebSocket connection.
 *
 * Passes [HaClient.states] straight through rather than keeping a second flow of its own, so there is
 * a single source of truth: one push from HA produces one emission, not two.
 */
internal class WebSocketExpHomeAssistantApi(
    private val client: HaClient,
    // Owned here rather than by the caller so a service call outlives the composable that fired it.
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ExpHomeAssistantApi {

    override fun loadAllStates(): Flow<Map<String, EntityState>> = client.states

    override val connection: StateFlow<HaConnectionStatus> get() = client.connection

    override fun connect(config: HaConfig) = client.start(config)

    override fun disconnect() = client.stop()

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

    override fun toggleEntity(entityId: String) {
        scope.launch {
            client.callService(
                domain = entityId.substringBefore('.'),
                service = "toggle",
                entityId = entityId,
            )
        }
    }

    /**
     * Invoke the Home Assistant service `domain.service`, optionally targeting [entityId] and passing
     * [serviceData]. Fire-and-forget: any effect surfaces later via the [states] flow. Silently
     * dropped while disconnected.
     */
    override fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?,
    ) {
        scope.launch {
            client.callService(domain, service, entityId, serviceData)
        }
    }
}
