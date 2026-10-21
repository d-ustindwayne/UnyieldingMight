package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.enum
import com.unyieldingmight.server.db.instant
import com.unyieldingmight.server.db.instantOrNull
import com.unyieldingmight.server.db.intOrNull
import com.unyieldingmight.server.db.localDate
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.MembershipDto
import com.unyieldingmight.shared.model.MembershipPlanDto
import com.unyieldingmight.shared.model.MembershipPreview
import com.unyieldingmight.shared.model.MembershipStatus
import java.time.Instant
import java.time.LocalDate

data class MembershipRecord(
    val preview: MembershipPreview,
    val status: MembershipStatus,
    val linkedUserId: Int?,
)

fun Tx.findMembership(id: Int): MembershipRecord? = queryOne(
    """
    SELECT m.*, a.street, a.city, a.region, a.postcode, a.country, cp.user_id AS linked_user_id
    FROM membership m
    LEFT JOIN address a ON a.id = m.address_id
    LEFT JOIN customer_profile cp ON cp.membership_id = m.id
    WHERE m.id = ?
    """,
    id,
) { rs ->
    val addressId = rs.intOrNull("address_id")
    MembershipRecord(
        preview = MembershipPreview(
            membershipId = rs.getInt("id"),
            email = rs.getString("email"),
            dateOfBirth = rs.localDate("date_of_birth").toString(),
            gender = rs.enum("gender"),
            heightCm = rs.getDouble("height_cm"),
            weightKg = rs.getDouble("weight_kg"),
            activityLevel = rs.enum("activity_level"),
            address = addressId?.let {
                AddressDto(rs.getString("street"), rs.getString("city"), rs.getString("region"), rs.getString("postcode"), rs.getString("country"))
            },
        ),
        status = effectiveMembershipStatus(rs.enum("status"), rs.instantOrNull("expires_at")),
        linkedUserId = rs.intOrNull("linked_user_id"),
    )
}

fun Tx.insertMembership(
    email: String,
    dateOfBirth: LocalDate,
    gender: Gender,
    heightCm: Double,
    weightKg: Double,
    activityLevel: ActivityLevel,
    addressId: Int?,
    planId: Int?,
    startsAt: Instant,
    expiresAt: Instant?,
): Int = insert(
    """
    INSERT INTO membership (email, date_of_birth, gender, height_cm, weight_kg, activity_level, address_id, plan_id, status, starts_at, expires_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
    """,
    email.lowercase(), dateOfBirth, gender, heightCm, weightKg, activityLevel, addressId, planId, startsAt, expiresAt,
)

fun Tx.findMembershipDto(id: Int): MembershipDto? = queryOne(
    "SELECT m.*, mp.name AS plan_name FROM membership m LEFT JOIN membership_plan mp ON mp.id = m.plan_id WHERE m.id = ?",
    id,
) {
    MembershipDto(
        id = it.getInt("id"),
        planName = it.getString("plan_name"),
        status = effectiveMembershipStatus(it.enum("status"), it.instantOrNull("expires_at")),
        startsAt = it.instant("starts_at").toEpochMilli(),
        expiresAt = it.instantOrNull("expires_at")?.toEpochMilli(),
    )
}

/** Extends an existing membership (renewal) from whichever is later: now or its current expiry. */
fun Tx.renewMembership(id: Int, planId: Int, durationDays: Int, now: Instant = Instant.now()) {
    val currentExpiry = queryOne("SELECT expires_at FROM membership WHERE id = ?", id) { it.instantOrNull("expires_at") }
    val from = currentExpiry?.takeIf { it.isAfter(now) } ?: now
    update(
        "UPDATE membership SET plan_id = ?, status = 'ACTIVE', expires_at = ? WHERE id = ?",
        planId, from.plusSeconds(durationDays * 86_400L), id,
    )
}

fun Tx.expireMemberships(now: Instant = Instant.now()): Int =
    update("UPDATE membership SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at IS NOT NULL AND expires_at < ?", now)

fun Tx.listMembershipPlans(): List<MembershipPlanDto> =
    query("SELECT * FROM membership_plan WHERE active = TRUE ORDER BY price_cents") {
        MembershipPlanDto(
            id = it.getInt("id"),
            code = it.getString("code"),
            name = it.getString("name"),
            description = it.getString("description"),
            priceCents = it.getInt("price_cents"),
            durationDays = it.getInt("duration_days"),
        )
    }

fun Tx.findMembershipPlan(id: Int): MembershipPlanDto? = listMembershipPlans().firstOrNull { it.id == id }
