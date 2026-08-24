package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.DeviceMetadata
import com.degree.homedash.shared.model.entity.FanMetadata

data class FanState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
    val percentage: Int,
    val isOscillating: Boolean,
    val isMisting: Boolean,
) : ToggleableDeviceState, DeviceState {

    /** Reads the mister's own `humidifier.*` entity, which the fan's state knows nothing about. */
    override fun withCompanions(
        metadata: DeviceMetadata,
        allStates: Map<String, EntityState>,
    ): DeviceState =
        if (metadata !is FanMetadata) this
        else copy(isMisting = metadata.misting?.let { allStates[it.entityId]?.isOn } == true)
}
