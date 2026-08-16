package com.example.unyieldingmight

import com.example.unyieldingmight.models.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.Date

class GymClassTest {
    private lateinit var gymClass: GymClass
    private lateinit var trainer: Trainer
    private val date = Date(System.currentTimeMillis())

    @Before
    fun setUp() {
        trainer = Trainer(
            trainerID = 1,
            firstName = "Dara",
            lastName = "Sanglap",
            profileDescription = "Testing trainer"
        )

        gymClass = GymClass(
            ID = 1,
            name = "HIIT",
            trainer = trainer,
            startDateTime = date,
            endDateTime = date,
            description = "Testing gym class class",
            currentCapacity = 12,
            maxCapacity = 15,
            status = ClassStatus.ONGOING,
            avgCaloriesBurnedPerDay = 1300f
        )
    }

    @Test
    fun testGymClassProperties() {
        Assert.assertEquals(1, gymClass.ID)
        Assert.assertEquals("HIIT", gymClass.name)
        Assert.assertEquals(trainer, gymClass.trainer)
        Assert.assertEquals(date, gymClass.startDateTime)
        Assert.assertEquals(date, gymClass.endDateTime)
        Assert.assertEquals("Testing gym class class", gymClass.description)
        Assert.assertEquals(12, gymClass.currentCapacity)
        Assert.assertEquals(15, gymClass.maxCapacity)
        Assert.assertEquals(ClassStatus.ONGOING, gymClass.status)
        Assert.assertEquals(1300f, gymClass.avgCaloriesBurnedPerDay, 0.1f)
    }

    @Test
    fun testIntensityAthlete() {
        Assert.assertEquals(GymClass.Intensity.ATHLETE, gymClass.intensity)
    }

    @Test
    fun testIntensityIntermediate() {
        val intermediateClass = gymClass.copy(avgCaloriesBurnedPerDay = 400f)
        Assert.assertEquals(GymClass.Intensity.INTERMEDIATE, intermediateClass.intensity)
    }

    @Test
    fun testIntensityBeginner() {
        val beginnerClass = gymClass.copy(avgCaloriesBurnedPerDay = 200f)
        Assert.assertEquals(GymClass.Intensity.BEGINNER, beginnerClass.intensity)
    }
}
