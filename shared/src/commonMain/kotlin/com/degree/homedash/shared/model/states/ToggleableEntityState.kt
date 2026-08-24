package com.degree.homedash.shared.model.states

/**
 * Entity state interface that holds for toggleable cards. Render data only — the toggle itself hangs
 * off [com.degree.homedash.shared.model.entity.ToggleableEntityMetadata], since flipping an entity
 * needs its identity rather than its current value.
 */
interface ToggleableEntityState {

    /** Whether the entity is currently "on". */
    val isOn: Boolean
}
