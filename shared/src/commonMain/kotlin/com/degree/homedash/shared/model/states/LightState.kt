package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

// State instantiation for a light.
data class LightState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
) : ToggleableEntityState {

    override fun onToggle(repo: ExpHomeAssistantRepo) = repo.api.toggleEntity(entityId)
}
