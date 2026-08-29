package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.DeviceState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow

/**
 * Metadata for a device that has a live [DeviceState], parameterised by the state type it produces.
 *
 * The metadata builds its own state rather than the transport mapping by Home Assistant domain,
 * because the domain is not a discriminator: `sensor.*` alone backs climate readings, soil moisture
 * and the cat fountain's water level. Only the roster knows which is which.
 *
 * Sealed so [DeviceMetadata]'s hierarchy stays closed — an open sub-interface would force every
 * exhaustive `when` over metadata to grow a branch for it.
 */
sealed interface StatefulDeviceMetadata<out S : DeviceState> : DeviceMetadata {

    /**
     * This device's live state, re-derived whenever the entities behind it change.
     *
     * Each implementation pulls the entities it needs off [repo] — usually just its own, via
     * `entityForDevice`, but a device spread across more than one entity combines several. Reading
     * per entity rather than off the whole snapshot means a device only wakes when something it
     * actually depends on changes.
     *
     * A device whose entity Home Assistant has never reported still emits, offline, rather than
     * vanishing from the screen.
     */
    fun loadState(
        repo: ExpHomeAssistantRepo,
    ): Flow<S>
}
