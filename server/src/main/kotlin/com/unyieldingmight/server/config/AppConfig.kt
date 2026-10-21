package com.unyieldingmight.server.config

import java.io.File
import java.util.Properties

/**
 * Server configuration. Values come from environment variables first, then from the
 * repository's git-ignored `local.properties`, so local development needs no extra setup
 * and production (e.g. Aiven MySQL) can be configured purely through the environment.
 */
data class AppConfig(
    val port: Int,
    val db: DbConfig,
    val jwt: JwtConfig,
    val smtp: SmtpConfig?,
    val stripe: StripeConfig?,
    val newsApiKey: String?,
    val emailVerificationApiKey: String?,
    val currency: String,
    val seedDemoData: Boolean,
    /** When true, one-time codes are logged so registration works without SMTP (never enable in production). */
    val logOtpCodes: Boolean,
) {
    data class DbConfig(val url: String, val user: String, val password: String, val maxPoolSize: Int = 10)

    data class JwtConfig(val secret: String, val issuer: String, val audience: String, val ttlHours: Long)

    data class SmtpConfig(val host: String, val port: Int, val user: String, val password: String, val fromName: String)

    data class StripeConfig(val secretKey: String, val publishableKey: String)

    companion object {
        fun load(env: Map<String, String> = System.getenv(), propertiesFile: File = locateLocalProperties()): AppConfig {
            val props = Properties().apply {
                if (propertiesFile.exists()) propertiesFile.inputStream().use { load(it) }
            }
            fun get(key: String): String? = env[key]?.takeIf { it.isNotBlank() } ?: props.getProperty(key)?.takeIf { it.isNotBlank() }
            fun require(key: String): String = get(key) ?: error("Missing required configuration '$key' (set it as an environment variable or in local.properties)")

            val smtp = get("SMTP_HOST")?.let { host ->
                val user = get("SMTP_USER")
                val pass = get("SMTP_PASS")
                if (user == null || pass == null) null
                else SmtpConfig(host, get("SMTP_PORT")?.toIntOrNull() ?: 465, user, pass, get("SMTP_FROM_NAME") ?: "Unyielding Might")
            }
            val stripe = get("STRIPE_SECRET_KEY")?.let { sk ->
                get("STRIPE_PUBLISHABLE_KEY")?.let { pk -> StripeConfig(sk, pk) }
            }

            return AppConfig(
                port = get("PORT")?.toIntOrNull() ?: 8080,
                db = DbConfig(
                    url = get("MYSQL_URL") ?: "jdbc:mariadb://localhost:3306/unyielding_might",
                    user = require("MYSQL_USER"),
                    password = get("MYSQL_PASSWORD") ?: "",
                ),
                jwt = JwtConfig(
                    secret = require("JWT_SECRET"),
                    issuer = "unyielding-might",
                    audience = "unyielding-might-app",
                    ttlHours = get("JWT_TTL_HOURS")?.toLongOrNull() ?: (24 * 7),
                ),
                smtp = smtp,
                stripe = stripe,
                newsApiKey = get("NEWS_API_KEY"),
                emailVerificationApiKey = get("QEV_APIKEY")?.takeIf { get("VERIFY_EMAIL_DELIVERABILITY")?.toBooleanStrictOrNull() != false },
                currency = get("CURRENCY") ?: "nzd",
                seedDemoData = get("SEED_DEMO_DATA")?.toBooleanStrictOrNull() ?: true,
                logOtpCodes = get("LOG_OTP_CODES")?.toBooleanStrictOrNull() ?: (smtp == null),
            )
        }

        /** Walks up from the working directory so `local.properties` is found from the repo root or the server module. */
        private fun locateLocalProperties(): File {
            var dir: File? = File("").absoluteFile
            while (dir != null) {
                val candidate = File(dir, "local.properties")
                if (candidate.exists()) return candidate
                dir = dir.parentFile
            }
            return File("local.properties")
        }
    }
}
