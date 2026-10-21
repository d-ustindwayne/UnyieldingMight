package com.unyieldingmight.server.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Password hashing. The original app stored unsalted SHA-256 hashes; passwords are now
 * hashed with bcrypt (salted, deliberately slow) so a leaked table can't be reversed with lookup tables.
 */
object Passwords {
    private const val COST = 12

    fun hash(password: String): String = BCrypt.withDefaults().hashToString(COST, password.toCharArray())

    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}

object Codes {
    private val random = SecureRandom()

    /** Six-digit one-time code, zero padded. */
    fun otp(): String = random.nextInt(1_000_000).toString().padStart(6, '0')

    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
