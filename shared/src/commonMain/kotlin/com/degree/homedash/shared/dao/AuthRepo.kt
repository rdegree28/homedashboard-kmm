package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

/**
 * UI-facing auth entry point. Owns the (barebones) user roster + PINs and wraps the internal [AuthDao]
 * persistence, so callers never touch either directly: it exposes the current session reactively,
 * login/logout, the list of selectable users, and PIN validation. Build the production instance with
 * [create].
 */
class AuthRepo internal constructor(
    private val authDao: AuthDao,
) {

    /** The current logged-in user as a reactive stream; `null` when logged out. */
    fun loadCurrentUser(): StateFlow<AuthUser?> = authDao.load()

    /** Record [username] as logged in and persist the session. */
    suspend fun onUserLoggedIn(username: String) = authDao.save(AuthUser(name = username))

    /** Clear the persisted session (log out). */
    suspend fun onUserLoggedOut() = authDao.clear()

    /** Names of all selectable app users, for the login screen. */
    fun loadAllUserNames(): List<String> = PINS.keys.toList()

    /** True if [pin] matches the configured PIN for [username] (false for an unknown user). */
    fun validate(username: String, pin: String): Boolean = PINS[username] == pin

    companion object {
        /** The fixed set of app users and their PINs. Barebones — edit to taste. */
        private val PINS: Map<String, String> = mapOf(
            "Rob" to "9876",
            "Molly" to "1234",
        )

        /** The production auth repo, backed by settings persistence. */
        fun create(): AuthRepo = AuthRepo(AuthDaoSettingsImpl())
    }
}
