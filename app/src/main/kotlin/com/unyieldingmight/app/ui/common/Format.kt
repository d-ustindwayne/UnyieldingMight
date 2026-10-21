package com.unyieldingmight.app.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Date/time formatting in the device's time zone. */
object Format {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val dayMonth = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
    private val fullDate = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    private val dayHeader = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())

    fun date(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun time(millis: Long): String = time.format(Instant.ofEpochMilli(millis).atZone(zone))

    fun timeRange(start: Long, end: Long): String = "${time(start)} – ${time(end)}"

    fun fullDate(millis: Long): String = fullDate.format(Instant.ofEpochMilli(millis).atZone(zone))

    /** "Today", "Tomorrow" or "Fri 24 Oct". */
    fun relativeDay(millis: Long): String {
        val days = ChronoUnit.DAYS.between(LocalDate.now(zone), date(millis))
        return when (days) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            else -> dayMonth.format(date(millis))
        }
    }

    fun dayHeader(date: LocalDate): String {
        val days = ChronoUnit.DAYS.between(LocalDate.now(zone), date)
        return when (days) {
            0L -> "Today"
            1L -> "Tomorrow"
            else -> dayHeader.format(date)
        }
    }

    /** Short label for day filter chips: "Today", "Tomorrow", "Fri 24". */
    fun chipDay(date: LocalDate): String = when (ChronoUnit.DAYS.between(LocalDate.now(zone), date)) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> DateTimeFormatter.ofPattern("EEE d", Locale.getDefault()).format(date)
    }

    fun ago(millis: Long): String {
        if (millis <= 0) return ""
        val minutes = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(millis), Instant.now())
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d ago"
            else -> dayMonth.format(Instant.ofEpochMilli(millis).atZone(zone))
        }
    }

    fun durationMinutes(minutes: Long): String = when {
        minutes < 60 -> "$minutes min"
        minutes % 60 == 0L -> "${minutes / 60} h"
        else -> "${minutes / 60} h ${minutes % 60} min"
    }
}
