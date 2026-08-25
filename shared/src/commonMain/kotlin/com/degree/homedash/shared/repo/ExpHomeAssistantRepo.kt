package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.api.PreviewExpHomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.entity.DeviceMetadata
import com.degree.homedash.shared.model.entity.FanMetadata
import com.degree.homedash.shared.model.entity.ToggleableDeviceMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Public face of the experimental stack — the exp counterpart to [HomeAssistantRepo], wired as a Koin
 * `single` in `SharedModule`. [ExpHomeAssistantApi] is internal, so this forwards the actions callers
 * need rather than handing out an api.
 */
class ExpHomeAssistantRepo internal constructor(
    private val api: ExpHomeAssistantApi,
) {

    /**
     * Live map of Home Assistant entity id → its latest state, for a roster to project against with
     * `DeviceMetadata.toUi`.
     *
     * Raw rather than typed on purpose: an entity's domain doesn't determine which device it is
     * (`sensor.*` backs climate, soil moisture and water level alike), so only the roster can decide.
     * Re-emits on every push for *any* entity, so callers projecting into UI state should
     * `distinctUntilChanged` downstream, as the ViewModels do.
     */
    fun loadEntityStates(): Flow<Map<String, EntityState>> = api.loadAllStates()


    /**
     * One entity's live state, re-emitting only when *that* entity changes rather than on every push.
     * Null while Home Assistant has never reported [entityId] — a disabled or mistyped id.
     */
    internal fun entityFor(entityId: String): Flow<EntityState?> =
        loadEntityStates().map { it[entityId] }.distinctUntilChanged()

    /** [entityFor] the entity backing [metadata]. */
    internal fun entityForDevice(metadata: DeviceMetadata): Flow<EntityState?> =
        entityFor(metadata.entityId)

    /**
     * Flips [entity] on or off.
     *
     * Takes metadata rather than an id because toggling needs the entity's identity, not its live
     * value — and the [ToggleableDeviceMetadata] parameter is what keeps non-toggleable entities out.
     * Reached through [ToggleableDeviceMetadata.onToggle] rather than called directly, so the only
     * caller today is `LightMetadata`.
     *
     * Fire-and-forget: HA answers a service call by pushing a new state, so there is nothing to await.
     */
    internal fun toggleEntity(entity: ToggleableDeviceMetadata) {
        api.toggleEntity(entityId = entity.entityId)
    }

    /** Set a fan's speed (0–100%). The value arrives back via the entity's `percentage` attribute. */
    internal fun setFanPercentage(
        metadata: FanMetadata,
        percentage: Int,
    ) = api.callService("fan", "set_percentage", metadata.entityId, buildJsonObject { put("percentage", percentage) })

    /**
     * Turn a fan's mister on or off.
     *
     * Targets the mister's own `humidifier.*` entity, not the fan's — Home Assistant models the two
     * separately (see [FanMetadata.MistingControl]). No-ops for a fan that has no mister.
     */
    internal fun setMisting(
        metadata: FanMetadata,
        misting: Boolean,
    ) {
        val misterId = metadata.misting?.entityId ?: return
        api.callService(misterId.substringBefore('.'), if (misting) "turn_on" else "turn_off", misterId)
    }

    /** Turn a fan's oscillation on or off. The value arrives back via the `oscillating` attribute. */
    internal fun setFanOscillating(
        metadata: FanMetadata,
        oscillating: Boolean,
    ) = api.callService("fan", "oscillate", metadata.entityId, buildJsonObject { put("oscillating", oscillating) })

    companion object {

        /**
         * An inert repo for `@Preview` use: no states ever arrive and actions do nothing.
         *
         * Previews render outside the app's Koin graph, so anything reaching for a repo — a
         * [ToggleableDeviceMetadata] card wanting somewhere to send its toggle — has nothing to
         * resolve. This exists because the real constructor is internal to `:shared`, so `:composeApp`
         * can't build a stand-in itself.
         */
        fun preview(): ExpHomeAssistantRepo = ExpHomeAssistantRepo(PreviewExpHomeAssistantApi)
    }
}