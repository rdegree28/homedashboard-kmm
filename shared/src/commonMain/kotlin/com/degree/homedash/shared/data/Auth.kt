package com.degree.homedash.shared.data

/** An app user that can log in. */
data class AuthUser(val name: String)

/** The fixed set of app users and their PINs (edit to taste). */
object Users {
    val all: List<AuthUser> = listOf(
        AuthUser("Rob"),
        AuthUser("Molly"),
    )

    private val pins: Map<String, String> = mapOf(
        "Rob" to "9876",
        "Molly" to "1234",
    )

    /** True if [pin] matches [user]'s configured PIN. */
    fun validate(user: AuthUser, pin: String): Boolean = pins[user.name] == pin
}
