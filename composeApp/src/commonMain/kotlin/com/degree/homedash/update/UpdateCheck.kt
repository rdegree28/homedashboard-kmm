package com.degree.homedash.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay

/**
 * The build stamp the server is currently serving (see `tools/deploy-web.sh`), or null on platforms
 * that don't update by redeploy — Android ships as an APK, so it has nothing to poll.
 */
expect suspend fun fetchDeployedBuildStamp(): String?

/** Reloads the app from the server. No-op off the web. */
expect fun reloadApp()

/**
 * True once a newer build has been deployed than the one running.
 *
 * A wall tablet can sit on the same tab for days: cache headers only take effect on a reload, so
 * without this an open tab never picks up a deploy. Compares against the stamp seen at startup
 * rather than the app's version constant, so it catches a redeploy even if the version wasn't bumped.
 *
 * Returns false forever where [fetchDeployedBuildStamp] is null, so the banner never shows on Android.
 */
@Composable
fun rememberUpdateAvailable(pollInterval: Duration = 2.minutes): Boolean {
    var available by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val running = fetchDeployedBuildStamp() ?: return@LaunchedEffect
        while (!available) {
            delay(pollInterval)
            // A failed poll (server down, offline) returns null — keep waiting rather than nagging.
            val deployed = fetchDeployedBuildStamp() ?: continue
            available = deployed != running
        }
    }

    return available
}
