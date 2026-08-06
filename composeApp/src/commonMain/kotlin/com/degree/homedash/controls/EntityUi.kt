package com.degree.homedash.controls

import com.degree.homedash.shared.model.entity.*
import androidx.compose.runtime.Immutable

/**
 * Live render state for an entity, one variant per control type. Each variant nests its *typed*
 * [EntityMetadata] (so a light's state can't be paired with fan metadata) plus the values that change
 * with each state push — the label lives on the metadata, not here. Built by [toEntityUi] and
 * rendered by [EntityControl]; screens hand lists of these to `ControlGroup`.
 */
@Immutable
sealed interface EntityUi {
    val metadata: EntityMetadata

    @Immutable
    data class Light(
        override val metadata: LightMetadata,
        val isOn: Boolean,
        val offline: Boolean,
    ) : EntityUi

    @Immutable
    data class Fan(
        override val metadata: FanMetadata,
        val isOn: Boolean,
        val offline: Boolean,
        val percentage: Int,
        val oscillating: Boolean = false,
        val misting: Boolean = false,
    ) : EntityUi

    @Immutable
    data class Climate(
        override val metadata: ClimateMetadata,
        val valueText: String,
        val subvalueText: String? = null,
    ) : EntityUi

    /**
     * A thermostat. Unlike [Climate] and the sensor variants, the numbers stay numbers rather than
     * pre-formatted text: the setpoint stepper does arithmetic on them, so a display string would
     * only have to be parsed back.
     */
    @Immutable
    data class Thermostat(
        override val metadata: ThermostatMetadata,
        val offline: Boolean,
        /** The entity's own state. null when offline, or reporting a mode we don't model. */
        val hvacMode: HvacMode?,
        /** `hvac_action` — the live Idle/Heating/Cooling badge. null when the device omits it. */
        val hvacAction: HvacAction?,
        /** `temperature`. Absent in heat_cool, which publishes a low/high pair instead. */
        val targetTemperature: Double?,
        /** `current_temperature` — the ambient reading shown under the setpoint. */
        val currentTemperature: Double?,
        /** Raw Home Assistant strings, matched against the lists the metadata declares. */
        val fanMode: String?,
        val presetMode: String?,
    ) : EntityUi

    @Immutable
    data class Door(
        override val metadata: DoorMetadata,
        val statusText: String,
        val open: Boolean,
        val unavailable: Boolean,
    ) : EntityUi

    @Immutable
    data class SoilMoisture(
        override val metadata: SoilMoistureMetadata,
        val pct: Double?,
        val valueText: String,
    ) : EntityUi

    @Immutable
    data class WaterLevel(
        override val metadata: WaterLevelMetadata,
        val pct: Double?,
        val valueText: String,
    ) : EntityUi

    /** A launcher card. Alone among these it has nothing that changes — the metadata is the whole state. */
    @Immutable
    data class Navigation(
        override val metadata: NavigationMetadata,
    ) : EntityUi

    /** A scene/script trigger card. Like [Navigation], nothing about it changes at runtime. */
    @Immutable
    data class Trigger(
        override val metadata: TriggerEntityMetadata,
    ) : EntityUi
}

/** Convenience for list keys and actions — the underlying entity id. */
val EntityUi.entityId: String get() = metadata.entityId

/** Convenience for rendering — the label this entity shows. */
val EntityUi.displayName: String get() = metadata.displayName
