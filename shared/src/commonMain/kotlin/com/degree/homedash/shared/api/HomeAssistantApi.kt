package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * Typed Home Assistant surface the repository layer depends on — the single boundary between the app
 * and the transport. Implementations hide the wire-protocol concerns (command encoding, response
 * parsing) so callers work in terms of states, connection, service calls, and history rather than raw
 * WebSocket messages. [WebSocketHomeAssistantApi] is the production implementation.
 */
internal interface HomeAssistantApi {

    /** Live map of entity id → latest [EntityState], updated as Home Assistant pushes state changes. */
    val states: StateFlow<Map<String, EntityState>>

    /** Live connection status to the Home Assistant WebSocket API. */
    val connection: StateFlow<HaConnectionStatus>

    /** Open (or restart) the connection using [config], reconnecting automatically until [disconnect]. */
    fun connect(config: HaConfig)

    /** Close the connection and stop reconnecting. */
    fun disconnect()

    /**
     * Invoke the Home Assistant service `domain.service`, optionally targeting [entityId] and passing
     * [serviceData]. Fire-and-forget: any effect surfaces later via the [states] flow. Silently
     * dropped while disconnected.
     */
    suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    )

    /**
     * Fetch numeric history for [entityId] between [startIso] and [endIso] (ISO-8601 timestamps),
     * returning samples in time order. Non-numeric states are skipped; an empty list means no data in
     * the range (or the request failed).
     */
    suspend fun history(
        entityId: String,
        startIso: String,
        endIso: String,
    ): List<HistoryPoint>

    /**
     * Fetch [entityId]'s long-term statistics between [startIso] and [endIso], aggregated into
     * [period]-wide buckets carrying mean/min/max. Unlike [history] — which reads raw recorder states
     * and so only reaches back as far as the recorder's purge window — hourly and daily statistics are
     * retained indefinitely, so this is the only way to chart windows longer than that.
     *
     * Empty when the entity has no statistics (only sensors with a `state_class` get them) or the
     * request failed.
     */
    suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: HaProtocolHelper.StatisticsPeriod,
    ): List<HistoryPoint>
}
