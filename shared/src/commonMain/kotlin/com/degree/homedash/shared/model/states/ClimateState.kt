package com.degree.homedash.shared.model.states

class ClimateState(
    override val entityId: String,
    override val isOffline: Boolean,
    val value: Double?,
    val unit: String?,
) : DeviceState