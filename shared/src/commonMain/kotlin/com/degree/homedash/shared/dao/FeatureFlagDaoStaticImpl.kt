package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.model.FeatureFlag

/**
 * [FeatureFlagDao] backed by static, hardcoded rules mapping each flag to the user names allowed to
 * see it (by [AuthUser.name]). Barebones — no remote config or persistence.
 */
class FeatureFlagDaoStaticImpl : FeatureFlagDao {

    override fun getFeatureFlagsForUser(user: AuthUser): Set<FeatureFlag> =
        FeatureFlag.entries.filterTo(mutableSetOf()) { user.name in ENABLED_FOR[it].orEmpty() }

    private companion object {
        /** For each flag, the set of user names allowed to see it; a flag absent here is off for everyone. */
        val ENABLED_FOR: Map<FeatureFlag, Set<String>> = mapOf(
            FeatureFlag.ViewOfficeScreen to setOf("Rob"),
            FeatureFlag.ViewLivingRoomLights to setOf("Rob", "Molly"),
        )
    }
}
