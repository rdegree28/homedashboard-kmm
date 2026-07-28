package com.degree.homedash.shared.network

import com.degree.homedash.shared.api.HaProtocolHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HaProtocolHelperTest {

    @Test
    fun readsMessageType() {
        assertEquals("auth_required", HaProtocolHelper.messageType("""{"type":"auth_required","ha_version":"x"}"""))
        assertEquals("auth_ok", HaProtocolHelper.messageType("""{"type":"auth_ok"}"""))
        assertNull(HaProtocolHelper.messageType("not json"))
    }

    @Test
    fun parsesGetStatesResult() {
        val text = """
            {"id":2,"type":"result","success":true,"result":[
              {"entity_id":"light.office_light","state":"on","attributes":{"friendly_name":"Office Light"}},
              {"entity_id":"sensor.office_workstation_power","state":"5.2","attributes":{"unit_of_measurement":"W"}}
            ]}
        """.trimIndent()

        assertTrue(HaProtocolHelper.isResultSuccess(text))
        assertEquals(2L, HaProtocolHelper.resultId(text))

        val states = HaProtocolHelper.parseStates(text)
        assertEquals(2, states.size)
        val light = states.first { it.entityId == "light.office_light" }
        assertTrue(light.isOn)
        assertEquals("Office Light", light.friendlyName)
        assertEquals("light", light.domain)
        val power = states.first { it.entityId == "sensor.office_workstation_power" }
        assertEquals("W", power.attrString("unit_of_measurement"))
    }

    @Test
    fun parsesStateChangedWithNewState() {
        val text = """
            {"type":"event","event":{"event_type":"state_changed","data":{
              "entity_id":"fan.office_box_fan",
              "new_state":{"entity_id":"fan.office_box_fan","state":"on","attributes":{}},
              "old_state":{"entity_id":"fan.office_box_fan","state":"off","attributes":{}}
            }}}
        """.trimIndent()

        val change = HaProtocolHelper.parseStateChanged(text)
        assertEquals("fan.office_box_fan", change?.entityId)
        assertEquals("on", change?.newState?.state)
        assertTrue(change?.newState?.isOn == true)
    }

    @Test
    fun stateChangedRemovalHasNullNewState() {
        val text = """
            {"type":"event","event":{"event_type":"state_changed","data":{
              "entity_id":"light.gone","new_state":null,
              "old_state":{"entity_id":"light.gone","state":"on","attributes":{}}
            }}}
        """.trimIndent()

        val change = HaProtocolHelper.parseStateChanged(text)
        assertEquals("light.gone", change?.entityId)
        assertNull(change?.newState)
    }

    @Test
    fun encodesAuthAndCommands() {
        val auth = HaProtocolHelper.encodeAuth("TOKEN123")
        assertTrue(auth.contains("\"auth\""))
        assertTrue(auth.contains("\"access_token\":\"TOKEN123\""))

        val getStates = HaProtocolHelper.encodeGetStates(1)
        assertEquals("get_states", HaProtocolHelper.messageType(getStates))
        assertEquals(1L, HaProtocolHelper.resultId(getStates))

        val call = HaProtocolHelper.encodeCallService(7, "light", "toggle", "light.office_light")
        assertEquals("call_service", HaProtocolHelper.messageType(call))
        assertTrue(call.contains("\"domain\":\"light\""))
        assertTrue(call.contains("\"service\":\"toggle\""))
        assertTrue(call.contains("light.office_light"))
    }

    @Test
    fun callServiceWithoutEntityOmitsTarget() {
        val call = HaProtocolHelper.encodeCallService(8, "homeassistant", "restart", entityId = null)
        // explicitNulls=false should drop the null target entirely
        assertTrue(!call.contains("target"))
    }

    @Test
    fun parseRootRejectsMalformedJson() {
        assertNull(HaProtocolHelper.parseRoot("not json"))
        assertNull(HaProtocolHelper.parseRoot("""["an","array"]"""))
        assertNotNull(HaProtocolHelper.parseRoot("""{"type":"auth_ok"}"""))
    }

    /**
     * The receive loop parses a frame once and calls the [kotlinx.serialization.json.JsonObject]
     * overloads; everything else calls the String ones. They must not drift.
     */
    @Test
    fun jsonObjectOverloadsAgreeWithStringOverloads() {
        val frames = listOf(
            """{"id":2,"type":"result","success":true,"result":[
              {"entity_id":"light.office_light","state":"on","attributes":{"friendly_name":"Office Light"}}
            ]}""",
            """{"id":3,"type":"result","success":false,"error":{"code":"x"}}""",
            """{"type":"event","event":{"event_type":"state_changed","data":{
              "entity_id":"fan.office_box_fan",
              "new_state":{"entity_id":"fan.office_box_fan","state":"on","attributes":{}}
            }}}""",
            """{"type":"event","event":{"event_type":"state_changed","data":{
              "entity_id":"light.gone","new_state":null
            }}}""",
            """{"type":"auth_ok"}""",
        )

        for (text in frames) {
            val root = assertNotNull(HaProtocolHelper.parseRoot(text), text)
            assertEquals(HaProtocolHelper.messageType(text), HaProtocolHelper.messageType(root), text)
            assertEquals(HaProtocolHelper.resultId(text), HaProtocolHelper.resultId(root), text)
            assertEquals(HaProtocolHelper.isResultSuccess(text), HaProtocolHelper.isResultSuccess(root), text)
            assertEquals(HaProtocolHelper.parseStates(text), HaProtocolHelper.parseStates(root), text)
            assertEquals(HaProtocolHelper.parseStateChanged(text), HaProtocolHelper.parseStateChanged(root), text)
        }
    }
}
