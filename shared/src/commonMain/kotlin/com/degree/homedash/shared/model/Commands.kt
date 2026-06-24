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

/** Result of a `state_changed` event: a removed entity has a null [newState]. */
data class StateChanged(
    val entityId: String,
    val newState: EntityState?,
)
