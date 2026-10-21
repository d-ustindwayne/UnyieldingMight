package com.unyieldingmight.server.payment

import com.stripe.StripeClient
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import com.unyieldingmight.server.config.AppConfig
import com.unyieldingmight.shared.model.PaymentMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class CreatedIntent(val providerRef: String, val clientSecret: String?)

enum class GatewayStatus { SUCCEEDED, PROCESSING, REQUIRES_ACTION, FAILED, CANCELLED }

interface PaymentGateway {
    val mode: PaymentMode
    val publishableKey: String?

    suspend fun createIntent(amountCents: Int, currency: String, description: String, metadata: Map<String, String>): CreatedIntent
    suspend fun status(providerRef: String): GatewayStatus
    suspend fun refund(providerRef: String)
    suspend fun cancel(providerRef: String)
}

/** Stripe test-mode PaymentIntents. The app confirms them with PaymentSheet; the server re-checks the status before fulfilling. */
class StripeGateway(config: AppConfig.StripeConfig) : PaymentGateway {
    private val client = StripeClient(config.secretKey)
    override val mode = PaymentMode.STRIPE
    override val publishableKey: String = config.publishableKey

    override suspend fun createIntent(amountCents: Int, currency: String, description: String, metadata: Map<String, String>): CreatedIntent =
        withContext(Dispatchers.IO) {
            val params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents.toLong())
                .setCurrency(currency)
                .setDescription(description)
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build()
            val intent = client.v1().paymentIntents().create(params)
            CreatedIntent(intent.id, intent.clientSecret)
        }

    override suspend fun status(providerRef: String): GatewayStatus = withContext(Dispatchers.IO) {
        val intent = client.v1().paymentIntents().retrieve(providerRef)
        when (intent.status) {
            "succeeded" -> GatewayStatus.SUCCEEDED
            "processing" -> GatewayStatus.PROCESSING
            "canceled" -> GatewayStatus.CANCELLED
            // A brand-new intent also sits in requires_payment_method; it has only failed if an attempt was declined.
            "requires_payment_method" -> if (intent.lastPaymentError != null) GatewayStatus.FAILED else GatewayStatus.REQUIRES_ACTION
            else -> GatewayStatus.REQUIRES_ACTION
        }
    }

    override suspend fun refund(providerRef: String) {
        withContext(Dispatchers.IO) {
            client.v1().refunds().create(RefundCreateParams.builder().setPaymentIntent(providerRef).build())
        }
    }

    override suspend fun cancel(providerRef: String) {
        withContext(Dispatchers.IO) { runCatching { client.v1().paymentIntents().cancel(providerRef) } }
    }
}

/**
 * Stand-in used when no Stripe key is configured (and in tests). Every payment succeeds,
 * so the whole booking/shop/membership flow can be demonstrated offline.
 */
class MockGateway : PaymentGateway {
    override val mode = PaymentMode.MOCK
    override val publishableKey: String? = null
    val refunded = mutableListOf<String>()
    private val cancelled = mutableSetOf<String>()

    override suspend fun createIntent(amountCents: Int, currency: String, description: String, metadata: Map<String, String>) =
        CreatedIntent("mock_pi_${UUID.randomUUID()}", clientSecret = null)

    override suspend fun status(providerRef: String) =
        if (providerRef in cancelled) GatewayStatus.CANCELLED else GatewayStatus.SUCCEEDED

    override suspend fun refund(providerRef: String) {
        refunded += providerRef
    }

    override suspend fun cancel(providerRef: String) {
        cancelled += providerRef
    }
}
