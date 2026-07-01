package com.degree.homedash.shared.network

/** Live status of the Home Assistant WebSocket connection. */
sealed interface ConnectionStatus {
    data object Disconnected : ConnectionStatus
    data object Connecting : ConnectionStatus
    data object Connected : ConnectionStatus
    data class Error(
        val message: String?,
    ) : ConnectionStatus
}

class HaException(
    message: String,
) : Exception(message)
