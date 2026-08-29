package com.degree.homedash.core.device

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.device_metadata.HvacAction
import com.degree.homedash.shared.model.device_metadata.HvacMode
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.device_state.ThermostatState
import com.degree.homedash.shared.repo.HomeAssistantRepo

/**
 * Render state for a thermostat card: the static descriptor plus the live readings.
 *
 * [metadata] is public here, unlike the other device UIs which keep theirs private: the card draws a
 * row per capability the thermostat declares — its setpoint bounds, mode lists and temperature
 * presets — so the descriptor is half of what it renders, not just an identity to act through.
 */
@Immutable
data class ThermostatDeviceUi(
    val metadata: ThermostatMetadata,
    private val state: ThermostatState,
) : DeviceUi {

    override val id: String get() = metadata.entityId

    /** Always full width: the stepper and mode pills need the room. */
    override val cardSpan: Int get() = 2

    val name: String get() = metadata.displayName
    val offline: Boolean get() = state.isOffline

    val hvacMode: HvacMode? get() = state.hvacMode
    val hvacAction: HvacAction? get() = state.hvacAction
    val targetTemperature: Double? get() = state.targetTemperature
    val currentTemperature: Double? get() = state.currentTemperature
    val currentHumidity: Double? get() = state.currentHumidity
    val fanMode: String? get() = state.fanMode
    val presetMode: String? get() = state.presetMode
    val extremeActive: Boolean get() = state.extremeActive

    fun setTargetTemperature(temperature: Double, repo: HomeAssistantRepo) =
        metadata.setTargetTemperature(temperature, repo)

    fun setHvacMode(mode: HvacMode, repo: HomeAssistantRepo) = metadata.setHvacMode(mode, repo)

    fun setFanMode(mode: String, repo: HomeAssistantRepo) = metadata.setFanMode(mode, repo)

    fun setPresetMode(mode: String, repo: HomeAssistantRepo) = metadata.setPresetMode(mode, repo)

    fun setExtremeTemperatures(extreme: Boolean, repo: HomeAssistantRepo) =
        metadata.setExtremeTemperatures(extreme, repo)
}
