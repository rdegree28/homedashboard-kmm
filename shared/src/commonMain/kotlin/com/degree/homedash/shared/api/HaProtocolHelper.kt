package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.AuthMessage
import com.degree.homedash.shared.model.CallServiceCommand
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryCommand
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.SimpleCommand
import com.degree.homedash.shared.model.StateChanged
import com.degree.homedash.shared.model.StatisticsCommand
import com.degree.homedash.shared.model.StatisticsPeriod
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
internal object HaProtocolHelper {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }

    /**
     * Parse a frame into its root object, or null if it isn't valid JSON.
     *
     * Callers that read more than one field should parse once with this and use the [JsonObject]
     * overloads below — the `get_states` reply is a few hundred KB, so re-parsing it per field is the
     * difference between one pass and four.
     */
    fun parseRoot(text: String): JsonObject? = runCatching {
        json.parseToJsonElement(text).jsonObject
    }.getOrNull()

    fun messageType(root: JsonObject): String? = root["type"]?.jsonPrimitive?.contentOrNull

    fun messageType(text: String): String? = parseRoot(text)?.let(::messageType)

    fun resultId(root: JsonObject): Long? = root["id"]?.jsonPrimitive?.longOrNull

    fun resultId(text: String): Long? = parseRoot(text)?.let(::resultId)

    fun isResultSuccess(root: JsonObject): Boolean =
        root["success"]?.jsonPrimitive?.booleanOrNull == true

    fun isResultSuccess(text: String): Boolean = parseRoot(text)?.let(::isResultSuccess) ?: false

    /** Parse the `result` array of a `get_states` response into entity states. */
    fun parseStates(root: JsonObject): List<EntityState> = runCatching {
        val arr = root["result"]?.jsonArray ?: return emptyList()
        arr.map { json.decodeFromJsonElement(EntityState.serializer(), it) }
    }.getOrDefault(emptyList())

    fun parseStates(text: String): List<EntityState> =
        parseRoot(text)?.let(::parseStates) ?: emptyList()

    /** Parse a `state_changed` event; [com.degree.homedash.shared.model.StateChanged.newState] is null when the entity was removed. */
    fun parseStateChanged(root: JsonObject): StateChanged? = runCatching {
        val data = root["event"]?.jsonObject?.get("data")?.jsonObject ?: return null
        val entityId = data["entity_id"]?.jsonPrimitive?.content ?: return null
        val ns = data["new_state"]
        val newState = if (ns == null || ns is JsonNull) {
            null
        } else {
            json.decodeFromJsonElement(EntityState.serializer(), ns)
        }
        StateChanged(entityId, newState)
    }.getOrNull()

    fun parseStateChanged(text: String): StateChanged? = parseRoot(text)?.let(::parseStateChanged)

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

    fun encodeStatistics(
        id: Long,
        entityId: String,
        startTimeIso: String,
        endTimeIso: String,
        period: StatisticsPeriod,
    ): String =
        json.encodeToString(
            StatisticsCommand.serializer(),
            StatisticsCommand(
                id = id,
                startTime = startTimeIso,
                endTime = endTimeIso,
                statisticIds = listOf(entityId),
                period = period.wire,
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

    /**
     * Parse a `recorder/statistics_during_period` result into samples for [entityId]. Each entry is one
     * bucket: `start`/`end` are epoch *milliseconds* (unlike history's seconds) and the requested
     * `mean`/`min`/`max` come back as numbers. The bucket's start time is used as the sample time.
     *
     * The value is the mean where there is one, falling back to `state`/`sum` so meter-style sensors
     * (which record a total rather than a mean) still plot. Buckets with no usable number are skipped.
     */
    fun parseStatistics(
        resultText: String,
        entityId: String,
    ): List<HistoryPoint> = runCatching {
        val arr = json.parseToJsonElement(resultText).jsonObject["result"]
            ?.jsonObject?.get(entityId)?.jsonArray ?: return emptyList()
        arr.mapNotNull { el ->
            val o = el.jsonObject
            fun num(key: String) = o[key]?.jsonPrimitive?.doubleOrNull
            val startMs = num("start") ?: return@mapNotNull null
            val min = num("min")
            val max = num("max")
            val value = num("mean") ?: num("state") ?: num("sum") ?: max ?: min ?: return@mapNotNull null
            HistoryPoint(
                timeSeconds = startMs / 1000.0,
                value = value,
                // Keep min ≤ value ≤ max even if a bucket reports an odd combination, so charts that
                // draw the spread as a band never invert it.
                min = (min ?: value).coerceAtMost(value),
                max = (max ?: value).coerceAtLeast(value),
            )
        }
    }.getOrDefault(emptyList())
}