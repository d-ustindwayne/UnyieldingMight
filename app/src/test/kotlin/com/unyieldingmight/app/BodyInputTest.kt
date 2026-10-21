package com.unyieldingmight.app

import com.unyieldingmight.app.data.UnitSystem
import com.unyieldingmight.app.ui.common.BodyInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BodyInputTest {

    @Test
    fun `metric input is stored as entered`() {
        val input = BodyInput()
        input.heightCm = "180"
        input.weight = "75.5"
        assertEquals(180.0, input.heightInCm())
        assertEquals(75.5, input.weightInKg())
        assertNull(input.heightError())
    }

    @Test
    fun `imperial input converts to cm and kg`() {
        val input = BodyInput(units = UnitSystem.IMPERIAL)
        input.feet = "5"
        input.inches = "11"
        input.weight = "165"
        assertEquals(180.34, input.heightInCm())
        assertEquals(74.84, input.weightInKg())
    }

    @Test
    fun `switching units keeps the same body measurements`() {
        val input = BodyInput(heightCm = 180.34, weightKg = 74.84)
        input.switchUnits(UnitSystem.IMPERIAL)
        assertEquals("5", input.feet)
        assertEquals("11", input.inches)
        input.switchUnits(UnitSystem.METRIC)
        assertEquals("180.3", input.heightCm)
    }

    @Test
    fun `out of range values are rejected`() {
        val input = BodyInput()
        input.heightCm = "20"
        input.weight = "abc"
        assertNotNull(input.heightError())
        assertNotNull(input.weightError())
    }
}
