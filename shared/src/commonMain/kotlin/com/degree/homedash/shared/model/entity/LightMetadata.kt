package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * A light.
 */
data class LightMetadata(
    override val entityId: String,
    override val displayName: String,
) : ToggleableEntityMetadata {

    override fun onToggle(repo: ExpHomeAssistantRepo) = repo.toggleEntity(this)
}
