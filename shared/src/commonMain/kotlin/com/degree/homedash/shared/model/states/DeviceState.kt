package com.degree.homedash.shared.model.states

/**
 * The live state of a device — what it is doing right now, as opposed to `DeviceMetadata`'s static
 * description of it.
 *
 * One implementation per device type ([LightState], [FanState], …), each built by its own metadata
 * from the raw `EntityState`s Home Assistant reports.
 */
interface DeviceState {

    /** Id of the Home Assistant entity this device's state is read from. */
    val entityId: String

    val isOffline: Boolean
}
