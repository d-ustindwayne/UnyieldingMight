package com.unyieldingmight.server

import com.unyieldingmight.server.config.AppConfig
import com.unyieldingmight.server.db.DevSeeder
import com.unyieldingmight.server.db.Migrations
import com.unyieldingmight.server.repo.expireMemberships
import com.unyieldingmight.server.routes.adminRoutes
import com.unyieldingmight.server.routes.authRoutes
import com.unyieldingmight.server.routes.classRoutes
import com.unyieldingmight.server.routes.meRoutes
import com.unyieldingmight.server.routes.publicRoutes
import com.unyieldingmight.server.routes.shopRoutes
import com.unyieldingmight.shared.model.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.minutes

private val log = LoggerFactory.getLogger("com.unyieldingmight.server.Application")

fun main() {
    val config = AppConfig.load()
    val services = Services.fromConfig(config)
    Migrations.migrate(services.db)
    if (config.seedDemoData) DevSeeder.seedIfEmpty(services.db)

    log.info(
        "Payments: {} | Email: {} | News: {}",
        services.payments.mode,
        if (config.smtp != null) "SMTP" else "logged only",
        if (config.newsApiKey != null) "NewsAPI" else "disabled",
    )
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(services)
        startBackgroundJobs(services)
    }.start(wait = true)
}

const val AUTH_JWT = "jwt"
val AUTH_RATE_LIMIT = RateLimitName("auth")

fun Application.module(services: Services) {
    install(ContentNegotiation) { json(Services.json) }

    install(CallLogging) {
        level = Level.INFO
        filter { it.request.path().startsWith("/api") }
    }

    install(StatusPages) {
        exception<ApiException> { call, e -> call.respond(e.status, ApiError(e.code, e.message)) }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("bad_request", "Some of the details sent were invalid"))
        }
        exception<Throwable> { call, e ->
            log.error("Unhandled error on {}", call.request.path(), e)
            call.respond(HttpStatusCode.InternalServerError, ApiError("server_error", "Something went wrong on our side. Please try again."))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ApiError("not_found", "Not found"))
        }
    }

    install(Authentication) {
        jwt(AUTH_JWT) {
            realm = services.jwt.realm
            verifier(services.jwt.verifier)
            validate { credential -> JWTPrincipal(credential.payload) }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiError("unauthorized", "Your session has expired. Please log in again."))
            }
        }
    }

    // Slows down password guessing and OTP spamming per client address.
    install(RateLimit) {
        register(AUTH_RATE_LIMIT) {
            rateLimiter(limit = 20, refillPeriod = 1.minutes)
            requestKey { it.request.origin.remoteHost }
        }
    }

    routing {
        route("/api") {
            publicRoutes(services)
            authRoutes(services)
            classRoutes(services)
            shopRoutes(services)
            meRoutes(services)
            adminRoutes(services)
        }
    }
}

/** Every 5 minutes: complete finished classes (emailing attendees) and expire lapsed memberships. */
fun Application.startBackgroundJobs(services: Services) {
    val job = launch {
        while (true) {
            runCatching {
                services.classes.completeFinishedClasses()
                services.db.tx { expireMemberships() }
            }.onFailure { log.warn("Background job failed: {}", it.message) }
            delay(5.minutes)
        }
    }
    monitor.subscribe(ApplicationStopping) { job.cancel() }
}
