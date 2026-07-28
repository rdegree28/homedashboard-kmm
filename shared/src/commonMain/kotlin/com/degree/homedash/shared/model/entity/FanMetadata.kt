package com.degree.homedash.shared.model.entity

/**
 * A fan.
 *
 * @param speedAdjustment the fan's discrete speeds, or null when it is on/off only — controls show a
 *   speed slider exactly when this is non-null.
 */
data class FanMetadata(
    override val entityId: String,
    override val displayName: String,
    val speedAdjustment: SpeedAdjustment? = null,
) : EntityMetadata {

    /**
     * How many discrete speeds a fan has. Derived from Home Assistant's `percentage_step`
     * (12 steps for a typical ceiling fan); controls step the slider in whole levels so every
     * speed is reachable.
     */
    data class SpeedAdjustment(
        val levelCount: Int,
    ) {
        companion object {
            /**
             * [levelCount] steps, or null when the fan can't usefully be sped up or down — fewer than
             * two levels means the only reachable speeds are off and full.
             */
            fun forLevelCount(levelCount: Int): SpeedAdjustment? =
                if (levelCount >= 2) SpeedAdjustment(levelCount) else null
        }
    }
}
