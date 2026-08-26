package com.degree.homedash.shared.model.states

class OfficeWorkstationState(
    override val entityId: String,
    override val isOffline: Boolean,
    override val isOn: Boolean,

) : ToggleableDeviceState, DeviceState