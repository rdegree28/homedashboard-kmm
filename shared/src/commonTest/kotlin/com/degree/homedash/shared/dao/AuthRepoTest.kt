package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepoTest {

    @Test
    fun loadCurrentUserReflectsDao() {
        val dao = FakeAuthDao()
        val repo = AuthRepo(dao)

        assertNull(repo.loadCurrentUser().value)
        dao.state.value = AuthUser("Rob")
        assertEquals(AuthUser("Rob"), repo.loadCurrentUser().value)
    }

    @Test
    fun onUserLoggedInSavesUser() = runTest {
        val dao = FakeAuthDao()
        val repo = AuthRepo(dao)

        repo.onUserLoggedIn("Molly")

        assertEquals(AuthUser("Molly"), dao.saved)
        assertEquals(AuthUser("Molly"), repo.loadCurrentUser().value)
    }

    @Test
    fun onUserLoggedOutClearsUser() = runTest {
        val dao = FakeAuthDao()
        val repo = AuthRepo(dao)
        repo.onUserLoggedIn("Rob")

        repo.onUserLoggedOut()

        assertTrue(dao.cleared)
        assertNull(repo.loadCurrentUser().value)
    }

    @Test
    fun loadAllUserNamesReturnsRoster() {
        val repo = AuthRepo(FakeAuthDao())

        assertEquals(listOf("Rob", "Molly"), repo.loadAllUserNames())
    }

    @Test
    fun validateChecksPinForKnownUser() {
        val repo = AuthRepo(FakeAuthDao())

        assertTrue(repo.validate("Rob", "9876"))
        assertTrue(repo.validate("Molly", "1234"))
        assertFalse(repo.validate("Rob", "0000")) // wrong pin
        assertFalse(repo.validate("Nobody", "9876")) // unknown user
    }
}

/** In-memory [AuthDao] recording save/clear and exposing a settable current-user flow. */
private class FakeAuthDao : AuthDao {
    val state = MutableStateFlow<AuthUser?>(null)
    var saved: AuthUser? = null
    var cleared = false

    override fun load(): StateFlow<AuthUser?> = state

    override suspend fun save(user: AuthUser) {
        saved = user
        state.value = user
    }

    override suspend fun clear() {
        cleared = true
        state.value = null
    }
}
