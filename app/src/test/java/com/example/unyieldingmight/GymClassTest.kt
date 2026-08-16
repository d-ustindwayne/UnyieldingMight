package com.example.unyieldingmight;
import com.example.unyieldingmight.Models.Address;
import com.example.unyieldingmight.Models.ClassStatus;
import com.example.unyieldingmight.Models.Gender;
import com.example.unyieldingmight.Models.GymClass;
import com.example.unyieldingmight.Models.Profile;
import com.example.unyieldingmight.Models.Trainer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class GymClassTest {

    private GymClass gymClass;
    private Trainer trainer;
    private Date date = new Date();

    @Before
    public void setUp() {
        trainer = new Trainer.Builder()
                .trainerID(1)
                .firstName("Dara")
                .lastName("Sanglap")
                .profileDescription("Testing trainer")
                .build();

        gymClass = new GymClass.Builder()
                .ID(1)
                .name("HIIT")
                .trainer(trainer)
                .startDateTime(date)
                .endDateTime(date)
                .description("Testing gym class class")
                .currentCapacity(12)
                .maxCapacity(15)
                .status(ClassStatus.ONGOING)
                .avgCaloriesBurnedPerDay(1300)
                .build();


    }
    @Test
    public void GymClassBuilderSuccess(){
        Assert.assertEquals(1, gymClass.getID());
        Assert.assertEquals("HIIT", gymClass.getName());
        Assert.assertEquals(trainer, gymClass.getTrainer());
        Assert.assertEquals(date, gymClass.getStartDateTime());
        Assert.assertEquals(date, gymClass.getEndDateTime());
        Assert.assertEquals("Testing gym class class", gymClass.getDescription());
        Assert.assertEquals(12, gymClass.getCurrentCapacity());
        Assert.assertEquals(15, gymClass.getMaxCapacity());
        Assert.assertEquals(ClassStatus.ONGOING, gymClass.getStatus());
        Assert.assertEquals(1300, gymClass.getAvgCaloriesBurnedPerDay(), 0.0);

    }
    //This test is already part of the first instance that was created in the setUp method
    // ratio = 1300 / 2000 = 0.65 → ATHLETE
    @Test
    public void getIntensity_Athlete(){
        Assert.assertEquals(GymClass.Intensity.ATHLETE, gymClass.getIntensity(10));
    }

    //This is for the intermediate level testing
    // ratio = 400 / 2000 = 0.20 → INTERMEDIATE
    @Test
    public void getIntensity_Intermediate(){
        GymClass intermediateClass = new GymClass.Builder()
                .ID(1)
                .name("HIIT")
                .trainer(trainer)
                .startDateTime(date)
                .endDateTime(date)
                .description("Testing gym class")
                .currentCapacity(12)
                .maxCapacity(15)
                .status(ClassStatus.ONGOING)
                .avgCaloriesBurnedPerDay(400f)
                .build();

        Assert.assertEquals(GymClass.Intensity.INTERMEDIATE, intermediateClass.getIntensity());
    }

    //This is for the beginner level testing
    // ratio = 300 / 2000 = 0.15 → BEGINNER
    @Test
    public void getIntensity_Beginner(){
        GymClass beginnerClass = new GymClass.Builder()
                .ID(1)
                .name("HIIT")
                .trainer(trainer)
                .startDateTime(date)
                .endDateTime(date)
                .description("Testing gym class")
                .currentCapacity(12)
                .maxCapacity(15)
                .status(ClassStatus.ONGOING)
                .avgCaloriesBurnedPerDay(300f)
                .build();

        Assert.assertEquals(GymClass.Intensity.BEGINNER, beginnerClass.getIntensity());
    }
}