package com.degree.homedash.shared.api

/** Live status of the Home Assistant WebSocket connection. */
sealed interface HaConnectionStatus {
    data object Disconnected : HaConnectionStatus
    data object Connecting : HaConnectionStatus
    data object Connected : HaConnectionStatus
    data class Error(
        val message: String?,
    ) : HaConnectionStatus
}

class HaException(
    message: String,
) : Exception(message)
