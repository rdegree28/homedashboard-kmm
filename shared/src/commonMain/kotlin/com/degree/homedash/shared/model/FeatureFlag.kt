package com.degree.homedash.shared.model

/** A togglable app feature, enabled per [AuthUser] via [com.degree.homedash.shared.dao.FeatureFlagDao]. */
enum class FeatureFlag {
    ViewOfficeScreen,
    ViewLivingRoomLights
}