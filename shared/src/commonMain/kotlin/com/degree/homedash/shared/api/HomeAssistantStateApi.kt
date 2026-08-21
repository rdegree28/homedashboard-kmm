package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow

interface HomeAssistantStateApi {

    fun loadAllStates(): Flow<Map<String, ExpEntityState>>
}