package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.states.ClimateState
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A read-only climate sensor (temperature, humidity, dew point). [kind] picks the row's presentation;
 * the reading itself is formatted upstream and lives on the UI state, not here.
 */
data class ClimateMetadata(
    override val entityId: String,
    override val displayName: String,
    val kind: ClimateKind,
) : StatefulDeviceMetadata<ClimateState> {

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<ClimateState> =
        repo.entityForDevice(this).map { entity ->
            ClimateState(
                entityId = entityId,
                isOffline = entity == null || entity.isUnavailable,
                value = entity?.state?.toDoubleOrNull(),
                unit = entity?.attrString("unit_of_measurement").orEmpty(),
            )
        }

    /** Which climate sensor a [ClimateMetadata] describes — selects the row's icon + tint. */
    enum class ClimateKind {
        Temperature,
        Humidity,
        DewPoint
    }
}
