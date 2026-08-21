package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow

internal class WebSocketHomeAssistantStateApi(
    private val client: HaClient
) : HomeAssistantStateApi {

    override fun loadAllStates(): Flow<Map<String, ExpEntityState>> {
        return client.expStates
    }
}