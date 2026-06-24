package com.degree.homedash.shared.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HaProtocolTest {

    @Test
    fun readsMessageType() {
        assertEquals("auth_required", HaProtocol.messageType("""{"type":"auth_required","ha_version":"x"}"""))
        assertEquals("auth_ok", HaProtocol.messageType("""{"type":"auth_ok"}"""))
        assertNull(HaProtocol.messageType("not json"))
    }

    @Test
    fun parsesGetStatesResult() {
        val text = """
            {"id":2,"type":"result","success":true,"result":[
              {"entity_id":"light.office_light","state":"on","attributes":{"friendly_name":"Office Light"}},
              {"entity_id":"sensor.office_workstation_power","state":"5.2","attributes":{"unit_of_measurement":"W"}}
            ]}
        """.trimIndent()

        assertTrue(HaProtocol.isResultSuccess(text))
        assertEquals(2L, HaProtocol.resultId(text))

        val states = HaProtocol.parseStates(text)
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

        val change = HaProtocol.parseStateChanged(text)
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

        val change = HaProtocol.parseStateChanged(text)
        assertEquals("light.gone", change?.entityId)
        assertNull(change?.newState)
    }

    @Test
    fun encodesAuthAndCommands() {
        val auth = HaProtocol.encodeAuth("TOKEN123")
        assertTrue(auth.contains("\"auth\""))
        assertTrue(auth.contains("\"access_token\":\"TOKEN123\""))

        val getStates = HaProtocol.encodeGetStates(1)
        assertEquals("get_states", HaProtocol.messageType(getStates))
        assertEquals(1L, HaProtocol.resultId(getStates))

        val call = HaProtocol.encodeCallService(7, "light", "toggle", "light.office_light")
        assertEquals("call_service", HaProtocol.messageType(call))
        assertTrue(call.contains("\"domain\":\"light\""))
        assertTrue(call.contains("\"service\":\"toggle\""))
        assertTrue(call.contains("light.office_light"))
    }

    @Test
    fun callServiceWithoutEntityOmitsTarget() {
        val call = HaProtocol.encodeCallService(8, "homeassistant", "restart", entityId = null)
        // explicitNulls=false should drop the null target entirely
        assertTrue(!call.contains("target"))
    }
}
