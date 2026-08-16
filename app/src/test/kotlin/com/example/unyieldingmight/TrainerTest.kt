package com.example.unyieldingmight

import com.example.unyieldingmight.models.Trainer
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TrainerTest {
    private lateinit var trainer: Trainer

    @Before
    fun setUp() {
        trainer = Trainer(
            trainerID = 1,
            firstName = "Dara",
            lastName = "Sanglap",
            profileDescription = "Testing trainer"
        )
    }

    @Test
    fun testTrainerProperties() {
        Assert.assertEquals(1, trainer.trainerID)
        Assert.assertEquals("Dara Sanglap", trainer.name)
        Assert.assertEquals("Testing trainer", trainer.profileDescription)
    }
}
