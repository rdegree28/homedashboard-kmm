package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HaProtocolHelper.StatisticsPeriod
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.device_metadata.HvacMode
import com.degree.homedash.shared.model.device_metadata.ThermostatMetadata
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the exact wire shape of the `climate.*` service calls. Previews can show the card's every
 * state but can't catch a misspelled service name or a mode sent as `"Cool"` instead of `"cool"`.
 */
class HomeAssistantRepoTest {

    private val thermostat = ThermostatMetadata(
        entityId = "climate.living_room_thermostat",
        displayName = "Thermostat",
    )

    @Test
    fun setTargetTemperatureCallsClimateSetTemperature() = runTest {
        val api = FakeExpHomeAssistantApi()

        HomeAssistantRepo(api).setTargetTemperature(thermostat, 72.0)

        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall(
                "climate", "set_temperature", thermostat.entityId,
                buildJsonObject { put("temperature", 72.0) },
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun setHvacModeSendsHomeAssistantsSnakeCaseValue() = runTest {
        val api = FakeExpHomeAssistantApi()

        HomeAssistantRepo(api).setHvacMode(thermostat, HvacMode.HeatCool)

        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall(
                "climate", "set_hvac_mode", thermostat.entityId,
                // "heat_cool", not the enum's "HeatCool" — the whole reason haValue exists.
                buildJsonObject { put("hvac_mode", "heat_cool") },
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun everyHvacModeHasADistinctHomeAssistantValue() {
        val values = HvacMode.entries.map { it.haValue }

        assertEquals(values.size, values.toSet().size)
        // Round-trips, so parsing a state HA sent back always lands on the mode we sent.
        HvacMode.entries.forEach { mode -> assertEquals(mode, HvacMode.fromHa(mode.haValue)) }
    }

    @Test
    fun setThermostatFanModeCallsClimateNotFan() = runTest {
        val api = FakeExpHomeAssistantApi()

        HomeAssistantRepo(api).setThermostatFanMode(thermostat, "low")

        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall(
                "climate", "set_fan_mode", thermostat.entityId,
                buildJsonObject { put("fan_mode", "low") },
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun setPresetModeCallsClimateSetPresetMode() = runTest {
        val api = FakeExpHomeAssistantApi()

        HomeAssistantRepo(api).setPresetMode(thermostat, "eco")

        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall(
                "climate", "set_preset_mode", thermostat.entityId,
                buildJsonObject { put("preset_mode", "eco") },
            ),
            api.serviceCalls.single(),
        )
    }

    // --- composite-entity actions ---
    // Extreme setpoints live in their own input_boolean, so the call must land on the helper.

    @Test
    fun setExtremeTemperaturesTargetsTheHelperNotTheThermostat() = runTest {
        val api = FakeExpHomeAssistantApi()
        val withHelper = thermostat.copy(
            extremeToggle = ThermostatMetadata.ExtremeToggle("input_boolean.extreme_temperatures"),
        )

        HomeAssistantRepo(api).setExtremeTemperatures(withHelper, extreme = true)

        assertEquals(
            FakeExpHomeAssistantApi.ServiceCall(
                "input_boolean", "turn_on", "input_boolean.extreme_temperatures", null,
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun setExtremeTemperaturesIsANoOpWithoutAHelper() = runTest {
        val api = FakeExpHomeAssistantApi()

        HomeAssistantRepo(api).setExtremeTemperatures(thermostat, extreme = true)

        assertEquals(0, api.serviceCalls.size)
    }

    // --- history source selection ---
    // The recorder purges raw states after ~10 days, so which source a window reads from is the whole
    // difference between a 1y chart showing a year and showing the last week and a half.

    @Test
    fun shortWindowsReadRawRecorderStates() = runTest {
        val api = FakeExpHomeAssistantApi()
        api.historyResult = listOf(HistoryPoint(1.0, 40.0))

        // A day and a full week both stay inside the purge window.
        assertEquals(api.historyResult, HomeAssistantRepo(api).getHistoryForEntity("sensor.power", hoursBack = 24))
        assertEquals(api.historyResult, HomeAssistantRepo(api).getHistoryForEntity("sensor.power", hoursBack = 24 * 7))

        assertEquals(2, api.historyCalls.size)
        assertEquals(0, api.statisticsCalls.size)
    }

    @Test
    fun longWindowsReadStatisticsAtAPeriodMatchingTheSpan() = runTest {
        val api = FakeExpHomeAssistantApi()
        api.statisticsResult = listOf(HistoryPoint(1.0, 40.0, min = 10.0, max = 90.0))

        assertEquals(api.statisticsResult, HomeAssistantRepo(api).getHistoryForEntity("sensor.power", hoursBack = 24 * 30))
        assertEquals(api.statisticsResult, HomeAssistantRepo(api).getHistoryForEntity("sensor.power", hoursBack = 24 * 365))

        assertEquals(0, api.historyCalls.size)
        // A month of hourly buckets is ~720 points; a year of them would be ~8,700, so that steps to daily.
        assertEquals(
            listOf(StatisticsPeriod.HOUR, StatisticsPeriod.DAY),
            api.statisticsCalls.map { it.period },
        )
        assertEquals("sensor.power", api.statisticsCalls.first().entityId)
    }

    @Test
    fun longWindowFallsBackToRawStatesWhenAnEntityHasNoStatistics() = runTest {
        val api = FakeExpHomeAssistantApi()
        api.statisticsResult = emptyList()
        api.historyResult = listOf(HistoryPoint(1.0, 40.0))

        // Sensors without a state_class never get statistics — a short chart beats a blank one.
        assertEquals(api.historyResult, HomeAssistantRepo(api).getHistoryForEntity("sensor.odd", hoursBack = 24 * 30))

        assertEquals(1, api.statisticsCalls.size)
        assertEquals(1, api.historyCalls.size)
        // Both sources were asked for the same window.
        assertEquals(api.statisticsCalls.single().startIso, api.historyCalls.single().startIso)
        assertEquals(api.statisticsCalls.single().endIso, api.historyCalls.single().endIso)
    }
}
