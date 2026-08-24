package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.ExpHomeAssistantApi
import com.degree.homedash.shared.api.PreviewExpHomeAssistantApi
import com.degree.homedash.shared.model.entity.EntityMetadata
import com.degree.homedash.shared.model.entity.ToggleableEntityMetadata
import com.degree.homedash.shared.model.states.ExpEntityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Public face of the experimental stack — the exp counterpart to [HomeAssistantRepo], wired as a Koin
 * `single` in `SharedModule`. [ExpHomeAssistantApi] is internal, so this forwards the actions callers
 * need rather than handing out an api.
 */
class ExpHomeAssistantRepo internal constructor(
    private val api: ExpHomeAssistantApi,
) {

    /**
     * Pairs each entry of [metadataList] with its live state.
     *
     * Keyed by *metadata* rather than entity id so callers get their own roster back and never have to
     * look ids up again, and ordered to match [metadataList] so card layout stays stable.
     *
     * Two things to know:
     * - Metadata with no matching state is **dropped**. An entity Home Assistant has never reported
     *   (a typo'd or disabled id) is absent from the map rather than present-and-offline, so it
     *   disappears from the screen instead of rendering as unavailable.
     * - This re-emits on every state push for *any* entity, not just the ones in [metadataList] — the
     *   upstream is the whole-map flow. Callers that project into UI state should
     *   `distinctUntilChanged` downstream, as `LivingRoomViewModel` does.
     */
    fun loadEntityStatesForMetadata(metadataList: List<EntityMetadata>): Flow<Map<EntityMetadata, ExpEntityState>> {
        return api.loadAllStates().map { stateMap ->
            metadataList.mapNotNull { meta ->
                stateMap[meta.entityId]?.let { state -> meta to state }
            }.toMap()
        }
    }

    /**
     * Flips [entity] on or off.
     *
     * Takes metadata rather than an id because toggling needs the entity's identity, not its live
     * value — and the [ToggleableEntityMetadata] parameter is what keeps non-toggleable entities out.
     * Reached through [ToggleableEntityMetadata.onToggle] rather than called directly, so the only
     * caller today is `LightMetadata`.
     *
     * Fire-and-forget: HA answers a service call by pushing a new state, so there is nothing to await.
     */
    fun toggleEntity(entity: ToggleableEntityMetadata) {
        api.toggleEntity(entityId = entity.entityId)
    }

    companion object {

        /**
         * An inert repo for `@Preview` use: no states ever arrive and actions do nothing.
         *
         * Previews render outside the app's Koin graph, so anything reaching for a repo — a
         * [ToggleableEntityMetadata] card wanting somewhere to send its toggle — has nothing to
         * resolve. This exists because the real constructor is internal to `:shared`, so `:composeApp`
         * can't build a stand-in itself.
         */
        fun preview(): ExpHomeAssistantRepo = ExpHomeAssistantRepo(PreviewExpHomeAssistantApi)
    }
}