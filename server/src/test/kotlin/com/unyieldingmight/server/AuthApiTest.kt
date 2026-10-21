package com.unyieldingmight.server

import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.ApiError
import com.unyieldingmight.shared.model.AuthResponse
import com.unyieldingmight.shared.model.EmailRequest
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.LoginRequest
import com.unyieldingmight.shared.model.MembershipLookupRequest
import com.unyieldingmight.shared.model.MembershipPreview
import com.unyieldingmight.shared.model.RegisterRequest
import com.unyieldingmight.shared.model.RegistrationTicket
import com.unyieldingmight.shared.model.ResetPasswordRequest
import com.unyieldingmight.shared.model.UserDto
import com.unyieldingmight.shared.model.VerifyOtpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthApiTest {
    private val address = AddressDto("1 Test St", "Auckland", "Auckland", "1010", "New Zealand")

    private suspend fun HttpClient.ticketFor(env: TestEnv, email: String): RegistrationTicket {
        assertEquals(HttpStatusCode.OK, postJson("/api/auth/register/start", EmailRequest(email)).status)
        return postJson("/api/auth/register/verify", VerifyOtpRequest(email, env.lastOtp(email))).body()
    }

    private fun registration(ticket: RegistrationTicket, membershipId: Int? = null) = RegisterRequest(
        registrationToken = ticket.registrationToken,
        password = "secret123",
        firstName = "Test",
        lastName = "User",
        dateOfBirth = "2000-01-01",
        gender = Gender.MALE,
        address = address,
        heightCm = 180.0,
        weightKg = 80.0,
        activityLevel = ActivityLevel.MODERATE,
        membershipId = membershipId,
    )

    @Test
    fun `full registration with email verification then login`() = apiTest { env, client ->
        val ticket = client.ticketFor(env, "new@example.com")
        val response = client.postJson("/api/auth/register", registration(ticket))
        assertEquals(HttpStatusCode.Created, response.status)
        val auth = response.body<AuthResponse>()
        assertEquals("new@example.com", auth.user.email)
        assertEquals(false, auth.user.isMember)
        assertTrue(auth.user.stats!!.tdee > 0)

        val token = client.login("new@example.com", "secret123")
        val me = client.get("/api/me") { bearerAuth(token) }.body<UserDto>()
        assertEquals("Test User", me.fullName)
    }

    @Test
    fun `registering links a gym-issued membership`() = apiTest { env, client ->
        val email = "newmember@unyieldingmight.app"
        val ticket = client.ticketFor(env, email)
        val preview = client.postJson("/api/auth/register/membership", MembershipLookupRequest(ticket.registrationToken, 1001)).body<MembershipPreview>()
        assertEquals(Gender.FEMALE, preview.gender)

        val auth = client.postJson("/api/auth/register", registration(ticket, membershipId = 1001)).body<AuthResponse>()
        assertTrue(auth.user.isMember)
    }

    @Test
    fun `membership lookup rejects another person's card and linked cards`() = apiTest { env, client ->
        val ticket = client.ticketFor(env, "someone@example.com")
        val mismatch = client.postJson("/api/auth/register/membership", MembershipLookupRequest(ticket.registrationToken, 1001))
        assertEquals("email_mismatch", mismatch.body<ApiError>().code)
        val missing = client.postJson("/api/auth/register/membership", MembershipLookupRequest(ticket.registrationToken, 9999))
        assertEquals(HttpStatusCode.NotFound, missing.status)
    }

    @Test
    fun `wrong otp is rejected and locks after too many attempts`() = apiTest { env, client ->
        client.postJson("/api/auth/register/start", EmailRequest("x@example.com"))
        val wrong = if (env.lastOtp("x@example.com") == "000000") "111111" else "000000"
        repeat(5) {
            val r = client.postJson("/api/auth/register/verify", VerifyOtpRequest("x@example.com", wrong))
            assertEquals("otp_incorrect", r.body<ApiError>().code)
        }
        val locked = client.postJson("/api/auth/register/verify", VerifyOtpRequest("x@example.com", env.lastOtp("x@example.com")))
        assertEquals("otp_locked", locked.body<ApiError>().code)
    }

    @Test
    fun `existing email cannot register again`() = apiTest { _, client ->
        val r = client.postJson("/api/auth/register/start", EmailRequest(CUSTOMER))
        assertEquals(HttpStatusCode.Conflict, r.status)
    }

    @Test
    fun `registration token is required`() = apiTest { _, client ->
        val r = client.postJson("/api/auth/register", registration(RegistrationTicket("forged", "a@b.co")))
        assertEquals(HttpStatusCode.Unauthorized, r.status)
    }

    @Test
    fun `wrong password gives a generic error`() = apiTest { _, client ->
        val r = client.postJson("/api/auth/login", LoginRequest(CUSTOMER, "nope"))
        assertEquals(HttpStatusCode.Unauthorized, r.status)
        assertEquals("Incorrect email or password", r.body<ApiError>().message)
    }

    @Test
    fun `password reset with emailed code`() = apiTest { env, client ->
        client.postJson("/api/auth/password/forgot", EmailRequest(CUSTOMER))
        val r = client.postJson("/api/auth/password/reset", ResetPasswordRequest(CUSTOMER, env.lastOtp(CUSTOMER), "brandnew99"))
        assertEquals(HttpStatusCode.OK, r.status)
        client.login(CUSTOMER, "brandnew99")
    }

    @Test
    fun `protected routes need a token`() = apiTest { _, client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/me").status)
    }
}
