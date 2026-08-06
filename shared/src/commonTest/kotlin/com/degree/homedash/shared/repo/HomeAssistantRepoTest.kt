package com.degree.homedash.shared.repo

import com.degree.homedash.shared.api.HaConfig
import com.degree.homedash.shared.api.HaConnectionStatus
import com.degree.homedash.shared.api.HomeAssistantApi
import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.model.HistoryPoint
import com.degree.homedash.shared.model.entity.HvacMode
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

    override suspend fun history(entityId: String, startIso: String, endIso: String): List<HistoryPoint> =
        emptyList()

    data class ServiceCall(
        val domain: String,
        val service: String,
        val entityId: String?,
        val serviceData: JsonObject?,
    )
}
