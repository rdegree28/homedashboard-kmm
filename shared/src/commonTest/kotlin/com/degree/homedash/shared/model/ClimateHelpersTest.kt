package com.degree.homedash.shared.model

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The dew point maths, which every climate card and thermostat readout leans on. */
class ClimateHelpersTest {

    @Test
    fun dewPointComesBackInTheScaleItWasGiven() {
        // 21.4 °C at 59 % RH is about 13.1 °C — the same air, expressed either way.
        val celsius = dewPoint(temperature = 21.4, rh = 59.0, fahrenheit = false)!!
        val fahrenheit = dewPoint(temperature = 70.5, rh = 59.0, fahrenheit = true)!!

        assertTrue(abs(celsius - 13.1) < 0.2, "got $celsius")
        assertTrue(abs(fahrenheit - 55.6) < 0.4, "got $fahrenheit")
        // The two agree once converted, so neither path is doing its own thing.
        assertTrue(abs((celsius * 9 / 5 + 32) - fahrenheit) < 0.5)
    }

    @Test
    fun saturatedAirIsAtItsOwnTemperature() {
        // At 100 % humidity the air is already at its dew point.
        val dew = dewPoint(temperature = 20.0, rh = 100.0, fahrenheit = false)!!

        assertTrue(abs(dew - 20.0) < 0.1, "got $dew")
    }

    @Test
    fun drierAirHasALowerDewPoint() {
        val damp = dewPoint(temperature = 21.0, rh = 70.0, fahrenheit = false)!!
        val dry = dewPoint(temperature = 21.0, rh = 30.0, fahrenheit = false)!!

        assertTrue(dry < damp, "$dry should sit below $damp")
    }

    @Test
    fun anImpossibleHumidityHasNoDewPoint() {
        // The formula's ln(rh/100) diverges at zero, so there is no answer to give.
        assertNull(dewPoint(temperature = 20.0, rh = 0.0, fahrenheit = false))
        assertNull(dewPoint(temperature = 20.0, rh = -5.0, fahrenheit = false))
    }
}
