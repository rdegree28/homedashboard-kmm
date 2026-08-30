package com.degree.homedash.shared.model

import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.model.device_metadata.ClimateMetadata
import com.degree.homedash.shared.model.device_metadata.DoorMetadata
import com.degree.homedash.shared.model.device_metadata.FanMetadata
import com.degree.homedash.shared.model.device_metadata.HvacAction
import com.degree.homedash.shared.model.device_metadata.HvacMode
import com.degree.homedash.shared.model.device_metadata.LightMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeSignalMetadata
import com.degree.homedash.shared.model.device_metadata.OfficeWorkstationMetadata
import com.degree.homedash.shared.model.device_metadata.PetFountainMetadata
import com.degree.homedash.shared.model.device_metadata.SoilMoistureMetadata
import com.degree.homedash.shared.model.device_metadata.StatefulDeviceMetadata
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import com.degree.homedash.shared.model.device_state.DeviceState
import com.degree.homedash.shared.repo.FakeExpHomeAssistantApi
import com.degree.homedash.shared.repo.HomeAssistantRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers what each device metadata makes of the raw entities behind it — the one place Home
 * Assistant's strings and attributes become typed state. A wrong attribute name or a missed
 * offline guard reads as a plausible number on screen, which is exactly what a preview can't catch.
 */
class DeviceStateProjectionTest {

    // --- single-entity devices ---

    @Test
    fun aLightReadsItsOnStateAndGoesOfflineWhenUnreported() = runTest {
        val metadata = LightMetadata("light.office", "Office")

        val on = metadata.project(entity("light.office", "on"))
        assertTrue(on.isOn)
        assertFalse(on.isOffline)

        assertFalse(metadata.project(entity("light.office", "off")).isOn)
        // An entity Home Assistant has never reported reads the same as an unavailable one.
        assertTrue(metadata.project().isOffline)
        assertTrue(metadata.project(entity("light.office", "unavailable")).isOffline)
    }

    @Test
    fun aDoorIsOpenWhenItsContactReportsOn() = runTest {
        val metadata = DoorMetadata("binary_sensor.office_door", "Office Door")

        // device_class opening: "on" is open, not "closed".
        assertTrue(metadata.project(entity("binary_sensor.office_door", "on")).isOpen)
        assertFalse(metadata.project(entity("binary_sensor.office_door", "off")).isOpen)
    }

    @Test
    fun aSoilSensorKeepsItsUnitAndDashesWhenUnavailable() = runTest {
        val metadata = SoilMoistureMetadata("sensor.louie_soil_moisture", "Louie")

        val reading = metadata.project(entity("sensor.louie_soil_moisture", "58.4", unit = "%")).reading
        assertEquals(58.4, reading.value)
        assertEquals("%", reading.unit)

        // Nothing usable to show: not 0 %, which would read as bone dry.
        assertEquals(HistoricalEntityReading.Missing, metadata.project(entity("sensor.louie_soil_moisture", "unavailable")).reading)
    }

    // --- composite devices: a second entity carries part of the state ---

    @Test
    fun aFanReadsItsMisterFromTheMistersOwnEntity() = runTest {
        val metadata = FanMetadata(
            entityId = "fan.misting_fan",
            displayName = "Misting Fan",
            speedAdjustment = FanMetadata.SpeedAdjustment(6),
            hasOscillationFeature = true,
            misting = FanMetadata.MistingControl("humidifier.misting_fan"),
        )

        val state = metadata.project(
            entity("fan.misting_fan", "on", attributes = mapOf("percentage" to "66.6", "oscillating" to "true")),
            entity("humidifier.misting_fan", "on"),
        )

        assertTrue(state.isOn)
        assertEquals(67, state.percentage) // rounded, since the card steps in whole levels
        assertTrue(state.isOscillating)
        assertTrue(state.isMisting)
    }

    @Test
    fun aFanWithoutAMisterIsNeverMisting() = runTest {
        val metadata = FanMetadata("fan.box_fan", "Box Fan")

        val state = metadata.project(entity("fan.box_fan", "on"))

        assertFalse(state.isMisting)
        assertEquals(0, state.percentage) // a plain switch publishes no percentage
    }

    @Test
    fun aDewPointCardIsComputedFromItsHumidityAndTemperaturePair() = runTest {
        val metadata = ClimateMetadata(
            entityId = "sensor.humidity",
            displayName = "Dew Point",
            kind = ClimateMetadata.ClimateKind.DewPoint,
            dewPointSource = ClimateMetadata.DewPointSource("sensor.temperature"),
        )

        val state = metadata.project(
            entity("sensor.humidity", "59", unit = "%"),
            entity("sensor.temperature", "70.5", unit = "°F"),
        )

        assertFalse(state.isOffline)
        // ~13.1 °C expressed in the temperature sensor's own scale.
        assertEquals(55.5, state.reading.value?.roundTo(1))
        assertEquals("°F", state.reading.unit)
        // The humidity rides along as the card's subvalue.
        assertEquals(59.0, state.subvalue?.value)
    }

    @Test
    fun aDewPointCardIsOfflineUntilBothSensorsReport() = runTest {
        val metadata = ClimateMetadata(
            entityId = "sensor.humidity",
            displayName = "Dew Point",
            kind = ClimateMetadata.ClimateKind.DewPoint,
            dewPointSource = ClimateMetadata.DewPointSource("sensor.temperature"),
        )

        // A dew point from one half of the pair would be a number the room isn't at.
        val state = metadata.project(entity("sensor.humidity", "59", unit = "%"))

        assertTrue(state.isOffline)
        assertNull(state.reading.value)
    }

    @Test
    fun aFountainRoundsItsFilterToWholeDaysAndReportsNoneWhenItHasNoFilter() = runTest {
        val withFilter = PetFountainMetadata(
            entityId = "sensor.fountain_water",
            displayName = "Remaining Water",
            filterHealth = PetFountainMetadata.FilterHealth("sensor.fountain_filter", maxDays = 31),
        )

        val state = withFilter.project(
            entity("sensor.fountain_water", "41", unit = "%"),
            entity("sensor.fountain_filter", "11.6"),
        )
        assertEquals(41.0, state.waterLevel.value)
        assertEquals(12, state.filterDaysRemaining) // half a day of filter life is noise

        // null, not 0 — a fountain with no filter declared mustn't read as "change it now".
        val noFilter = PetFountainMetadata("sensor.fountain_water", "Remaining Water")
        assertNull(noFilter.project(entity("sensor.fountain_water", "41")).filterDaysRemaining)
    }

    @Test
    fun theWorkstationReadsBothMetersOffTheirOwnSensors() = runTest {
        val metadata = OfficeWorkstationMetadata(
            displayName = "Workstation",
            toggleEntityId = "switch.workstation",
            currentPowerEntityId = "sensor.workstation_power",
            totalPowerEntityId = "sensor.workstation_energy",
        )

        val state = metadata.project(
            entity("switch.workstation", "on"),
            entity("sensor.workstation_power", "62.8", unit = "W"),
            entity("sensor.workstation_energy", "62.29", unit = "kWh"),
        )

        assertTrue(state.isOn)
        assertEquals(62.8, state.currentPower.value)
        assertEquals("W", state.currentPower.unit)
        assertEquals(62.29, state.totalPower.value)
        assertEquals("kWh", state.totalPower.unit)
    }

    @Test
    fun theSignalReadsItsModeFromTheMirroringSensor() = runTest {
        val metadata = OfficeSignalMetadata(
            entityId = "sensor.office_signal_mode",
            displayName = "Signal",
            trafficLight = "light.office_traffic_signal",
            modeScripts = emptyMap(),
        )

        assertEquals(
            OfficeSignalMetadata.SignalMode.FOCUSED,
            metadata.project(entity("sensor.office_signal_mode", "amber")).mode,
        )
        // A colour we don't model highlights nothing rather than guessing.
        assertNull(metadata.project(entity("sensor.office_signal_mode", "purple")).mode)
    }

    // --- the thermostat's offline guard ---

    @Test
    fun aThermostatShowsNoReadingsWhileItIsOffline() = runTest {
        val metadata = ThermostatMetadata(
            entityId = "climate.thermostat",
            displayName = "Thermostat",
            extremeToggle = ThermostatMetadata.ExtremeToggle("input_boolean.extreme"),
        )

        // Some integrations keep stale attributes around for a device that has dropped off the
        // network; a live-looking readout for a unit that isn't there is worse than a dash.
        val state = metadata.project(
            entity(
                "climate.thermostat", "unavailable",
                attributes = mapOf("temperature" to "72", "current_temperature" to "70", "hvac_action" to "cooling"),
            ),
            entity("input_boolean.extreme", "on"),
        )

        assertTrue(state.isOffline)
        assertNull(state.hvacMode)
        assertNull(state.hvacAction)
        assertNull(state.targetTemperature)
        assertNull(state.currentTemperature)
        // The helper is its own entity, so the preset pills still know which setpoints they'd write.
        assertTrue(state.extremeActive)
    }

    @Test
    fun aLiveThermostatReadsItsModeActionAndSetpoints() = runTest {
        val metadata = ThermostatMetadata("climate.thermostat", "Thermostat")

        val state = metadata.project(
            entity(
                "climate.thermostat", "cool",
                attributes = mapOf(
                    "hvac_action" to "idle",
                    "temperature" to "72",
                    "current_temperature" to "70.5",
                    "current_humidity" to "59",
                    "fan_mode" to "auto",
                    "preset_mode" to "eco",
                ),
            ),
        )

        assertEquals(HvacMode.Cool, state.hvacMode)
        assertEquals(HvacAction.Idle, state.hvacAction)
        assertEquals(72.0, state.targetTemperature)
        assertEquals(70.5, state.currentTemperature)
        assertEquals(59.0, state.currentHumidity)
        assertEquals("auto", state.fanMode)
        assertEquals("eco", state.presetMode)
        assertFalse(state.extremeActive) // no helper declared
    }
}

// --- helpers ---

/** The state this metadata makes of [entities], which stand in for what Home Assistant has reported. */
private suspend fun <S : DeviceState> StatefulDeviceMetadata<S>.project(vararg entities: EntityState): S {
    val api = FakeExpHomeAssistantApi()
    api.states.value = entities.associateBy { it.entityId }
    api.connection.value = HaConnectionStatus.Connected
    return loadState(HomeAssistantRepo(api)).first()
}

/** An entity as Home Assistant reports it: a state string plus whatever attributes came with it. */
private fun entity(
    entityId: String,
    state: String,
    unit: String? = null,
    attributes: Map<String, String> = emptyMap(),
): EntityState {
    val attrs = attributes + (unit?.let { mapOf("unit_of_measurement" to it) } ?: emptyMap())
    return EntityState(
        entityId = entityId,
        state = state,
        attributes = JsonObject(attrs.mapValues { (_, v) -> JsonPrimitive(v) }),
    )
}

private fun Double.roundTo(decimals: Int): Double {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    return kotlin.math.round(this * factor) / factor
}
