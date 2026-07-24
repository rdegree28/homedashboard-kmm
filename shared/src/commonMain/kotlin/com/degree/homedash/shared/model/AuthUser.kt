package com.degree.homedash.shared.model

/**
 * An app user that can log in. The roster + PINs live in `AuthRepo`.
 */
data class AuthUser(
    val name: String
)