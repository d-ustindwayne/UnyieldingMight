package com.unyieldingmight.shared.domain

import kotlin.math.pow
import kotlin.math.roundToLong

/** Converts user-entered measurements into the units stored in the database (cm, kg). */
object Units {
    private const val CM_PER_INCH = 2.54
    private const val CM_PER_FOOT = 30.48
    private const val LB_PER_KG = 2.2046226218

    fun round(value: Double, decimals: Int = 2): Double {
        val factor = 10.0.pow(decimals)
        return (value * factor).roundToLong() / factor
    }

    fun inchesToCm(inches: Double): Double = round(inches * CM_PER_INCH)

    fun feetToCm(feet: Double): Double = round(feet * CM_PER_FOOT)

    fun feetInchesToCm(feet: Int, inches: Double): Double = round(feet * CM_PER_FOOT + inches * CM_PER_INCH)

    fun cmToInches(cm: Double): Double = round(cm / CM_PER_INCH)

    fun lbToKg(lb: Double): Double = round(lb / LB_PER_KG)

    fun kgToLb(kg: Double): Double = round(kg * LB_PER_KG)

    /** e.g. 180.34 cm -> "5'11\"" */
    fun cmToFeetInchesLabel(cm: Double): String {
        val totalInches = (cm / CM_PER_INCH).roundToLong()
        return "${totalInches / 12}'${totalInches % 12}\""
    }
}
