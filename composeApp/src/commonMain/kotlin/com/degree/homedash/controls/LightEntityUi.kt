package com.degree.homedash.controls

import androidx.compose.runtime.Immutable
import com.degree.homedash.shared.model.states.LightState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo

/**
 * Render state for a light card — a pure value: the [LightState] plus what it takes to label it.
 *
 * The repo needed to act on the light is handed to [onToggle] at the call site rather than held, so
 * nothing about the card's identity depends on a DI object.
 */
@Immutable
data class LightEntityUi(
    override val id: String,
    val name: String,
    private val state: LightState,
) : ExpEntityUi {

    val isOn: Boolean get() = state.isOn
    val offline: Boolean get() = state.isOffline

    fun onToggle(repo: ExpHomeAssistantRepo) = state.onToggle(repo)

    override val cardSpan: Int
        get() = 1
}
