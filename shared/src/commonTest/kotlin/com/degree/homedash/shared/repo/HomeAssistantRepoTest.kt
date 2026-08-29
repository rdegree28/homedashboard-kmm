package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.StatisticsPeriod
import com.degree.homedash.shared.model.device_metadata.HvacMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the exact wire shape of the `climate.*` service calls. Previews can show the card's every
 * state but can't catch a misspelled service name or a mode sent as `"Cool"` instead of `"cool"`.
 */
class HomeAssistantRepoTest {

    private val thermostat = "climate.living_room_thermostat"

    @Test
    fun setTargetTemperatureCallsClimateSetTemperature() = runTest {
        val api = FakeHomeAssistantApi()

        HomeAssistantRepo(api).setTargetTemperature(thermostat, 72.0)

        assertEquals(
            FakeHomeAssistantApi.ServiceCall(
                "climate", "set_temperature", thermostat,
                buildJsonObject { put("temperature", 72.0) },
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun setHvacModeSendsHomeAssistantsSnakeCaseValue() = runTest {
        val api = FakeHomeAssistantApi()

        HomeAssistantRepo(api).setHvacMode(thermostat, HvacMode.HeatCool)

        assertEquals(
            FakeHomeAssistantApi.ServiceCall(
                "climate", "set_hvac_mode", thermostat,
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
        val api = FakeHomeAssistantApi()

        HomeAssistantRepo(api).setThermostatFanMode(thermostat, "low")

        assertEquals(
            FakeHomeAssistantApi.ServiceCall(
                "climate", "set_fan_mode", thermostat,
                buildJsonObject { put("fan_mode", "low") },
            ),
            api.serviceCalls.single(),
        )
    }

    @Test
    fun setPresetModeCallsClimateSetPresetMode() = runTest {
        val api = FakeHomeAssistantApi()

        HomeAssistantRepo(api).setPresetMode(thermostat, "eco")

        assertEquals(
            FakeHomeAssistantApi.ServiceCall(
                "climate", "set_preset_mode", thermostat,
                buildJsonObject { put("preset_mode", "eco") },
            ),
            api.serviceCalls.single(),
        )
    }

    // --- history source selection ---
    // The recorder purges raw states after ~10 days, so which source a window reads from is the whole
    // difference between a 1y chart showing a year and showing the last week and a half.

    @Test
    fun shortWindowsReadRawRecorderStates() = runTest {
        val api = FakeHomeAssistantApi()
        api.historyResult = listOf(HistoryPoint(1.0, 40.0))

        // A day and a full week both stay inside the purge window.
        assertEquals(api.historyResult, HomeAssistantRepo(api).history("sensor.power", hoursBack = 24))
        assertEquals(api.historyResult, HomeAssistantRepo(api).history("sensor.power", hoursBack = 24 * 7))

        assertEquals(2, api.historyCalls.size)
        assertEquals(0, api.statisticsCalls.size)
    }

    @Test
    fun longWindowsReadStatisticsAtAPeriodMatchingTheSpan() = runTest {
        val api = FakeHomeAssistantApi()
        api.statisticsResult = listOf(HistoryPoint(1.0, 40.0, min = 10.0, max = 90.0))

        assertEquals(api.statisticsResult, HomeAssistantRepo(api).history("sensor.power", hoursBack = 24 * 30))
        assertEquals(api.statisticsResult, HomeAssistantRepo(api).history("sensor.power", hoursBack = 24 * 365))

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
        val api = FakeHomeAssistantApi()
        api.statisticsResult = emptyList()
        api.historyResult = listOf(HistoryPoint(1.0, 40.0))

        // Sensors without a state_class never get statistics — a short chart beats a blank one.
        assertEquals(api.historyResult, HomeAssistantRepo(api).history("sensor.odd", hoursBack = 24 * 30))

        assertEquals(1, api.statisticsCalls.size)
        assertEquals(1, api.historyCalls.size)
        // Both sources were asked for the same window.
        assertEquals(api.statisticsCalls.single().startIso, api.historyCalls.single().startIso)
        assertEquals(api.statisticsCalls.single().endIso, api.historyCalls.single().endIso)
    }
}

/** In-memory [HomeAssistantApi] that records the calls the repo makes. */
private class FakeHomeAssistantApi : HomeAssistantApi {
    override val states = MutableStateFlow<Map<String, EntityState>>(emptyMap())
    override val connection = MutableStateFlow<HaConnectionStatus>(HaConnectionStatus.Disconnected)

    val serviceCalls = mutableListOf<ServiceCall>()

    override fun connect(config: HaConfig) = Unit
    override fun disconnect() = Unit

    override suspend fun callService(
        domain: String,
        service: String,
        entityId: String?,
        serviceData: JsonObject?,
    ) {
        serviceCalls += ServiceCall(domain, service, entityId, serviceData)
    }

    val historyCalls = mutableListOf<HistoryCall>()
    val statisticsCalls = mutableListOf<StatisticsCall>()
    var historyResult: List<HistoryPoint> = emptyList()
    var statisticsResult: List<HistoryPoint> = emptyList()

    override suspend fun history(entityId: String, startIso: String, endIso: String): List<HistoryPoint> {
        historyCalls += HistoryCall(entityId, startIso, endIso)
        return historyResult
    }

    override suspend fun statistics(
        entityId: String,
        startIso: String,
        endIso: String,
        period: StatisticsPeriod,
    ): List<HistoryPoint> {
        statisticsCalls += StatisticsCall(entityId, startIso, endIso, period)
        return statisticsResult
    }

    data class HistoryCall(val entityId: String, val startIso: String, val endIso: String)

    data class StatisticsCall(
        val entityId: String,
        val startIso: String,
        val endIso: String,
        val period: StatisticsPeriod,
    )

    data class ServiceCall(
        val domain: String,
        val service: String,
        val entityId: String?,
        val serviceData: JsonObject?,
    )
}
