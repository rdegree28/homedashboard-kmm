package com.degree.homedash.shared.model.entity

/**
 * A water-level sensor (the pet fountain), rendered as a percentage gauge. Read-only, so identity is
 * all it carries — the level comes from the live state.
 */
data class WaterLevelMetadata(
    override val entityId: String,
    override val displayName: String,
    /** Non-null when a second entity reports filter life; the screen shows its bar exactly then. */
    val filterHealth: FilterHealth? = null,
) : EntityMetadata {

    /**
     * The filter's own sensor, plus the cycle length its days count down from.
     *
     * A composite entity, the way [FanMetadata.MistingControl] and [ThermostatMetadata.ExtremeToggle]
     * are: the fountain publishes its filter life as a separate `sensor.*`, not as an attribute of
     * the water level.
     *
     * [maxDays] is hand-declared rather than read from the fountain's `number.*` filter-cycle entity,
     * for the reason [ThermostatMetadata.TargetTemperature] gives: a bar whose full scale can vanish
     * when a device drops offline collapses to nothing.
     */
    data class FilterHealth(val entityId: String, val maxDays: Int)
}
