package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * A pressable card that fires a Home Assistant service — activating a scene, running a script,
 * triggering an automation.
 *
 * Unlike the other metadata types this one has no live state to show: [entityId] is synthetic (there
 * is no `trigger.*` domain in Home Assistant), and exists only to give the card a stable list key.
 * The entity the press actually reaches is [targetEntityId].
 *
 * The call is described by the constructor and made by [onActivate] — the same split as a toggleable
 * device, where the behavior hangs off the static descriptor because firing a trigger needs its
 * identity and nothing else. Build the common ones with the factories in
 * TriggerEntityMetadataFactory.kt.
 *
 * @param targetEntityId the `scene.*` / `script.*` / `automation.*` the call is aimed at.
 * @param service what to call on it. `turn_on` activates all three, so it rarely needs stating.
 * @param serviceDomain the domain that service lives in, which is [targetEntityId]'s own unless a
 *   trigger needs to call some other integration's service against it.
 */
data class TriggerDeviceMetadata(
    override val entityId: String,
    override val displayName: String,
    val targetEntityId: String,
    val service: String = "turn_on",
    val serviceDomain: String = targetEntityId.substringBefore('.'),
) : DeviceMetadata {

    /**
     * Fires this card's call through [repo]. Fire-and-forget: a scene or script reports its result by
     * pushing new states for whatever it touched, so there is nothing to await and nothing to hand back.
     */
    fun onActivate(repo: ExpHomeAssistantRepo) = repo.activateTrigger(this)

    companion object
}
