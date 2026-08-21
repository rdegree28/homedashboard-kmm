package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import com.degree.homedash.shared.model.states.ToggleableEntityState

internal class WebSocketHomeAssistantActionApi(
    private val client: HaClient,
) : HomeAssistantActionApi{

    override suspend fun toggleEntity(entityState: ToggleableEntityState) {
        client.callService(
            domain = entityState.entityDomain(),
            service = "toggle",
            entityId = entityState.entityId
        )
    }

    private fun ExpEntityState.entityDomain() = entityId.substringBefore('.')
}