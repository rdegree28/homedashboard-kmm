package com.degree.homedash.shared.data

import com.russhwolf.settings.Settings

/** Persists the logged-in [AuthUser] across launches (by name). */
class AuthDao(
    private val settings: Settings = createSettings(),
) {

    /** The saved user, resolved against [Users.all] so a removed user won't stay logged in. */
    fun load(): AuthUser? =
        settings.getStringOrNull(KEY_USER)?.let { name -> Users.all.firstOrNull { it.name == name } }

    fun save(user: AuthUser) {
        settings.putString(KEY_USER, user.name)
    }

    fun clear() {
        settings.remove(KEY_USER)
    }

    private companion object {
        const val KEY_USER = "auth_user"
    }
}
