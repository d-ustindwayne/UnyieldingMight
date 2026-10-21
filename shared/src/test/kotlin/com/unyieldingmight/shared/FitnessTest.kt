package com.unyieldingmight.shared

import com.unyieldingmight.shared.domain.CalorieBurner
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.domain.Units
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.Intensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FitnessTest {

    @Test
    fun `bmr follows Mifflin-St Jeor for each gender`() {
        // 10*75 + 6.25*180 - 5*20 = 1775
        assertEquals(1780.0, Fitness.bmr(75.0, 180.0, 20, Gender.MALE))
        assertEquals(1614.0, Fitness.bmr(75.0, 180.0, 20, Gender.FEMALE))
        assertEquals(1697.0, Fitness.bmr(75.0, 180.0, 20, Gender.OTHER))
    }

    @Test
    fun `tdee applies the activity multiplier`() {
        assertEquals(1780.0 * 1.9, Fitness.tdee(1780.0, ActivityLevel.EXTREME))
    }

    @Test
    fun `body stats are rounded`() {
        val stats = Fitness.bodyStats(180.0, 75.0, ActivityLevel.MODERATE, 20, Gender.MALE)
        assertEquals(23.1, stats.bmi)
        assertEquals(1780, stats.bmr)
        assertEquals(2759, stats.tdee)
    }

    @Test
    fun `bmi categories`() {
        assertEquals("Underweight", Fitness.bmiCategory(17.0))
        assertEquals("Healthy", Fitness.bmiCategory(22.0))
        assertEquals("Overweight", Fitness.bmiCategory(27.0))
        assertEquals("Obese", Fitness.bmiCategory(31.0))
    }

    @Test
    fun `activity level resolves legacy multipliers`() {
        assertEquals(ActivityLevel.MODERATE, ActivityLevel.fromMultiplier(1.55))
        // The old seed data used 1.275 which isn't a real level; it snaps to the nearest one.
        assertEquals(ActivityLevel.LIGHT, ActivityLevel.fromMultiplier(1.275 + 0.05))
    }

    @Test
    fun `intensity thresholds match the original app`() {
        assertEquals(Intensity.BEGINNER, Intensity.fromCalories(299))
        assertEquals(Intensity.INTERMEDIATE, Intensity.fromCalories(300))
        assertEquals(Intensity.INTERMEDIATE, Intensity.fromCalories(599))
        assertEquals(Intensity.ATHLETE, Intensity.fromCalories(600))
    }

    @Test
    fun `unit conversions`() {
        // The original INtoCM divided by 2.54; inches to cm must multiply.
        assertEquals(25.4, Units.inchesToCm(10.0))
        assertEquals(182.88, Units.feetToCm(6.0))
        assertEquals(180.34, Units.feetInchesToCm(5, 11.0))
        assertEquals(68.04, Units.lbToKg(150.0))
        assertEquals("5'11\"", Units.cmToFeetInchesLabel(180.34))
    }

    @Test
    fun `calories burned uses MET formula`() {
        // 8 MET * 3.5 * 70 / 200 * 30 = 294
        assertEquals(294, CalorieBurner.caloriesBurned(8.0, 70.0, 30.0))
        assertEquals(0, CalorieBurner.caloriesBurned(8.0, 0.0, 30.0))
        assertEquals(30, CalorieBurner.minutesToBurn(8.0, 70.0, 294))
        assertNotNull(CalorieBurner.byId("hiit"))
    }

    @Test
    fun `money formatting`() {
        assertEquals("$25.99", Money.format(2599))
        assertEquals("$0.05", Money.format(5))
        assertEquals("Free", Money.format(0, freeLabel = "Free"))
        assertEquals("-$1.50", Money.format(-150))
    }

    @Test
    fun `validation rules`() {
        assertNull(Validation.email("someone@example.com"))
        assertNotNull(Validation.email("not-an-email"))
        assertNotNull(Validation.password("short1"))
        assertNotNull(Validation.password("allletters"))
        assertNull(Validation.password("letters123"))
        assertNull(Validation.otp("012345"))
        assertNotNull(Validation.otp("12a456"))
        assertNotNull(Validation.heightCm(10.0))
        assertNull(Validation.weightKg(70.0))
    }
}
