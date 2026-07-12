package com.degree.homedash.shared.data

/** Resolves which [FeatureFlag]s are enabled for a given user (local hardcoded rules for now). */
class FeatureFlagDao {

    private val enabledFor: Map<FeatureFlag, Set<String>> = mapOf(
        FeatureFlag.ViewOfficeScreen to setOf("Rob"), // by AuthUser.name
        FeatureFlag.ViewLivingRoomLights to setOf("Rob", "Molly"),
    )

    fun isEnabled(flag: FeatureFlag, user: AuthUser?): Boolean =
        user != null && enabledFor[flag]?.contains(user.name) == true

    /** The set of flags enabled for [user] (none when logged out). */
    fun flagsFor(user: AuthUser?): Set<FeatureFlag> =
        FeatureFlag.entries.filterTo(mutableSetOf()) { isEnabled(it, user) }
}
