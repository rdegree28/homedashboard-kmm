package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Metadata for a device that can be flipped on and off.
 *
 * Toggling needs only the device's identity, never its live value, so the action hangs off the static
 * descriptor rather than the state — the same place [TriggerDeviceMetadata] keeps its behavior, and
 * the place composite action targets already live (see [FanMetadata.MistingControl]).
 *
 * Sealed so [DeviceMetadata]'s hierarchy stays closed — an open sub-interface would force every
 * exhaustive `when` over metadata to grow a branch for it.
 */
sealed interface ToggleableDeviceMetadata {

    val entityId: String

    /**
     * Flips the device through [repo]. Fire-and-forget: HA reports the result by pushing a new state,
     * so there is nothing to await and nothing to hand back.
     */
    fun onToggle(repo: ExpHomeAssistantRepo) = repo.toggleEntity(this)
}
