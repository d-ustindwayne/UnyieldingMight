package com.unyieldingmight.shared

import com.unyieldingmight.shared.domain.ClassRecommender
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.Intensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClassRecommenderTest {
    private val now = 1_000_000_000_000L
    private val day = 86_400_000L

    private fun gymClass(
        id: Int,
        calories: Int,
        category: ClassCategory = ClassCategory.STRENGTH,
        startsIn: Long = 5 * day,
        booked: Int = 0,
        max: Int = 20,
        status: ClassStatus = ClassStatus.SCHEDULED,
        bookedByMe: Boolean = false,
    ) = GymClassDto(
        id = id, name = "Class $id", description = "", category = category, trainer = null,
        startsAt = now + startsIn, endsAt = now + startsIn + 3_600_000, maxCapacity = max,
        bookedCount = booked, avgCalories = calories, priceCents = 1500, status = status, bookedByMe = bookedByMe,
    )

    private val beginner = ClassRecommender.Profile(age = 30, activityLevel = ActivityLevel.INACTIVE, bmi = 22.0)
    private val athlete = ClassRecommender.Profile(age = 25, activityLevel = ActivityLevel.EXTREME, bmi = 23.0)

    @Test
    fun `preferred intensity follows activity level`() {
        assertEquals(Intensity.BEGINNER, ClassRecommender.preferredIntensity(beginner))
        assertEquals(Intensity.ATHLETE, ClassRecommender.preferredIntensity(athlete))
    }

    @Test
    fun `age and bmi ease the preferred intensity`() {
        val older = athlete.copy(age = 60)
        assertEquals(Intensity.INTERMEDIATE, ClassRecommender.preferredIntensity(older))
        val olderHighBmi = older.copy(bmi = 32.0)
        assertEquals(Intensity.BEGINNER, ClassRecommender.preferredIntensity(olderHighBmi))
    }

    @Test
    fun `matching intensity ranks first`() {
        val classes = listOf(gymClass(1, 800), gymClass(2, 200), gymClass(3, 450))
        val result = ClassRecommender.recommend(beginner, classes, now)
        assertEquals(listOf(2, 3), result.map { it.gymClass.id })
        assertTrue(result.first().reasons.any { "intensity" in it })
    }

    @Test
    fun `past, full, cancelled and already-booked classes are excluded`() {
        val classes = listOf(
            gymClass(1, 200, startsIn = -day),
            gymClass(2, 200, booked = 20, max = 20),
            gymClass(3, 200, status = ClassStatus.CANCELLED),
            gymClass(4, 200, bookedByMe = true),
        )
        assertTrue(ClassRecommender.recommend(beginner, classes, now).isEmpty())
    }

    @Test
    fun `favourite categories earn a bonus`() {
        val fan = athlete.copy(favouriteCategories = setOf(ClassCategory.COMBAT))
        val combat = ClassRecommender.score(fan, gymClass(1, 700, ClassCategory.COMBAT), now)!!
        val strength = ClassRecommender.score(fan, gymClass(2, 700, ClassCategory.STRENGTH), now)!!
        assertTrue(combat.score > strength.score)
    }

    @Test
    fun `classes two intensity steps away are not recommended without other reasons`() {
        assertNull(ClassRecommender.score(beginner, gymClass(1, 900), now))
    }
}
