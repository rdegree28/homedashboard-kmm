package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * Low-level Home Assistant transport that [HomeAssistantApi] is built on: live state/connection flows
 * plus the primitives for firing service calls and issuing request/response commands. [HaWebSocketClient]
 * is the production implementation over a WebSocket; tests substitute a fake so the API can be exercised
 * without a live connection.
 */
internal interface HaClient {

    /** Live map of entity id → latest [EntityState], updated as Home Assistant pushes state changes. */
    val states: StateFlow<Map<String, EntityState>>
    val expStates: StateFlow<Map<String, ExpEntityState>>

    /** Live connection status to the Home Assistant WebSocket API. */
    val connection: StateFlow<HaConnectionStatus>

    /** Open (or restart) the connection with [config], reconnecting automatically until [stop]. */
    fun start(config: HaConfig)

    /** Close the connection and stop reconnecting. */
    fun stop()

    /** Fire the `domain.service` service call (optionally targeting [entityId] with [serviceData]); no-op while disconnected. */
    suspend fun callService(domain: String, service: String, entityId: String?, serviceData: JsonObject? = null)

    /** Send the command built by [buildCommand] (given an allocated request id) and await its matching `result` text. */
    suspend fun request(buildCommand: (Long) -> String): String
}
