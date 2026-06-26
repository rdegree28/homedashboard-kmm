package com.degree.homedash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.window
import org.w3c.dom.events.Event

private class BrowserBackState {
    /** Sentinel history entries we've pushed — one per in-app back level (depth - 1). */
    var sentinels = 0

    /** popstate events we triggered ourselves (mirroring an in-app back) and must ignore. */
    var selfPops = 0
}

/**
 * Mirrors the app's back stack onto the browser history so the Back button navigates in-app
 * instead of leaving the page. For each back level we push a sentinel history entry; the browser
 * Back button then pops a sentinel (firing `popstate`), which we translate into [onBack]. In-app
 * back presses are mirrored the other way via `history.back()` so the two stay balanced.
 */
@Composable
actual fun PlatformBackHandler(backStackSize: Int, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)
    val state = remember { BrowserBackState() }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            when {
                state.selfPops > 0 -> state.selfPops-- // our own history.back(); already handled
                state.sentinels > 0 -> {
                    state.sentinels-- // browser Back consumed a sentinel
                    currentOnBack()
                }
            }
        }
        window.addEventListener("popstate", listener)
        onDispose { window.removeEventListener("popstate", listener) }
    }

    // Keep sentinel count == depth - 1: push on forward nav, mirror in-app back into history.
    LaunchedEffect(backStackSize) {
        val target = backStackSize - 1
        while (state.sentinels < target) {
            window.history.pushState(null, "", null)
            state.sentinels++
        }
        while (state.sentinels > target) {
            state.sentinels--
            state.selfPops++
            window.history.back()
        }
    }
}
