package com.degree.homedash.shared.dao

import com.degree.homedash.shared.model.AuthUser
import com.degree.homedash.shared.model.FeatureFlag

/**
 * Resolves which [FeatureFlag]s are enabled for a given user. [FeatureFlagDaoStaticImpl] is the
 * production implementation, backed by hardcoded local rules.
 */
interface FeatureFlagDao {

    /** The set of feature flags enabled for [user]. */
    fun getFeatureFlagsForUser(user: AuthUser): Set<FeatureFlag>
}
