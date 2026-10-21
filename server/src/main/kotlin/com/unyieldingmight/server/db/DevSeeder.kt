package com.unyieldingmight.server.db

import com.unyieldingmight.server.auth.Passwords
import com.unyieldingmight.server.repo.insertAddress
import com.unyieldingmight.server.repo.insertClass
import com.unyieldingmight.server.repo.insertCustomerProfile
import com.unyieldingmight.server.repo.insertShopItem
import com.unyieldingmight.server.repo.insertTrainer
import com.unyieldingmight.server.repo.insertUser
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemUpsertRequest
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Demo data for local development and the showcase build. Class names and descriptions come
 * from the original project's seed script; the schedule is generated relative to today so the
 * app always has upcoming classes to book.
 */
object DevSeeder {
    private val log = LoggerFactory.getLogger(DevSeeder::class.java)
    private val gymZone: ZoneId = ZoneId.of("Pacific/Auckland")
    const val DEMO_PASSWORD = "password123"

    private data class WeeklyClass(
        val name: String,
        val description: String,
        val category: ClassCategory,
        val trainer: Int,
        val day: DayOfWeek,
        val start: LocalTime,
        val minutes: Long,
        val capacity: Int,
        val calories: Int,
        val priceCents: Int,
    )

    private val schedule = listOf(
        WeeklyClass("Power Lifting", "Focus on compound movements: squat, bench and deadlift with coached technique.", ClassCategory.STRENGTH, 0, DayOfWeek.MONDAY, LocalTime.of(9, 0), 90, 10, 450, 2000),
        WeeklyClass("Beginner Yoga", "Focus on flexibility, breathing and balance. Perfect first class.", ClassCategory.MIND_BODY, 3, DayOfWeek.MONDAY, LocalTime.of(11, 0), 60, 20, 250, 1500),
        WeeklyClass("Muay Thai Striking", "Learn basic kicks, elbows, and knees with fight-prep intensity.", ClassCategory.COMBAT, 4, DayOfWeek.MONDAY, LocalTime.of(17, 0), 90, 12, 720, 2500),
        WeeklyClass("HIIT Blast", "High intensity cardio intervals to spike your heart rate.", ClassCategory.CONDITIONING, 0, DayOfWeek.TUESDAY, LocalTime.of(8, 0), 60, 15, 600, 1800),
        WeeklyClass("Spin & Sweat", "High-energy rhythm indoor cycling to crush your morning cardio.", ClassCategory.CARDIO, 3, DayOfWeek.TUESDAY, LocalTime.of(6, 0), 60, 25, 550, 1800),
        WeeklyClass("Core Crusher", "Targeted abs, lower back, and obliques burning session.", ClassCategory.CONDITIONING, 3, DayOfWeek.TUESDAY, LocalTime.of(12, 0), 45, 20, 320, 1200),
        WeeklyClass("Rugby Fit Intervals", "Power endurance circuits inspired by professional rugby training.", ClassCategory.CONDITIONING, 2, DayOfWeek.WEDNESDAY, LocalTime.of(7, 0), 90, 18, 780, 2500),
        WeeklyClass("Zumba Dance Party", "Groove your way into a heavy caloric deficit. Rhythm optional.", ClassCategory.DANCE, 1, DayOfWeek.WEDNESDAY, LocalTime.of(18, 0), 60, 30, 420, 1500),
        WeeklyClass("Aqua Fit", "Low-impact water workout. Scoob your inner self.", ClassCategory.CARDIO, 1, DayOfWeek.THURSDAY, LocalTime.of(8, 0), 60, 15, 267, 1500),
        WeeklyClass("Olympic Weightlifting", "Technical breakdown of the snatch, clean, and jerk.", ClassCategory.STRENGTH, 1, DayOfWeek.THURSDAY, LocalTime.of(10, 0), 90, 8, 480, 2500),
        WeeklyClass("Pilates Alignment", "Low-impact muscle toning, flexibility, and posture control.", ClassCategory.MIND_BODY, 3, DayOfWeek.THURSDAY, LocalTime.of(16, 0), 60, 15, 290, 1500),
        WeeklyClass("MetCon Madness", "Metabolic conditioning to completely test your VO2 max thresholds.", ClassCategory.CONDITIONING, 0, DayOfWeek.FRIDAY, LocalTime.of(6, 30), 60, 15, 660, 2000),
        WeeklyClass("Boxing Conditioning", "Heavy bag drills, pad combinations, and intense footwork.", ClassCategory.COMBAT, 2, DayOfWeek.FRIDAY, LocalTime.of(17, 30), 60, 14, 690, 2000),
        WeeklyClass("Flexibility & Mobility", "Deep active recovery stretching and joint restoration.", ClassCategory.MIND_BODY, 1, DayOfWeek.SATURDAY, LocalTime.of(10, 0), 60, 25, 180, 0),
        WeeklyClass("Strongman Sunday", "Flip commercial truck tires, carry logs, and build unyielding might.", ClassCategory.STRENGTH, 0, DayOfWeek.SUNDAY, LocalTime.of(9, 0), 120, 10, 880, 3000),
    )

    fun seedIfEmpty(db: Database, weeks: Int = 3, today: LocalDate = LocalDate.now(gymZone)) {
        val hasUsers = db.txBlocking { count("SELECT COUNT(*) FROM user_account") > 0 }
        if (hasUsers) return
        log.info("Seeding demo data")
        val passwordHash = Passwords.hash(DEMO_PASSWORD)

        db.txBlocking {
            val now = Instant.now()

            // Membership plans
            insert("INSERT INTO membership_plan (code, name, description, price_cents, duration_days, active) VALUES (?, ?, ?, ?, ?, TRUE)",
                "MONTHLY", "Monthly", "Unlimited classes and full gym access for 30 days.", 4900, 30)
            insert("INSERT INTO membership_plan (code, name, description, price_cents, duration_days, active) VALUES (?, ?, ?, ?, ?, TRUE)",
                "QUARTERLY", "Quarterly", "Three months of unlimited classes. Save 15%.", 12500, 90)
            insert("INSERT INTO membership_plan (code, name, description, price_cents, duration_days, active) VALUES (?, ?, ?, ?, ?, TRUE)",
                "ANNUAL", "Annual", "A full year of unyielding might. Best value: two months free.", 49000, 365)

            // Trainers (fictional)
            val trainers = listOf(
                TrainerUpsertRequest("Rocky", "Malone", "Pro bodybuilder and MMA enthusiast. Lives for heavy compound lifts.", "Strength"),
                TrainerUpsertRequest("Dara", "Sanglap", "Former dancer turned coach who makes every session feel like a party.", "Dance & Mobility"),
                TrainerUpsertRequest("Sonny", "Taufa", "Ex-rugby pro and heavyweight boxer. High intensity only.", "Combat & Conditioning"),
                TrainerUpsertRequest("Chloe", "Tan", "Core strength wizard and bodyweight transformation expert.", "Core & Cardio"),
                TrainerUpsertRequest("Izzy", "Adeyemi", "Agility and kickboxing maestro from Auckland Central.", "Kickboxing"),
            ).map { insertTrainer(it) }

            // Classes for the current week (from Monday) through [weeks] weeks ahead
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            for (week in 0 until weeks) {
                for (c in schedule) {
                    val date = monday.plusWeeks(week.toLong()).with(TemporalAdjusters.nextOrSame(c.day))
                    val start = date.atTime(c.start).atZone(gymZone).toInstant()
                    val end = start.plusSeconds(c.minutes * 60)
                    val id = insertClass(
                        ClassUpsertRequest(c.name, c.description, c.category, trainers[c.trainer], start.toEpochMilli(), end.toEpochMilli(), c.capacity, c.calories, c.priceCents),
                        now,
                    )
                    if (end.isBefore(now)) update("UPDATE gym_class SET status = 'COMPLETED' WHERE id = ?", id)
                }
            }

            // Gym-issued membership cards (IDs start at 1000 like the original). 1001 is unlinked so the
            // "link your membership" registration path can be demonstrated with newmember@unyieldingmight.app.
            val addressA = insertAddress(AddressDto("789 Admin Way", "Papatoetoe", "Auckland", "2025", "New Zealand"))
            val addressB = insertAddress(AddressDto("100 West Street", "Pukekohe", "Auckland", "2120", "New Zealand"))
            val membershipSql = """
                INSERT INTO membership (id, email, date_of_birth, gender, height_cm, weight_kg, activity_level, address_id, plan_id, status, starts_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, 'ACTIVE', ?, NULL)
            """
            update(membershipSql, 1000, "member@unyieldingmight.app", LocalDate.of(2001, 11, 7), Gender.MALE, 175.0, 72.5, ActivityLevel.HEAVY, addressB, now)
            update(membershipSql, 1001, "newmember@unyieldingmight.app", LocalDate.of(2002, 3, 5), Gender.FEMALE, 160.0, 55.0, ActivityLevel.MODERATE, addressB, now)
            update(membershipSql, 1002, "casey@example.com", LocalDate.of(1984, 7, 12), Gender.MALE, 172.7, 69.1, ActivityLevel.MODERATE, null, now)

            // Accounts
            insertUser("admin@unyieldingmight.app", passwordHash, "Gym", "Admin", LocalDate.of(1995, 1, 1), Gender.OTHER, Role.ADMIN, addressA)
            val member = insertUser("member@unyieldingmight.app", passwordHash, "Morgan", "Member", LocalDate.of(2001, 11, 7), Gender.MALE, Role.CUSTOMER, addressB)
            insertCustomerProfile(member, 175.0, 72.5, ActivityLevel.HEAVY, 1000)
            val demo = insertUser("demo@unyieldingmight.app", passwordHash, "Dana", "Demo", LocalDate.of(1998, 6, 21), Gender.FEMALE, Role.CUSTOMER, insertAddress(AddressDto("12 Queen Street", "Auckland CBD", "Auckland", "1010", "New Zealand")))
            insertCustomerProfile(demo, 165.0, 62.0, ActivityLevel.LIGHT, null)

            // Shop
            listOf(
                ShopItemUpsertRequest("UM Performance Tee", "Lightweight, sweat-wicking training tee with the Unyielding Might logo.", ShopCategory.APPAREL, 3500, 40, null),
                ShopItemUpsertRequest("UM Training Hoodie", "Heavyweight brushed-fleece hoodie for cold early sessions.", ShopCategory.APPAREL, 7900, 20, null),
                ShopItemUpsertRequest("Compression Shorts", "Four-way stretch shorts with a phone pocket.", ShopCategory.APPAREL, 4500, 25, null),
                ShopItemUpsertRequest("Whey Protein 1kg", "Vanilla whey isolate, 27 g protein per serve.", ShopCategory.SUPPLEMENTS, 6900, 30, null),
                ShopItemUpsertRequest("Pre-Workout 300g", "Citrus pre-workout with caffeine and beta-alanine.", ShopCategory.SUPPLEMENTS, 4900, 15, null),
                ShopItemUpsertRequest("Creatine Monohydrate 500g", "Pure micronised creatine. Unflavoured.", ShopCategory.SUPPLEMENTS, 3900, 4, null),
                ShopItemUpsertRequest("Lifting Straps", "Padded cotton straps for heavy pulls.", ShopCategory.EQUIPMENT, 2500, 35, null),
                ShopItemUpsertRequest("Leather Lifting Belt", "10 mm single-prong belt for squats and deadlifts.", ShopCategory.EQUIPMENT, 8900, 10, null),
                ShopItemUpsertRequest("Boxing Gloves 14oz", "Synthetic leather gloves for bag work and pads.", ShopCategory.EQUIPMENT, 7500, 12, null),
                ShopItemUpsertRequest("Jump Rope", "Speed rope with ball-bearing handles.", ShopCategory.EQUIPMENT, 1900, 50, null),
                ShopItemUpsertRequest("UM Shaker Bottle", "700 ml leak-proof shaker with mixing ball.", ShopCategory.ACCESSORIES, 1500, 60, null),
                ShopItemUpsertRequest("Gym Towel", "Quick-dry microfibre towel.", ShopCategory.ACCESSORIES, 1200, 3, null),
            ).forEach { insertShopItem(it, now) }
        }
        log.info("Demo data ready. Log in with admin@unyieldingmight.app / {} (or member@ / demo@)", DEMO_PASSWORD)
    }
}
