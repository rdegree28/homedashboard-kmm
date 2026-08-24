package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.entity.FanMetadata

data class FanState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
    val percentage: Int,
    val isOscillating: Boolean,
    val isMisting: Boolean,
) : ToggleableEntityState, ExpEntityState {

    /** Reads the mister's own `humidifier.*` entity, which the fan's state knows nothing about. */
    override fun withCompanions(
        metadata: EntityMetadata,
        allStates: Map<String, EntityState>,
    ): ExpEntityState =
        if (metadata !is FanMetadata) this
        else copy(isMisting = metadata.misting?.let { allStates[it.entityId]?.isOn } == true)
}
