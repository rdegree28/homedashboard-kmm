package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Metadata for an entity that can be flipped on and off.
 *
 * Toggling needs only the entity's identity, never its live value, so the action hangs off the static
 * descriptor rather than the state — the same place [TriggerEntityMetadata] keeps its behavior, and
 * the place composite action targets already live (see [FanMetadata.MistingControl]).
 *
 * Sealed so [EntityMetadata]'s hierarchy stays closed — an open sub-interface would force every
 * exhaustive `when` over metadata to grow a branch for it.
 */
sealed interface ToggleableEntityMetadata : EntityMetadata {

    /**
     * Flips the entity through [repo]. Fire-and-forget: HA reports the result by pushing a new state,
     * so there is nothing to await and nothing to hand back.
     */
    fun onToggle(repo: ExpHomeAssistantRepo)
}
