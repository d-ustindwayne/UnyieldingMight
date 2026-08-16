package com.example.unyieldingmight.utils

object Conversion {
    // Conversion class converts the height or weight into its default unit that it is going to be stored in for the database
    // Rounds the number to the 2nd decimal place
    fun rounded(f: Float): Float {
        return Math.round(f * 100.0f) / 100.0f
    }

    // Inches to Centimeters
    fun INtoCM(`val`: Float): Float {
        return rounded(`val` / 2.54f)
    }

    // Feet to Centimeters
    fun FTtoCM(`val`: Float): Float {
        return rounded(`val` * 30.48f)
    }

    // Pounds to Kilograms
    fun LBtoKG(`val`: Float): Float {
        return rounded(`val` / 2.205f)
    }
}
