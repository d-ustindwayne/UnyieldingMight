package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.auth.Codes
import com.unyieldingmight.server.auth.JwtService
import com.unyieldingmight.server.auth.Passwords
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.email.Email
import com.unyieldingmight.server.email.EmailSender
import com.unyieldingmight.server.email.EmailTemplates
import com.unyieldingmight.server.repo.MembershipRecord
import com.unyieldingmight.server.repo.OtpPurpose
import com.unyieldingmight.server.repo.consumeOtp
import com.unyieldingmight.server.repo.emailExists
import com.unyieldingmight.server.repo.findActiveOtp
import com.unyieldingmight.server.repo.findMembership
import com.unyieldingmight.server.repo.findUserByEmail
import com.unyieldingmight.server.repo.findUserById
import com.unyieldingmight.server.repo.insertAddress
import com.unyieldingmight.server.repo.insertCustomerProfile
import com.unyieldingmight.server.repo.insertOtp
import com.unyieldingmight.server.repo.insertUser
import com.unyieldingmight.server.repo.recentOtpCount
import com.unyieldingmight.server.repo.recordOtpAttempt
import com.unyieldingmight.server.repo.updatePassword
import com.unyieldingmight.server.validate
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.AuthResponse
import com.unyieldingmight.shared.model.MembershipPreview
import com.unyieldingmight.shared.model.MembershipStatus
import com.unyieldingmight.shared.model.RegisterRequest
import com.unyieldingmight.shared.model.RegistrationTicket
import com.unyieldingmight.shared.model.Role
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

class AuthService(
    private val db: Database,
    private val jwt: JwtService,
    private val emailSender: EmailSender,
    private val emailVerifier: EmailVerifier?,
    private val logOtpCodes: Boolean,
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun login(email: String, password: String): AuthResponse {
        val user = db.tx { findUserByEmail(email) }
        // Same message for unknown email and wrong password so accounts can't be enumerated.
        if (user == null || !Passwords.verify(password, user.passwordHash)) {
            throw ApiException.unauthorized("Incorrect email or password", "invalid_credentials")
        }
        return AuthResponse(jwt.sessionToken(user.id, user.role), user.toDto())
    }

    // ---------- Registration ----------

    suspend fun startRegistration(email: String) {
        validate(Validation.email(email))
        if (db.tx { emailExists(email) }) {
            throw ApiException.conflict("An account with this email already exists", "email_taken")
        }
        emailVerifier?.let { verifier ->
            if (verifier.isUndeliverable(email)) {
                throw ApiException.badRequest("This email address doesn't look deliverable. Please check it.", "email_undeliverable")
            }
        }
        sendOtp(email, OtpPurpose.REGISTER, "finish creating your account")
    }

    suspend fun verifyRegistration(email: String, code: String): RegistrationTicket {
        checkOtp(email, code, OtpPurpose.REGISTER)
        return RegistrationTicket(jwt.registrationToken(email.trim().lowercase()), email.trim().lowercase())
    }

    /** Looks up a gym-issued membership so the app can prefill registration (the original "Link your membership" step). */
    suspend fun lookupMembership(registrationToken: String, membershipId: Int): MembershipPreview {
        val email = verifiedEmail(registrationToken)
        return db.tx { requireLinkableMembership(membershipId, email) }.preview
    }

    suspend fun register(req: RegisterRequest): AuthResponse {
        val email = verifiedEmail(req.registrationToken)
        validate(
            Validation.password(req.password),
            Validation.name(req.firstName, "First name"),
            Validation.name(req.lastName, "Last name"),
            Validation.heightCm(req.heightCm),
            Validation.weightKg(req.weightKg),
            validateAddress(req.address),
        )
        val dateOfBirth = parseDateOfBirth(req.dateOfBirth)
        val passwordHash = Passwords.hash(req.password)

        val userId = db.tx {
            if (emailExists(email)) throw ApiException.conflict("An account with this email already exists", "email_taken")
            req.membershipId?.let { requireLinkableMembership(it, email) }
            val addressId = insertAddress(req.address)
            val id = insertUser(email, passwordHash, req.firstName, req.lastName, dateOfBirth, req.gender, Role.CUSTOMER, addressId)
            insertCustomerProfile(id, req.heightCm, req.weightKg, req.activityLevel, req.membershipId)
            id
        }
        val user = db.tx { findUserById(userId) }!!
        return AuthResponse(jwt.sessionToken(user.id, user.role), user.toDto())
    }

    // ---------- Passwords ----------

    suspend fun forgotPassword(email: String) {
        validate(Validation.email(email))
        // Always respond the same way; only send a code if the account exists.
        if (db.tx { emailExists(email) }) sendOtp(email, OtpPurpose.RESET_PASSWORD, "reset your password")
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String) {
        validate(Validation.password(newPassword))
        checkOtp(email, code, OtpPurpose.RESET_PASSWORD)
        val hash = Passwords.hash(newPassword)
        db.tx {
            val user = findUserByEmail(email) ?: throw ApiException.notFound("Account not found")
            updatePassword(user.id, hash)
        }
    }

    suspend fun changePassword(userId: Int, current: String, newPassword: String) {
        validate(Validation.password(newPassword))
        val user = db.tx { findUserById(userId) } ?: throw ApiException.unauthorized()
        if (!Passwords.verify(current, user.passwordHash)) {
            throw ApiException.badRequest("Your current password is incorrect", "wrong_password")
        }
        val hash = Passwords.hash(newPassword)
        db.tx { updatePassword(userId, hash) }
    }

    // ---------- Helpers ----------

    private fun verifiedEmail(token: String): String =
        jwt.verifyRegistrationToken(token)
            ?: throw ApiException.unauthorized("Your verification has expired. Please verify your email again.", "registration_expired")

    private suspend fun sendOtp(email: String, purpose: OtpPurpose, purposeText: String) {
        val now = Instant.now()
        val code = Codes.otp()
        db.tx {
            if (recentOtpCount(email, now.minus(Duration.ofHours(1))) >= MAX_OTPS_PER_HOUR) {
                throw ApiException(HttpStatusCode.TooManyRequests, "too_many_codes", "Too many codes requested. Please try again later.")
            }
            insertOtp(email, purpose, otpHash(email, code), now.plus(OTP_TTL))
        }
        val (subject, body) = EmailTemplates.otp(code, purposeText)
        emailSender.send(Email(email, email, subject, body))
        if (logOtpCodes) log.info("[dev] {} code for {}: {}", purpose, email, code)
    }

    private suspend fun checkOtp(email: String, code: String, purpose: OtpPurpose) {
        validate(Validation.otp(code))
        db.tx {
            val otp = findActiveOtp(email, purpose)
                ?: throw ApiException.badRequest("No active code. Please request a new one.", "otp_missing")
            when {
                otp.expiresAt.isBefore(Instant.now()) ->
                    throw ApiException.badRequest("This code has expired. Please request a new one.", "otp_expired")
                otp.attempts >= MAX_OTP_ATTEMPTS ->
                    throw ApiException.badRequest("Too many wrong attempts. Please request a new code.", "otp_locked")
                otp.codeHash != otpHash(email, code) -> {
                    recordOtpAttempt(otp.id)
                    ApiException.badRequest("That code is incorrect", "otp_incorrect")
                }
                else -> {
                    consumeOtp(otp.id)
                    null
                }
            }
        }?.let { throw it } // thrown after commit so the failed attempt is recorded
    }

    private fun otpHash(email: String, code: String) = Codes.sha256("${email.trim().lowercase()}:$code")

    companion object {
        val OTP_TTL: Duration = Duration.ofMinutes(10)
        const val MAX_OTP_ATTEMPTS = 5
        const val MAX_OTPS_PER_HOUR = 5

        fun validateAddress(address: AddressDto): String? =
            Validation.required(address.street, "Street") ?: Validation.required(address.city, "City", 50)
                ?: Validation.required(address.region, "Region", 50) ?: Validation.postcode(address.postcode)
                ?: Validation.required(address.country, "Country", 50)

        fun parseDateOfBirth(value: String): LocalDate {
            val date = try {
                LocalDate.parse(value)
            } catch (_: DateTimeParseException) {
                throw ApiException.badRequest("Enter a valid date of birth", "validation")
            }
            val age = java.time.Period.between(date, LocalDate.now()).years
            if (age !in 13..120) throw ApiException.badRequest("You must be at least 13 years old to register", "validation")
            return date
        }
    }
}

/** Membership checks carried over from the original MembershipResult statuses (NOT_FOUND, EMAIL_MISMATCH, ALREADY_LINKED). */
internal fun Tx.requireLinkableMembership(membershipId: Int, email: String): MembershipRecord {
    val membership = findMembership(membershipId) ?: throw ApiException.notFound("We couldn't find that membership ID")
    if (!membership.preview.email.equals(email.trim(), ignoreCase = true)) {
        throw ApiException.badRequest("This membership is registered to a different email address", "email_mismatch")
    }
    if (membership.linkedUserId != null) {
        throw ApiException.conflict("This membership is already linked to an account", "already_linked")
    }
    if (membership.status != MembershipStatus.ACTIVE) {
        throw ApiException.badRequest("This membership is no longer active", "membership_inactive")
    }
    return membership
}
