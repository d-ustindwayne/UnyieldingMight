package com.unyieldingmight.server

import java.time.LocalDate
import java.time.ZoneId

/** The gym's local time zone, used for "today" (ages, schedules, email times) instead of server UTC. */
object GymTime {
    val zone: ZoneId = ZoneId.of(System.getenv("GYM_TIMEZONE") ?: "Pacific/Auckland")

    fun today(): LocalDate = LocalDate.now(zone)
}
