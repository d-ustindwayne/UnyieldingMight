package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.instant
import java.time.Instant

enum class OtpPurpose { REGISTER, RESET_PASSWORD }

data class OtpRecord(val id: Int, val codeHash: String, val attempts: Int, val expiresAt: Instant)

/** Issuing a new code invalidates any earlier unused code for the same email and purpose. */
fun Tx.insertOtp(email: String, purpose: OtpPurpose, codeHash: String, expiresAt: Instant, now: Instant = Instant.now()): Int {
    update(
        "UPDATE email_otp SET consumed_at = ? WHERE email = ? AND purpose = ? AND consumed_at IS NULL",
        now, email.lowercase(), purpose,
    )
    return insert(
        "INSERT INTO email_otp (email, purpose, code_hash, attempts, expires_at, created_at) VALUES (?, ?, ?, 0, ?, ?)",
        email.lowercase(), purpose, codeHash, expiresAt, now,
    )
}

fun Tx.findActiveOtp(email: String, purpose: OtpPurpose): OtpRecord? = queryOne(
    """
    SELECT id, code_hash, attempts, expires_at FROM email_otp
    WHERE email = ? AND purpose = ? AND consumed_at IS NULL
    ORDER BY id DESC
    """,
    email.lowercase(), purpose,
) { OtpRecord(it.getInt("id"), it.getString("code_hash"), it.getInt("attempts"), it.instant("expires_at")) }

fun Tx.recordOtpAttempt(id: Int) = update("UPDATE email_otp SET attempts = attempts + 1 WHERE id = ?", id)

fun Tx.consumeOtp(id: Int, now: Instant = Instant.now()) = update("UPDATE email_otp SET consumed_at = ? WHERE id = ?", now, id)

fun Tx.recentOtpCount(email: String, since: Instant): Long =
    count("SELECT COUNT(*) FROM email_otp WHERE email = ? AND created_at >= ?", email.lowercase(), since)
