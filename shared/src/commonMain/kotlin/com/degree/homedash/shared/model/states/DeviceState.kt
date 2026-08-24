package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.DeviceMetadata

/**
 * The live state of a device — what it is doing right now, as opposed to [DeviceMetadata]'s static
 * description of it.
 *
 * One implementation per device type ([LightState], [FanState], …), each built from the raw
 * [EntityState]s Home Assistant reports.
 */
interface DeviceState {

    /** Id of the Home Assistant entity this device's state is read from. */
    val entityId: String

    val isOffline: Boolean

    /**
     * Fills in state Home Assistant keeps on a *different* entity than the one this state describes —
     * a misting fan's mister is its own `humidifier.*` entity, so whether it's running can't come from
     * the fan.
     *
     * [metadata] is required rather than incidental: the companion's id is declared there (see
     * `FanMetadata.MistingControl`) and can't be derived from the state. [allStates] is the raw HA
     * snapshot rather than typed states, so a companion of *any* domain can be read without the api
     * needing a mapping branch for it.
     *
     * Most states have no companions, so the default returns this unchanged.
     */
    fun withCompanions(
        metadata: DeviceMetadata,
        allStates: Map<String, EntityState>,
    ): DeviceState = this
}
