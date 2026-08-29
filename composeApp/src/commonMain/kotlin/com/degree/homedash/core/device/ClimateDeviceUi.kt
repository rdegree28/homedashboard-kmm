package com.degree.homedash.core.device

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.device_metadata.ClimateMetadata
import com.degree.homedash.shared.model.device_state.ClimateState
import com.degree.homedash.ui.readingText

@Immutable
data class ClimateDeviceUi(
    private val metadata: ClimateMetadata,
    private val state: ClimateState,
) : DeviceUi {
    override val id: String get() = metadata.entityId
    val name: String get() = metadata.displayName

    val valueText: String = state.readingText(decimals = 1)

    /** The humidity under a dew point; null on the plain sensors, which have nothing to qualify. */
    val subvalueText: String? = state.subvalue?.readingText(decimals = 0)
    val climateKind: ClimateMetadata.ClimateKind = metadata.kind

    override val cardSpan: Int get() = 1
}
