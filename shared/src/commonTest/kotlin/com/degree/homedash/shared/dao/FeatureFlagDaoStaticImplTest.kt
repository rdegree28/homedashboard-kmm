package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.model.FeatureFlag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureFlagDaoStaticImplTest {

    private val dao = FeatureFlagDaoStaticImpl()

    @Test
    fun robSeesAllConfiguredFlags() {
        assertEquals(
            setOf(FeatureFlag.ViewOfficeScreen, FeatureFlag.ViewLivingRoomLights),
            dao.getFeatureFlagsForUser(AuthUser("Rob")),
        )
    }

    @Test
    fun mollySeesOnlyHerFlags() {
        assertEquals(
            setOf(FeatureFlag.ViewLivingRoomLights),
            dao.getFeatureFlagsForUser(AuthUser("Molly")),
        )
    }

    @Test
    fun unknownUserSeesNoFlags() {
        assertTrue(dao.getFeatureFlagsForUser(AuthUser("Ghost")).isEmpty())
    }
}
