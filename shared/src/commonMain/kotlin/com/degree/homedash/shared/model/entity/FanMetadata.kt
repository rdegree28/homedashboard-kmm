package com.degree.homedash.shared.model.entity

/**
 * A fan.
 *
 * @param speedAdjustment TODO
 */
data class FanMetadata(
    override val entityId: String,
    val speedAdjustment: SpeedAdjustment,
) : EntityMetadata {


    data class SpeedAdjustment(
        val levelCount: Int,
    )
}
