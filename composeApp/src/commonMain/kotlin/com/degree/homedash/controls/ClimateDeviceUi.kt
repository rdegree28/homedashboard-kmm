package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.entity.ClimateMetadata
import com.degree.homedash.shared.model.states.ClimateState
import com.degree.homedash.ui.readingText

@Immutable
data class ClimateDeviceUi(
    private val metadata: ClimateMetadata,
    private val state: ClimateState,
) : DeviceUi {
    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val valueText: String = state.readingText(decimals = 1)
    val subvalueText: String? = null
    val climateKind: ClimateMetadata.ClimateKind = metadata.kind

    override val cardSpan: Int get() = 1
}