package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.enum
import com.unyieldingmight.server.db.epochMillis
import com.unyieldingmight.server.db.instantOrNull
import com.unyieldingmight.server.db.intOrNull
import com.unyieldingmight.server.db.localDate
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.MembershipDto
import com.unyieldingmight.shared.model.MembershipStatus
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.UserDto
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset

data class UserRecord(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val role: Role,
    val address: AddressDto?,
    val addressId: Int?,
    val newsletterOptIn: Boolean,
    val createdAt: Long,
    val heightCm: Double?,
    val weightKg: Double?,
    val activityLevel: ActivityLevel?,
    val membership: MembershipDto?,
) {
    val fullName: String get() = "$firstName $lastName"

    fun age(today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Int = Period.between(dateOfBirth, today).years

    fun toDto(): UserDto = UserDto(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = role,
        dateOfBirth = dateOfBirth.toString(),
        gender = gender,
        address = address,
        newsletterOptIn = newsletterOptIn,
        stats = if (heightCm != null && weightKg != null && activityLevel != null) {
            Fitness.bodyStats(heightCm, weightKg, activityLevel, age(), gender)
        } else null,
        membership = membership,
        createdAt = createdAt,
    )
}

private const val USER_SELECT = """
    SELECT u.*, a.street, a.city, a.region, a.postcode, a.country,
           cp.height_cm, cp.weight_kg, cp.activity_level, cp.membership_id,
           m.status AS m_status, m.starts_at AS m_starts_at, m.expires_at AS m_expires_at, mp.name AS m_plan_name
    FROM user_account u
    LEFT JOIN address a ON a.id = u.address_id
    LEFT JOIN customer_profile cp ON cp.user_id = u.id
    LEFT JOIN membership m ON m.id = cp.membership_id
    LEFT JOIN membership_plan mp ON mp.id = m.plan_id
"""

private fun ResultSet.toUserRecord(): UserRecord {
    val addressId = intOrNull("address_id")
    val membershipId = intOrNull("membership_id")
    return UserRecord(
        id = getInt("id"),
        email = getString("email"),
        passwordHash = getString("password_hash"),
        firstName = getString("first_name"),
        lastName = getString("last_name"),
        dateOfBirth = localDate("date_of_birth"),
        gender = enum("gender"),
        role = enum("role"),
        addressId = addressId,
        address = addressId?.let {
            AddressDto(getString("street"), getString("city"), getString("region"), getString("postcode"), getString("country"))
        },
        newsletterOptIn = getBoolean("newsletter_opt_in"),
        createdAt = epochMillis("created_at"),
        heightCm = getDouble("height_cm").takeUnless { wasNull() },
        weightKg = getDouble("weight_kg").takeUnless { wasNull() },
        activityLevel = getString("activity_level")?.let { ActivityLevel.valueOf(it) },
        membership = membershipId?.let {
            MembershipDto(
                id = it,
                planName = getString("m_plan_name"),
                status = effectiveMembershipStatus(enum("m_status"), instantOrNull("m_expires_at")),
                startsAt = instantOrNull("m_starts_at")!!.toEpochMilli(),
                expiresAt = instantOrNull("m_expires_at")?.toEpochMilli(),
            )
        },
    )
}

/** A membership past its expiry date is reported as EXPIRED even before a job updates the row. */
fun effectiveMembershipStatus(stored: MembershipStatus, expiresAt: Instant?, now: Instant = Instant.now()): MembershipStatus =
    if (stored == MembershipStatus.ACTIVE && expiresAt != null && expiresAt.isBefore(now)) MembershipStatus.EXPIRED else stored

fun Tx.findUserById(id: Int): UserRecord? =
    queryOne("$USER_SELECT WHERE u.id = ?", id) { it.toUserRecord() }

fun Tx.findUserByEmail(email: String): UserRecord? =
    queryOne("$USER_SELECT WHERE LOWER(u.email) = LOWER(?)", email.trim()) { it.toUserRecord() }

fun Tx.emailExists(email: String): Boolean =
    count("SELECT COUNT(*) FROM user_account WHERE LOWER(email) = LOWER(?)", email.trim()) > 0

fun Tx.insertAddress(address: AddressDto): Int = insert(
    "INSERT INTO address (street, city, region, postcode, country) VALUES (?, ?, ?, ?, ?)",
    address.street.trim(), address.city.trim(), address.region.trim(), address.postcode.trim(), address.country.trim(),
)

fun Tx.updateAddress(id: Int, address: AddressDto) = update(
    "UPDATE address SET street = ?, city = ?, region = ?, postcode = ?, country = ? WHERE id = ?",
    address.street.trim(), address.city.trim(), address.region.trim(), address.postcode.trim(), address.country.trim(), id,
)

fun Tx.insertUser(
    email: String,
    passwordHash: String,
    firstName: String,
    lastName: String,
    dateOfBirth: LocalDate,
    gender: Gender,
    role: Role,
    addressId: Int?,
    now: Instant = Instant.now(),
): Int = insert(
    """
    INSERT INTO user_account (email, password_hash, first_name, last_name, date_of_birth, gender, role, address_id, newsletter_opt_in, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """,
    email.trim().lowercase(), passwordHash, firstName.trim(), lastName.trim(), dateOfBirth, gender, role, addressId, true, now, now,
)

fun Tx.insertCustomerProfile(userId: Int, heightCm: Double, weightKg: Double, activityLevel: ActivityLevel, membershipId: Int?) = update(
    "INSERT INTO customer_profile (user_id, membership_id, height_cm, weight_kg, activity_level) VALUES (?, ?, ?, ?, ?)",
    userId, membershipId, heightCm, weightKg, activityLevel,
)

fun Tx.updateUserNames(id: Int, firstName: String, lastName: String, addressId: Int?, newsletterOptIn: Boolean) = update(
    "UPDATE user_account SET first_name = ?, last_name = ?, address_id = ?, newsletter_opt_in = ?, updated_at = ? WHERE id = ?",
    firstName.trim(), lastName.trim(), addressId, newsletterOptIn, Instant.now(), id,
)

fun Tx.updateCustomerStats(userId: Int, heightCm: Double, weightKg: Double, activityLevel: ActivityLevel) = update(
    "UPDATE customer_profile SET height_cm = ?, weight_kg = ?, activity_level = ? WHERE user_id = ?",
    heightCm, weightKg, activityLevel, userId,
)

fun Tx.updatePassword(userId: Int, passwordHash: String) = update(
    "UPDATE user_account SET password_hash = ?, updated_at = ? WHERE id = ?", passwordHash, Instant.now(), userId,
)

fun Tx.updateRole(userId: Int, role: Role) = update(
    "UPDATE user_account SET role = ?, updated_at = ? WHERE id = ?", role, Instant.now(), userId,
)

fun Tx.findRole(userId: Int): Role? =
    queryOne("SELECT role FROM user_account WHERE id = ?", userId) { it.enum<Role>("role") }

fun Tx.setCustomerMembership(userId: Int, membershipId: Int) = update(
    "UPDATE customer_profile SET membership_id = ? WHERE user_id = ?", membershipId, userId,
)

fun Tx.listUsers(search: String?): List<AdminUserDto> {
    val like = "%${search?.trim().orEmpty()}%"
    return query(
        """
        SELECT u.id, u.email, u.first_name, u.last_name, u.role, u.created_at,
               (m.status = 'ACTIVE' AND (m.expires_at IS NULL OR m.expires_at > ?)) AS is_member
        FROM user_account u
        LEFT JOIN customer_profile cp ON cp.user_id = u.id
        LEFT JOIN membership m ON m.id = cp.membership_id
        WHERE u.email LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?
        ORDER BY u.created_at DESC
        """,
        Instant.now(), like, like, like,
    ) {
        AdminUserDto(
            id = it.getInt("id"),
            email = it.getString("email"),
            firstName = it.getString("first_name"),
            lastName = it.getString("last_name"),
            role = it.enum("role"),
            isMember = it.getBoolean("is_member"),
            createdAt = it.epochMillis("created_at"),
        )
    }
}

/** Opted-in customers to receive a newsletter: (email, first name). */
fun Tx.newsletterRecipients(): List<Pair<String, String>> =
    query("SELECT email, first_name FROM user_account WHERE newsletter_opt_in = TRUE") { it.getString(1) to it.getString(2) }
