package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthDaoSettingsImplTest {

    @Test
    fun loadIsNullWhenNothingStored() {
        assertNull(AuthDaoSettingsImpl(MapSettings()).load().value)
    }

    @Test
    fun loadReturnsStoredName() {
        val dao = AuthDaoSettingsImpl(MapSettings(KEY to "Rob"))
        assertEquals(AuthUser("Rob"), dao.load().value)
    }

    @Test
    fun savePersistsAndEmits() = runTest {
        val settings = MapSettings()
        val dao = AuthDaoSettingsImpl(settings)

        dao.save(AuthUser("Molly"))

        assertEquals(AuthUser("Molly"), dao.load().value) // observed live
        assertEquals(AuthUser("Molly"), AuthDaoSettingsImpl(settings).load().value) // durably persisted
    }

    @Test
    fun clearRemovesAndEmitsNull() = runTest {
        val settings = MapSettings(KEY to "Rob")
        val dao = AuthDaoSettingsImpl(settings)

        dao.clear()

        assertNull(dao.load().value) // observed live
        assertNull(AuthDaoSettingsImpl(settings).load().value) // durably removed
    }

    private companion object {
        const val KEY = "auth_user"
    }
}
