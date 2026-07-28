package com.degree.homedash.shared.model.entity

/**
 * A read-only climate sensor (temperature, humidity, dew point). [kind] picks the row's presentation;
 * the reading itself is formatted upstream and lives on the UI state, not here.
 */
data class ClimateMetadata(
    override val entityId: String,
    override val displayName: String,
    val kind: ClimateKind,
) : EntityMetadata {

    /** Which climate sensor a [ClimateMetadata] describes — selects the row's icon + tint. */
    enum class ClimateKind {
        Temperature,
        Humidity,
        DewPoint
    }
}
