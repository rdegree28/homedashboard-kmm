package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Backs [ExpHomeAssistantRepo.preview]; every call is a no-op. */
internal object PreviewExpHomeAssistantApi : ExpHomeAssistantApi {
    override fun loadAllStates(): Flow<Map<String, ExpEntityState>> = emptyFlow()

    override fun toggleEntity(entityId: String) = Unit
}
