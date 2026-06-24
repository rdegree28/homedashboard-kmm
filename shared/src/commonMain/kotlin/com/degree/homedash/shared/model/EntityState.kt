package com.degree.homedash.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** A single Home Assistant entity's state, as delivered by `get_states` / `state_changed`. */
@Serializable
data class EntityState(
    @SerialName("entity_id") val entityId: String,
    val state: String,
    val attributes: JsonObject = JsonObject(emptyMap()),
    @SerialName("last_changed") val lastChanged: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
) {
    /** Domain portion of the id, e.g. `light` for `light.office_light`. */
    val domain: String get() = entityId.substringBefore('.')

    val isOn: Boolean get() = state.equals("on", ignoreCase = true)
    val isUnavailable: Boolean get() = state == "unavailable" || state == "unknown"

    val friendlyName: String? get() = attrString("friendly_name")

    fun attrString(key: String): String? = attributes[key]?.jsonPrimitive?.contentOrNull
    fun attrDouble(key: String): Double? = attributes[key]?.jsonPrimitive?.doubleOrNull
    fun attrInt(key: String): Int? = attributes[key]?.jsonPrimitive?.intOrNull
}
