package com.degree.homedash.shared.api

import com.degree.homedash.shared.model.EntityState
import com.degree.homedash.shared.api.HaProtocolHelper.StatisticsPeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class WebSocketExpHomeAssistantApiTest {

    @Test
    fun statesAndConnectionDelegateToClient() = runTest {
        val client = FakeHaClient()
        val api = WebSocketExpHomeAssistantApi(client)

        // Same underlying flows, and updates propagate through.
        assertSame(client.states, api.loadAllStates())
        assertSame(client.connection, api.connection)

        client.states.value = mapOf("light.a" to EntityState("light.a", "on"))
        client.connection.value = HaConnectionStatus.Connected
        assertEquals("on", api.loadAllStates().first()["light.a"]?.state)
        assertEquals(HaConnectionStatus.Connected, api.connection.value)
    }

    @Test
    fun connectStartsClientWithConfig() {
        val client = FakeHaClient()
        val config = HaConfig(baseUrl = "http://ha.local", token = "t")

        WebSocketExpHomeAssistantApi(client).connect(config)

        assertEquals(config, client.startedConfig)
    }

    @Test
    fun disconnectStopsClient() {
        val client = FakeHaClient()

        WebSocketExpHomeAssistantApi(client).disconnect()

        assertEquals(1, client.stopCount)
    }

    @Test
    fun callServiceForwardsAllArguments() = runTest {
        val client = FakeHaClient()
        val data = buildJsonObject { put("percentage", 50) }

        api(client).callService("fan", "set_percentage", "fan.office", data)

        assertEquals(1, client.serviceCalls.size)
        assertEquals(FakeHaClient.ServiceCall("fan", "set_percentage", "fan.office", data), client.serviceCalls.single())
    }

    @Test
    fun callServiceDefaultsServiceDataToNull() = runTest {
        val client = FakeHaClient()

        api(client).callService("light", "toggle", "light.a")

        assertNull(client.serviceCalls.single().serviceData)
    }

    @Test
    fun toggleEntityCallsToggleInTheEntitysOwnDomain() = runTest {
        val client = FakeHaClient()

        api(client).toggleEntity("light.office_light")

        assertEquals(
            FakeHaClient.ServiceCall("light", "toggle", "light.office_light", null),
            client.serviceCalls.single(),
        )
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

        val points = WebSocketExpHomeAssistantApi(client).history("sensor.power", "2026-07-18T00:00:00Z", "2026-07-19T00:00:00Z")

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

        val points = WebSocketExpHomeAssistantApi(client).history("sensor.power", "s", "e")

        assertTrue(points.isEmpty())
    }

    @Test
    fun statisticsEncodesRequestAndParsesBuckets() = runTest {
        val client = FakeHaClient()
        // Response shape captured from a live HA instance: `start`/`end` are epoch *milliseconds*,
        // unlike history's seconds.
        client.requestResponse = """
            {"id":1,"type":"result","success":true,"result":{"sensor.moisture":[
              {"start":1782363600000,"end":1782450000000,"mean":29.5,"min":0.0,"max":91.46},
              {"start":1782450000000,"end":1782536400000,"mean":41.0,"min":39.94,"max":42.07}
            ]}}
        """.trimIndent()

        val points = WebSocketExpHomeAssistantApi(client)
            .statistics("sensor.moisture", "2026-06-25T00:00:00Z", "2026-08-17T00:00:00Z", StatisticsPeriod.DAY)

        assertEquals(2, points.size)
        assertEquals(1782363600.0, points[0].timeSeconds)
        assertEquals(29.5, points[0].value)
        assertEquals(0.0, points[0].min)
        assertEquals(91.46, points[0].max)
        assertEquals(1782450000.0, points[1].timeSeconds)
        assertEquals(41.0, points[1].value)

        val command = client.lastRequestCommand!!
        assertTrue(command.contains("recorder/statistics_during_period"), command)
        assertTrue(command.contains("sensor.moisture"), command)
        assertTrue(command.contains("2026-06-25T00:00:00Z"), command)
        assertTrue(command.contains("2026-08-17T00:00:00Z"), command)
        // The wire value, not the enum name.
        assertTrue(command.contains("\"period\":\"day\""), command)
        assertTrue(command.contains("mean"), command)
    }

    @Test
    fun statisticsFallsBackToOtherAggregatesWhenABucketHasNoMean() = runTest {
        val client = FakeHaClient()
        // Meter-style sensors record a total rather than a mean.
        client.requestResponse = """
            {"id":1,"type":"result","success":true,"result":{"sensor.energy":[
              {"start":1782363600000,"end":1782450000000,"sum":12.5},
              {"start":1782450000000,"end":1782536400000},
              {"start":1782536400000,"end":1782622800000,"max":7.0}
            ]}}
        """.trimIndent()

        val points = WebSocketExpHomeAssistantApi(client)
            .statistics("sensor.energy", "s", "e", StatisticsPeriod.HOUR)

        // The bucket with no usable number at all is skipped.
        assertEquals(2, points.size)
        assertEquals(12.5, points[0].value)
        // Absent min/max collapse onto the value, so nothing draws a spurious band.
        assertEquals(12.5, points[0].min)
        assertEquals(12.5, points[0].max)
        assertEquals(7.0, points[1].value)
    }

    @Test
    fun statisticsReturnsEmptyWhenEntityHasNoStatistics() = runTest {
        val client = FakeHaClient()
        client.requestResponse = """{"id":1,"type":"result","success":true,"result":{}}"""

        val points = WebSocketExpHomeAssistantApi(client)
            .statistics("sensor.power", "s", "e", StatisticsPeriod.HOUR)

        assertTrue(points.isEmpty())
    }

    @Test
    fun rawHistorySamplesCarryNoSpread() = runTest {
        val client = FakeHaClient()
        client.requestResponse =
            """{"id":1,"type":"result","success":true,"result":{"sensor.power":[{"s":"40.0","lu":100.0}]}}"""

        val point = WebSocketExpHomeAssistantApi(client).history("sensor.power", "s", "e").single()

        // An instantaneous reading is its own min and max — charts use this to decide not to band it.
        assertEquals(40.0, point.min)
        assertEquals(40.0, point.max)
    }
}

/**
 * The api under test with its launches running eagerly on the test scheduler — [callService] and
 * [WebSocketExpHomeAssistantApi.toggleEntity] are fire-and-forget, so without an unconfined
 * dispatcher the assertions would run before the call reached the client.
 */
private fun TestScope.api(client: HaClient) =
    WebSocketExpHomeAssistantApi(client, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

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
