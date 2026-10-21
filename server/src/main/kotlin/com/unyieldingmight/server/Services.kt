package com.unyieldingmight.server

import com.unyieldingmight.server.auth.JwtService
import com.unyieldingmight.server.config.AppConfig
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.email.EmailObserver
import com.unyieldingmight.server.email.EmailSender
import com.unyieldingmight.server.email.LoggingEmailSender
import com.unyieldingmight.server.email.NotificationCenter
import com.unyieldingmight.server.email.SmtpEmailSender
import com.unyieldingmight.server.payment.MockGateway
import com.unyieldingmight.server.payment.PaymentGateway
import com.unyieldingmight.server.payment.StripeGateway
import com.unyieldingmight.server.service.AdminService
import com.unyieldingmight.server.service.AuthService
import com.unyieldingmight.server.service.ClassService
import com.unyieldingmight.server.service.EmailVerifier
import com.unyieldingmight.server.service.NewsService
import com.unyieldingmight.server.service.PaymentService
import com.unyieldingmight.server.service.ShopService
import com.unyieldingmight.server.service.UserService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Manual dependency wiring; tests build this with an in-memory database and fake gateways. */
class Services(
    val db: Database,
    val jwt: JwtService,
    val currency: String,
    gateway: PaymentGateway,
    emailSender: EmailSender,
    http: HttpClient,
    newsApiKey: String?,
    emailVerifier: EmailVerifier?,
    logOtpCodes: Boolean,
) {
    val notifications = NotificationCenter().apply { subscribe(EmailObserver(emailSender)) }
    val payments = PaymentService(db, gateway, currency)
    val auth = AuthService(db, jwt, emailSender, emailVerifier, logOtpCodes)
    val users = UserService(db, payments)
    val classes = ClassService(db, payments, notifications)
    val shop = ShopService(db, payments, notifications)
    val news = NewsService(http, newsApiKey)
    val admin = AdminService(db, notifications)

    init {
        payments.register(classes)
        payments.register(shop)
        payments.register(users)
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }

        fun httpClient() = HttpClient(CIO) {
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) { requestTimeoutMillis = 15_000 }
            expectSuccess = false
        }

        fun fromConfig(config: AppConfig): Services {
            val http = httpClient()
            return Services(
                db = Database.pooled(config.db.url, config.db.user, config.db.password, config.db.maxPoolSize),
                jwt = JwtService(config.jwt),
                currency = config.currency,
                gateway = config.stripe?.let { StripeGateway(it) } ?: MockGateway(),
                emailSender = config.smtp?.let { SmtpEmailSender(it) } ?: LoggingEmailSender(),
                http = http,
                newsApiKey = config.newsApiKey,
                emailVerifier = config.emailVerificationApiKey?.let { EmailVerifier(http, it) },
                logOtpCodes = config.logOtpCodes,
            )
        }
    }
}
