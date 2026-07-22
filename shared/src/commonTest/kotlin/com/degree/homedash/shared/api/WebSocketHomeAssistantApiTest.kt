package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WebSocketHomeAssistantApiTest {

    @Test
    fun statesAndConnectionDelegateToClient() {
        val client = FakeHaClient()
        val api = WebSocketHomeAssistantApi(client)

        // Same underlying flows, and updates propagate through.
        assertSame(client.states, api.states)
        assertSame(client.connection, api.connection)

        client.states.value = mapOf("light.a" to EntityState("light.a", "on"))
        client.connection.value = HaConnectionStatus.Connected
        assertEquals("on", api.states.value["light.a"]?.state)
        assertEquals(HaConnectionStatus.Connected, api.connection.value)
    }

    @Test
    fun connectStartsClientWithConfig() {
        val client = FakeHaClient()
        val config = HaConfig(baseUrl = "http://ha.local", token = "t")

        WebSocketHomeAssistantApi(client).connect(config)

        assertEquals(config, client.startedConfig)
    }

    @Test
    fun disconnectStopsClient() {
        val client = FakeHaClient()

        WebSocketHomeAssistantApi(client).disconnect()

        assertEquals(1, client.stopCount)
    }

    @Test
    fun callServiceForwardsAllArguments() = runTest {
        val client = FakeHaClient()
        val data = buildJsonObject { put("percentage", 50) }

        WebSocketHomeAssistantApi(client).callService("fan", "set_percentage", "fan.office", data)

        assertEquals(1, client.serviceCalls.size)
        assertEquals(FakeHaClient.ServiceCall("fan", "set_percentage", "fan.office", data), client.serviceCalls.single())
    }

    @Test
    fun callServiceDefaultsServiceDataToNull() = runTest {
        val client = FakeHaClient()

        WebSocketHomeAssistantApi(client).callService("light", "toggle", "light.a")

        assertNull(client.serviceCalls.single().serviceData)
    }

    @Test
    fun historyEncodesRequestAndParsesResponse() = runTest {
        val client = FakeHaClient()
        client.requestResponse = """
            {"id":1,"type":"result","success":true,"result":{"sensor.power":[
              {"s":"40.0","lu":100.0},
              {"s":"unavailable","lu":150.0},
              {"s":"55.5","lu":200.0}
            ]}}
        """.trimIndent()

        val points = WebSocketHomeAssistantApi(client).history("sensor.power", "2026-07-18T00:00:00Z", "2026-07-19T00:00:00Z")

        // The non-numeric "unavailable" sample is dropped; the rest parse in order.
        assertEquals(2, points.size)
        assertEquals(100.0, points[0].timeSeconds)
        assertEquals(40.0, points[0].value)
        assertEquals(200.0, points[1].timeSeconds)
        assertEquals(55.5, points[1].value)

        // The command carried the entity id and the exact time bounds.
        val command = client.lastRequestCommand!!
        assertTrue(command.contains("sensor.power"), command)
        assertTrue(command.contains("2026-07-18T00:00:00Z"), command)
        assertTrue(command.contains("2026-07-19T00:00:00Z"), command)
    }

    @Test
    fun historyReturnsEmptyWhenNoDataForEntity() = runTest {
        val client = FakeHaClient()
        client.requestResponse = """{"id":1,"type":"result","success":true,"result":{}}"""

        val points = WebSocketHomeAssistantApi(client).history("sensor.power", "s", "e")

        assertTrue(points.isEmpty())
    }
}

/** In-memory [HaClient] that records interactions and returns canned request responses. */
private class FakeHaClient : HaClient {
    override val states = MutableStateFlow<Map<String, EntityState>>(emptyMap())
    override val connection = MutableStateFlow<HaConnectionStatus>(HaConnectionStatus.Disconnected)

    var startedConfig: HaConfig? = null
    var stopCount = 0
    val serviceCalls = mutableListOf<ServiceCall>()
    var lastRequestCommand: String? = null
    var requestResponse: String = "{}"

    override fun start(config: HaConfig) {
        startedConfig = config
    }

    override fun stop() {
        stopCount++
    }

    override suspend fun callService(domain: String, service: String, entityId: String?, serviceData: JsonObject?) {
        serviceCalls += ServiceCall(domain, service, entityId, serviceData)
    }

    override suspend fun request(buildCommand: (Long) -> String): String {
        lastRequestCommand = buildCommand(REQUEST_ID)
        return requestResponse
    }

    data class ServiceCall(val domain: String, val service: String, val entityId: String?, val serviceData: JsonObject?)

    companion object {
        const val REQUEST_ID = 7L
    }
}
