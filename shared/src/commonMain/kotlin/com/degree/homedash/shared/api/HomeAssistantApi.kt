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
}
