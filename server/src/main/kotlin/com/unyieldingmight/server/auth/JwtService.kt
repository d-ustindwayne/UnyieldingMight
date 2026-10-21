package com.unyieldingmight.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.unyieldingmight.server.config.AppConfig
import com.unyieldingmight.shared.model.Role
import java.time.Duration
import java.time.Instant
import java.util.Date

/**
 * Issues two kinds of token:
 *  - session tokens (subject = user id, carries the role) for the app's API calls
 *  - short-lived registration tokens proving an email address passed OTP verification
 */
class JwtService(private val config: AppConfig.JwtConfig) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withClaim(CLAIM_TYPE, TYPE_SESSION)
        .build()

    private val registrationVerifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withClaim(CLAIM_TYPE, TYPE_REGISTRATION)
        .build()

    val realm = "unyielding-might"

    fun sessionToken(userId: Int, role: Role, now: Instant = Instant.now()): String = JWT.create()
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withSubject(userId.toString())
        .withClaim(CLAIM_TYPE, TYPE_SESSION)
        .withClaim(CLAIM_ROLE, role.name)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plus(Duration.ofHours(config.ttlHours))))
        .sign(algorithm)

    fun registrationToken(email: String, now: Instant = Instant.now()): String = JWT.create()
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withSubject(email)
        .withClaim(CLAIM_TYPE, TYPE_REGISTRATION)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plus(Duration.ofMinutes(30))))
        .sign(algorithm)

    /** Returns the verified email, or null if the token is invalid or expired. */
    fun verifyRegistrationToken(token: String): String? =
        runCatching { registrationVerifier.verify(token).subject }.getOrNull()

    companion object {
        const val CLAIM_TYPE = "typ"
        const val CLAIM_ROLE = "role"
        const val TYPE_SESSION = "session"
        const val TYPE_REGISTRATION = "registration"
    }
}
