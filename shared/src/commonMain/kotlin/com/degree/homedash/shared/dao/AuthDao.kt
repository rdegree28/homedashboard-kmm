package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Persists the logged-in [AuthUser] across launches and exposes it reactively. Internal to the module —
 * UI callers go through [AuthRepo]; [AuthDaoSettingsImpl] is the production implementation, backed by
 * multiplatform settings.
 */
internal interface AuthDao {

    /** The persisted user as a reactive stream; `null` when none is stored (or it no longer exists). */
    fun load(): StateFlow<AuthUser?>

    /** Persist [user] as the logged-in user. */
    suspend fun save(user: AuthUser)

    /** Clear the stored user (log out). */
    suspend fun clear()
}
