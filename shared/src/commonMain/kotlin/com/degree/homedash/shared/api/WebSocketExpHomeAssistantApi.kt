package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.states.ExpEntityState
import com.degree.homedash.shared.model.states.FanState
import com.degree.homedash.shared.model.states.LightState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlin.math.roundToInt

/**
 * [ExpHomeAssistantApi] over the live WebSocket connection.
 *
 * States are derived from [HaClient.states] rather than maintained as a second flow inside
 * [HaWebSocketClient], so there is a single source of truth: one push from HA produces one emission,
 * not two.
 */
internal class WebSocketExpHomeAssistantApi(
    private val client: HaClient,
    // Owned here rather than by the caller so a service call outlives the composable that fired it.
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ExpHomeAssistantApi {

    override fun loadAllStates(): Flow<Map<String, ExpEntityState>> =
        client.states.map { states ->
            states.values.mapNotNull { state ->
                state.toExpEntityState()?.let { state.entityId to it }
            }.toMap()
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

    /** Null while a domain has no [ExpEntityState] type yet — those entities are simply omitted. */
    private fun EntityState.toExpEntityState(): ExpEntityState? = when (domain) {
        "light" -> LightState(
            entityId = entityId,
            isOn = isOn,
            isOffline = isUnavailable,
        )
        "fan" -> FanState(
            entityId = entityId,
            isOn = isOn,
            isOffline = isUnavailable,
            percentage = attrDouble("percentage")?.roundToInt() ?: 0,
            isOscillating = attrBoolean("oscillating") == true,
            // Read off the mister's own entity, not the fan's.
//            isMisting = misting?.let { allStates[it.entityId]?.isOn == true } == true,
            isMisting = false,
        )

        else -> null
    }
}
