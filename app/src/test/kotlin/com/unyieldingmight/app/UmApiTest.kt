package com.unyieldingmight.app

import com.unyieldingmight.app.data.ApiException
import com.unyieldingmight.app.data.UmApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UmApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun api(token: String? = "t0ken", onUnauthorized: () -> Unit = {}, engine: MockEngine) =
        UmApi(tokenProvider = { token }, onUnauthorized = onUnauthorized, engine = engine, baseUrl = "http://test/")

    @Test
    fun `sends the bearer token and parses responses`() = runTest {
        var authHeader: String? = null
        val engine = MockEngine { request ->
            authHeader = request.headers[HttpHeaders.Authorization]
            assertEquals("/api/classes/7", request.url.encodedPath)
            respond(
                """{"id":7,"name":"HIIT","description":"","category":"CARDIO","startsAt":0,"endsAt":1,"maxCapacity":10,
                   "bookedCount":2,"avgCalories":650,"priceCents":1500,"status":"SCHEDULED","unknownField":true}""",
                HttpStatusCode.OK, json,
            )
        }
        val gymClass = api(engine = engine).gymClass(7)
        assertEquals("Bearer t0ken", authHeader)
        assertEquals(8, gymClass.spotsLeft)
        assertNull(gymClass.trainer)
    }

    @Test
    fun `public endpoints don't send the token`() = runTest {
        var authHeader: String? = "unset"
        val engine = MockEngine { request ->
            authHeader = request.headers[HttpHeaders.Authorization]
            respond("[]", HttpStatusCode.OK, json)
        }
        api(engine = engine).shopItems()
        assertNull(authHeader)
    }

    @Test
    fun `server errors become readable exceptions`() = runTest {
        val engine = MockEngine {
            respond("""{"code":"class_full","message":"Sorry, this class is full"}""", HttpStatusCode.Conflict, json)
        }
        val error = assertFailsWith<ApiException> { api(engine = engine).bookClass(1) }
        assertEquals("Sorry, this class is full", error.message)
        assertEquals("class_full", error.code)
        assertEquals(409, error.status)
    }

    @Test
    fun `an expired session triggers sign out`() = runTest {
        var signedOut = false
        val engine = MockEngine {
            respond("""{"code":"unauthorized","message":"Your session has expired."}""", HttpStatusCode.Unauthorized, json)
        }
        val error = assertFailsWith<ApiException> { api(onUnauthorized = { signedOut = true }, engine = engine).me() }
        assertTrue(error.isUnauthorized)
        assertTrue(signedOut)
    }

    @Test
    fun `a wrong password does not sign anyone out`() = runTest {
        var signedOut = false
        val engine = MockEngine {
            respond("""{"code":"invalid_credentials","message":"Incorrect email or password"}""", HttpStatusCode.Unauthorized, json)
        }
        assertFailsWith<ApiException> { api(token = null, onUnauthorized = { signedOut = true }, engine = engine).login("a@b.co", "x") }
        assertEquals(false, signedOut)
    }

    @Test
    fun `non-json failures still produce a message`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.BadGateway) }
        val error = assertFailsWith<ApiException> { api(engine = engine).news(com.unyieldingmight.shared.model.NewsTopic.GYM) }
        assertTrue(error.message.contains("502"))
    }
}
