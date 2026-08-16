package com.example.unyieldingmight.Utils;

public class Conversion {
    // Conversion class converts the height or weight into its default unit that it is going to be stored in for the database

    // Rounds the number to the 2nd decimal place
    public static float rounded(float f) {
        return Math.round(f * 100.0f) / 100.0f;
    }
    // Inches to Centimeters
    public static float INtoCM(float val) {
        return rounded(val / 2.54f);
    }
    // Feet to Centimeters
    public static float FTtoCM(float val) { return rounded(val * 30.48f);}
    // Pounds to Kilograms
    public static float LBtoKG(float val) { return rounded(val / 2.205f); }
}
