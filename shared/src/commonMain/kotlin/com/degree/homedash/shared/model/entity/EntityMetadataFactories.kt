package com.degree.homedash.shared.model.entity

/*

 File contains some factories for commonly created entities.

 */

fun FanMetadata.Companion.dellaTowerFan(
    entityId: String,
    displayName: String,
): FanMetadata {
    return FanMetadata(
        entityId = entityId,
        displayName = displayName,
        speedAdjustment = FanMetadata.SpeedAdjustment(12)
    )
}