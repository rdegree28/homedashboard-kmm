package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.OfficeSignalState
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The office traffic signal — a status light driven by a mode selector.
 *
 * Reads and writes land on *different* entities: the current mode comes from [entityId], a
 * `sensor.*` that mirrors the light, while setting a mode runs that mode's script. Each script does
 * more than set a colour, which is why this can't just call `turn_on` on the light.
 *
 * @param trafficLight the `light.*` the signal shows on, turned off directly for
 *   [SignalMode.OFF] since there is no script for "no status".
 * @param modeScripts the `script.*` to run per mode. A mode absent from the map falls back to
 *   turning [trafficLight] off.
 */
data class OfficeSignalMetadata(
    override val entityId: String,
    override val displayName: String,
    val trafficLight: String,
    val modeScripts: Map<SignalMode, String>,
) : StatefulDeviceMetadata<OfficeSignalState> {

    override fun loadState(repo: HomeAssistantRepo): Flow<OfficeSignalState> =
        repo.entityForDevice(this).map { entity ->
            OfficeSignalState(
                entityId = entityId,
                isOffline = entity == null || entity.isUnavailable,
                mode = SignalMode.fromStateValue(entity?.state),
            )
        }

    /** Switches the signal to [mode]. Fire-and-forget; the sensor reports the result. */
    fun setMode(mode: SignalMode, repo: HomeAssistantRepo) {
        val script = modeScripts[mode]
        if (script == null) repo.turnOff(trafficLight) else repo.runScript(script)
    }

    /** What the signal can be showing. [stateValue] is what the mirroring sensor reports. */
    enum class SignalMode(val label: String, val stateValue: String) {
        OFF("Off", "off"),
        AVAILABLE("Available", "green"),
        FOCUSED("Focused", "amber"),
        MEETING("Meeting", "red"),
        ;

        companion object {
            /** Null for anything unmodelled, including `unavailable`. */
            fun fromStateValue(value: String?): SignalMode? =
                entries.firstOrNull { it.stateValue == value }
        }
    }
}
