package com.degree.homedash.shared.data

/** Connection settings for a Home Assistant instance. */
data class HaConfig(
    val baseUrl: String,
    val token: String,
) {
    /** Build the `ws(s)://host/api/websocket` URL from the base HTTP(S) URL. */
    fun webSocketUrl(): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        val ws = when {
            trimmed.startsWith("https://") -> "wss://" + trimmed.removePrefix("https://")
            trimmed.startsWith("http://") -> "ws://" + trimmed.removePrefix("http://")
            else -> "ws://$trimmed"
        }
        return "$ws/api/websocket"
    }
}
