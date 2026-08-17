package com.degree.homedash.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Outgoing Home Assistant WebSocket command/message payloads. */

@Serializable
data class AuthMessage(
    val type: String = "auth",
    @SerialName("access_token") val accessToken: String,
)

@Serializable
data class SimpleCommand(
    val id: Long,
    val type: String,
)

@Serializable
data class SubscribeEventsCommand(
    val id: Long,
    val type: String = "subscribe_events",
    @SerialName("event_type") val eventType: String = "state_changed",
)

@Serializable
data class Target(
    @SerialName("entity_id") val entityId: String,
)

@Serializable
data class CallServiceCommand(
    val id: Long,
    val type: String = "call_service",
    val domain: String,
    val service: String,
    val target: Target? = null,
    @SerialName("service_data") val serviceData: JsonObject? = null,
)

@Serializable
data class HistoryCommand(
    val id: Long,
    val type: String = "history/history_during_period",
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("entity_ids") val entityIds: List<String>,
    @SerialName("minimal_response") val minimalResponse: Boolean = true,
    @SerialName("no_attributes") val noAttributes: Boolean = true,
)

/**
 * Bucket width for a long-term statistics query. Home Assistant keeps `FIVE_MINUTE` buckets only for
 * the recorder's retention window (same as raw states); [HOUR] and [DAY] are kept indefinitely, which
 * is what makes history older than the purge horizon reachable at all.
 */
enum class StatisticsPeriod(val wire: String) {
    FIVE_MINUTE("5minute"),
    HOUR("hour"),
    DAY("day"),
}

@Serializable
data class StatisticsCommand(
    val id: Long,
    val type: String = "recorder/statistics_during_period",
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("statistic_ids") val statisticIds: List<String>,
    val period: String,
    val types: List<String> = listOf("mean", "min", "max"),
)

/** Result of a `state_changed` event: a removed entity has a null [newState]. */
data class StateChanged(
    val entityId: String,
    val newState: EntityState?,
)
