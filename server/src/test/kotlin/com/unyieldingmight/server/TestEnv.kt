package com.unyieldingmight.server

import com.unyieldingmight.server.auth.JwtService
import com.unyieldingmight.server.config.AppConfig
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.db.DevSeeder
import com.unyieldingmight.server.db.Migrations
import com.unyieldingmight.server.email.LoggingEmailSender
import com.unyieldingmight.server.payment.MockGateway
import com.unyieldingmight.shared.model.AuthResponse
import com.unyieldingmight.shared.model.LoginRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.h2.jdbcx.JdbcDataSource
import java.util.UUID

/** A fresh in-memory database (H2 in MySQL mode) with the real schema and demo seed, plus fake payment/email providers. */
class TestEnv {
    val gateway = MockGateway()
    val email = LoggingEmailSender()
    val db = Database(JdbcDataSource().apply {
        setURL("jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
    })
    val services = Services(
        db = db,
        jwt = JwtService(AppConfig.JwtConfig("test-secret", "unyielding-might", "unyielding-might-app", 1)),
        currency = "nzd",
        gateway = gateway,
        emailSender = email,
        http = HttpClient(),
        newsApiKey = null,
        emailVerifier = null,
        logOtpCodes = false,
    )

    init {
        Migrations.migrate(db)
        DevSeeder.seedIfEmpty(db)
    }

    /** The 6-digit code from the most recent OTP email sent to [address]. */
    fun lastOtp(address: String): String =
        email.sent.last { it.to == address }.subject.takeLast(6)
}

fun apiTest(block: suspend ApplicationTestBuilder.(TestEnv, HttpClient) -> Unit) {
    val env = TestEnv()
    testApplication {
        application { module(env.services) }
        val client = createClient {
            install(ContentNegotiation) { json(Services.json) }
        }
        block(env, client)
    }
}

suspend fun HttpClient.login(email: String, password: String = DevSeeder.DEMO_PASSWORD): String =
    post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(email, password))
    }.body<AuthResponse>().token

suspend fun HttpClient.postJson(path: String, body: Any? = null, token: String? = null) =
    post(path) {
        contentType(ContentType.Application.Json)
        token?.let { bearerAuth(it) }
        if (body != null) setBody(body)
    }

const val ADMIN = "admin@unyieldingmight.app"
const val MEMBER = "member@unyieldingmight.app"
const val CUSTOMER = "demo@unyieldingmight.app"
