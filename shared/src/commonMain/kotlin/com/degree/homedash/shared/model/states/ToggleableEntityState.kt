package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Entity state interface that holds for toggleable cards.
 */
interface ToggleableEntityState : ExpEntityState {

    /** Whether the entity is currently "on". */
    val isOn: Boolean

    /**
     * Flips the entity through [repo]. The repo is passed in rather than held so states stay plain
     * values; what each domain does to toggle still lives here.
     *
     * Fire-and-forget: HA reports the result by pushing a new state, which replaces this instance,
     * so there is nothing to await and nothing to hand back.
     */
    fun onToggle(repo: ExpHomeAssistantRepo)
}
