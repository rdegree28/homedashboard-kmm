package com.degree.homedash.shared.model.states

import com.degree.homedash.shared.model.EntityState

data class FanState(
    override val entityId: String,
    override val isOn: Boolean,
    override val isOffline: Boolean,
    val percentage: Int,
    val isOscillating: Boolean,
    val isMisting: Boolean,
) : ToggleableEntityState, ExpEntityState