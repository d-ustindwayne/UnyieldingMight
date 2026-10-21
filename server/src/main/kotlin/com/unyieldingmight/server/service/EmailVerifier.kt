package com.unyieldingmight.server.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/**
 * QuickEmailVerification check (ported from the app's old EmailVerification class, now server-side
 * so the API key isn't shipped in the APK). Fails open: if the service is down, registration continues.
 */
class EmailVerifier(private val http: HttpClient, private val apiKey: String) {
    private val log = LoggerFactory.getLogger(EmailVerifier::class.java)

    @Serializable
    private data class Response(
        val result: String? = null,
        val reason: String? = null,
        val disposable: String? = null,
        @SerialName("safe_to_send") val safeToSend: String? = null,
    )

    suspend fun isUndeliverable(email: String): Boolean = runCatching {
        val response = http.get("https://api.quickemailverification.com/v1/verify") {
            parameter("email", email)
            parameter("apikey", apiKey)
        }
        if (!response.status.isSuccess()) return false
        val body = response.body<Response>()
        body.result == "invalid" || body.disposable == "true"
    }.onFailure { log.warn("Email verification unavailable: {}", it.message) }.getOrDefault(false)
}
