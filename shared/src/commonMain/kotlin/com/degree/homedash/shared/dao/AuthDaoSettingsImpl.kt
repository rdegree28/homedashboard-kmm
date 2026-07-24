package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.data.createSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [AuthDao] backed by multiplatform [Settings]. Persists the logged-in user by name and mirrors it in
 * a [StateFlow] seeded from storage, so callers observe login/logout without re-reading settings.
 */
internal class AuthDaoSettingsImpl(
    private val settings: Settings = createSettings(),
) : AuthDao {

    private val current = MutableStateFlow(readPersistedUser())

    override fun load(): StateFlow<AuthUser?> = current.asStateFlow()

    override suspend fun save(user: AuthUser) {
        settings.putString(KEY_USER, user.name)
        current.value = user
    }

    override suspend fun clear() {
        settings.remove(KEY_USER)
        current.value = null
    }

    /** The stored user, by name. Only validated names are ever saved (see [AuthRepo]). */
    private fun readPersistedUser(): AuthUser? =
        settings.getStringOrNull(KEY_USER)?.let { name -> AuthUser(name) }

    private companion object {
        const val KEY_USER = "auth_user"
    }
}
