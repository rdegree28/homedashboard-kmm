package com.degree.homedash.shared.model.device_metadata

import com.degree.homedash.shared.model.device_state.LightState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A light.
 *
 * [icon] and [tint] are how a light that doesn't look like a bulb stays a light: the hexagon panels
 * get their own glyph and a white glow, everything else takes the defaults and reads as a bulb.
 */
data class LightMetadata(
    override val entityId: String,
    override val displayName: String,
    /** Which glyph the card draws. A token, not a picture — see [LightIcon]. */
    val icon: LightIcon = LightIcon.Bulb,
    /** Colour of the glyph and its glow while the light is on, ARGB. Defaults to bulb amber. */
    val tint: Long = 0xFFFFC107,
) : ToggleableDeviceMetadata, StatefulDeviceMetadata<LightState> {

    override fun loadState(
        repo: ExpHomeAssistantRepo,
    ): Flow<LightState> {
        return repo.entityForDevice(this).map { entity ->
            LightState(
                entityId = entityId,
                isOn = entity?.isOn == true,
                isOffline = entity == null || entity.isUnavailable,
            )
        }
    }

    /**
     * Which glyph a light draws, named for the drawing rather than the fixture. A token, not a
     * picture: `:shared` has no Compose dependency, so the UI resolves these (see `LightIcon`).
     */
    enum class LightIcon {
        Bulb,
        Hexagon,
    }
}
