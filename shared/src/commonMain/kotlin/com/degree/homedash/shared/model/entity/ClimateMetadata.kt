package com.degree.homedash.shared.model.entity

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoricalEntityReading
import com.degree.homedash.shared.model.dewPoint
import com.degree.homedash.shared.model.states.ClimateState
import com.degree.homedash.shared.model.toReading
import com.degree.homedash.shared.repo.ExpHomeAssistantRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * A read-only climate sensor (temperature, humidity, dew point). [kind] picks the card's icon and
 * tint; the reading itself is formatted for display upstream.
 *
 * @param dewPointSource set on a [ClimateKind.DewPoint] card, naming the temperature sensor to pair
 *   this entity's humidity with. Home Assistant publishes no dew point of its own, so the card is
 *   computed from the two — see [DewPointSource].
 */
data class ClimateMetadata(
    override val entityId: String,
    override val displayName: String,
    val kind: ClimateKind,
    val dewPointSource: DewPointSource? = null,
) : StatefulDeviceMetadata<ClimateState> {

    override fun loadState(repo: ExpHomeAssistantRepo): Flow<ClimateState> {
        val source = dewPointSource
            ?: return repo.entityForDevice(this).map { entity ->
                ClimateState(
                    entityId = entityId,
                    isOffline = entity == null || entity.isUnavailable,
                    reading = entity.toReading(),
                )
            }

        return combine(
            repo.entityForDevice(this),
            repo.entityFor(source.temperatureEntityId),
        ) { humidity, temperature -> dewPointState(humidity, temperature) }
    }

    /**
     * The dew point of the humidity/temperature pair, in the temperature sensor's own unit, keeping
     * the humidity as the card's subvalue.
     *
     * Offline unless both halves report: a dew point derived from one of them would be a number the
     * room isn't at.
     */
    private fun dewPointState(
        humidity: EntityState?,
        temperature: EntityState?,
    ): ClimateState {
        val rh = humidity.toReading()
        val temp = temperature.toReading()
        val dew = if (temp.value != null && rh.value != null) {
            dewPoint(temp.value, rh.value, fahrenheit = temp.unit.contains("F", ignoreCase = true))
        } else {
            null
        }
        return ClimateState(
            entityId = entityId,
            isOffline = dew == null,
            reading = HistoricalEntityReading(dew, temp.unit),
            subvalue = rh.takeIf { it.value != null },
        )
    }

    /**
     * The temperature half of a dew-point card; the metadata's own [entityId] supplies the relative
     * humidity. Two entities rather than one, the same arrangement as a fan's mister — so the id is
     * carried here rather than guessed from the humidity sensor's name.
     */
    data class DewPointSource(
        val temperatureEntityId: String,
    )

    /** Which climate sensor a [ClimateMetadata] describes — selects the card's icon + tint. */
    enum class ClimateKind {
        Temperature,
        Humidity,
        DewPoint
    }
}
