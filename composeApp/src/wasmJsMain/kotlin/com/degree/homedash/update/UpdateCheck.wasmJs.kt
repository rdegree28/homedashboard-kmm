package com.degree.homedash.update

import kotlin.js.Promise
import kotlinx.browser.window
import kotlinx.coroutines.await

/**
 * Fetches the deploy stamp written by `tools/deploy-web.sh`.
 *
 * Cache-busted with a timestamp: `version.txt` has no explicit cache headers, and the whole point is
 * to see the server's current value rather than a cached one. Resolves to an empty string on any
 * failure — including the 200-with-HTML that Caddy's SPA fallback returns when the file is absent,
 * which is why the response is length-checked rather than trusted.
 */
private fun fetchStampJs(url: String): Promise<JsString> = js(
    """
    fetch(url, { cache: 'no-store' })
        .then(function (r) { return r.ok ? r.text() : ''; })
        .catch(function () { return ''; })
    """
)

actual suspend fun fetchDeployedBuildStamp(): String? {
    val raw = runCatching { fetchStampJs("version.txt?t=${window.performance.now()}").await<JsString>() }
        .getOrNull()
        ?.toString()
        ?.trim()
        ?: return null

    // The SPA fallback serves index.html for missing files, so reject anything that isn't a stamp.
    return raw.takeIf { it.isNotEmpty() && it.length < 64 && !it.contains('<') }
}

actual fun reloadApp() {
    window.location.reload()
}
