package com.example.unyieldingmight;
import com.example.unyieldingmight.Models.Trainer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
public class TrainerTest {
    private Trainer trainer;

    @Before
    public void setUp() {
        trainer = new Trainer.Builder()
                .trainerID(1)
                .firstName("Dara")
                .lastName("Sanglap")
                .profileDescription("Testing trainer")
                .build();
    }

    @Test
    public void TrainerBuilderSuccess() {
        Assert.assertEquals(1, trainer.getTrainerID());
        Assert.assertEquals("Dara Sanglap", trainer.getName());
        Assert.assertEquals("Testing trainer", trainer.getProfileDescription());
    }
}