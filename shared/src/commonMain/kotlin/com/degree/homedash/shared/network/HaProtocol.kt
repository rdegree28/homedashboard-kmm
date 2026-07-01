package com.degree.homedash.shared.network

import com.degree.homedash.shared.model.AuthMessage
import com.degree.homedash.shared.model.CallServiceCommand
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryCommand
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.SimpleCommand
import com.degree.homedash.shared.model.StateChanged
import com.degree.homedash.shared.model.SubscribeEventsCommand
import com.degree.homedash.shared.model.Target
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Pure (socket-free) encode/decode of the Home Assistant WebSocket protocol.
 * Mirrors the reference handshake in tools/ha-dashboard-loop/ha.mjs.
 * Kept side-effect free so it is fully unit-testable in commonTest.
 */
object HaProtocol {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }

    fun messageType(text: String): String? = runCatching {
        json.parseToJsonElement(text).jsonObject["type"]?.jsonPrimitive?.content
    }.getOrNull()

    fun resultId(text: String): Long? = runCatching {
        json.parseToJsonElement(text).jsonObject["id"]?.jsonPrimitive?.longOrNull
    }.getOrNull()

    fun isResultSuccess(text: String): Boolean = runCatching {
        json.parseToJsonElement(text).jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true
    }.getOrDefault(false)

    /** Parse the `result` array of a `get_states` response into entity states. */
    fun parseStates(text: String): List<EntityState> = runCatching {
        val arr = json.parseToJsonElement(text).jsonObject["result"]?.jsonArray ?: return emptyList()
        arr.map { json.decodeFromJsonElement(EntityState.serializer(), it) }
    }.getOrDefault(emptyList())

    /** Parse a `state_changed` event; [StateChanged.newState] is null when the entity was removed. */
    fun parseStateChanged(text: String): StateChanged? = runCatching {
        val data = json.parseToJsonElement(text).jsonObject["event"]
            ?.jsonObject?.get("data")?.jsonObject ?: return null
        val entityId = data["entity_id"]?.jsonPrimitive?.content ?: return null
        val ns = data["new_state"]
        val newState = if (ns == null || ns is JsonNull) {
            null
        } else {
            json.decodeFromJsonElement(EntityState.serializer(), ns)
        }
        StateChanged(entityId, newState)
    }.getOrNull()

    // --- encoders ---

    fun encodeAuth(token: String): String =
        json.encodeToString(AuthMessage.serializer(), AuthMessage(accessToken = token))

    fun encodeGetStates(id: Long): String =
        json.encodeToString(SimpleCommand.serializer(), SimpleCommand(id = id, type = "get_states"))

    fun encodeSubscribeStateChanged(id: Long): String =
        json.encodeToString(SubscribeEventsCommand.serializer(), SubscribeEventsCommand(id = id))

    fun encodeCallService(
        id: Long,
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject? = null,
    ): String = json.encodeToString(
        CallServiceCommand.serializer(),
        CallServiceCommand(
            id = id,
            domain = domain,
            service = service,
            target = entityId?.let { Target(it) },
            serviceData = serviceData,
        ),
    )

    fun encodeHistory(
        id: Long,
        entityId: String,
        startTimeIso: String,
        endTimeIso: String,
    ): String =
        json.encodeToString(
            HistoryCommand.serializer(),
            HistoryCommand(
                id = id,
                startTime = startTimeIso,
                endTime = endTimeIso,
                entityIds = listOf(entityId),
            ),
        )

    /**
     * Parse a `history/history_during_period` result into numeric samples for [entityId].
     * Entries use the compressed form: `s` = state value, `lu`/`lc` = last updated/changed (epoch s).
     * Non-numeric states (e.g. "unavailable") are skipped.
     */
    fun parseHistory(
        resultText: String,
        entityId: String,
    ): List<HistoryPoint> = runCatching {
        val arr = json.parseToJsonElement(resultText).jsonObject["result"]
            ?.jsonObject?.get(entityId)?.jsonArray ?: return emptyList()
        arr.mapNotNull { el ->
            val o = el.jsonObject
            val value = o["s"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: return@mapNotNull null
            val time = (o["lu"] ?: o["lc"])?.jsonPrimitive?.doubleOrNull ?: return@mapNotNull null
            HistoryPoint(timeSeconds = time, value = value)
        }
    }.getOrDefault(emptyList())
}
