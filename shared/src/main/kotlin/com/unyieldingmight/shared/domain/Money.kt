package com.unyieldingmight.shared.domain

object Money {
    /** 2599 -> "$25.99", 0 -> "Free" when [freeLabel] is set. */
    fun format(cents: Long, freeLabel: String? = null): String {
        if (cents == 0L && freeLabel != null) return freeLabel
        val sign = if (cents < 0) "-" else ""
        val abs = kotlin.math.abs(cents)
        return "$sign$${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    }

    fun format(cents: Int, freeLabel: String? = null): String = format(cents.toLong(), freeLabel)
}
