package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.payment.GatewayStatus
import com.unyieldingmight.server.payment.PaymentGateway
import com.unyieldingmight.server.repo.PaymentRecord
import com.unyieldingmight.server.repo.findPayment
import com.unyieldingmight.server.repo.insertPayment
import com.unyieldingmight.server.repo.setPaymentProviderRef
import com.unyieldingmight.server.repo.transitionPayment
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.PaymentPurpose
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import org.slf4j.LoggerFactory

/**
 * Owns the payment lifecycle: PENDING -> SUCCEEDED (then fulfilled) / FAILED / CANCELLED / REFUNDED.
 * Fulfilment is delegated to the service that owns the purchase (booking, order, membership).
 */
class PaymentService(
    private val db: Database,
    private val gateway: PaymentGateway,
    private val currency: String,
) {
    private val log = LoggerFactory.getLogger(PaymentService::class.java)

    /** Implemented by services that sell something. */
    interface Fulfiller {
        val purpose: PaymentPurpose

        /** Delivers the purchase. Throw [ApiException] if it can no longer be delivered; the payment is then refunded. */
        suspend fun fulfil(payment: PaymentRecord): PaymentResultDto

        /** Rebuilds the result for a payment that was already fulfilled (idempotent confirm). */
        suspend fun describe(payment: PaymentRecord): PaymentResultDto

        /** Releases anything held for a payment that won't complete. */
        suspend fun abandon(payment: PaymentRecord) = Unit
    }

    private val fulfillers = mutableMapOf<PaymentPurpose, Fulfiller>()

    fun register(fulfiller: Fulfiller) {
        fulfillers[fulfiller.purpose] = fulfiller
    }

    val mode get() = gateway.mode
    val publishableKey get() = gateway.publishableKey

    suspend fun start(userId: Int, purpose: PaymentPurpose, referenceId: Int, amountCents: Int, description: String): PaymentIntentDto {
        val paymentId = db.tx { insertPayment(userId, purpose, referenceId, amountCents, currency, gateway.mode) }
        val intent = try {
            gateway.createIntent(
                amountCents, currency, description,
                mapOf("payment_id" to paymentId.toString(), "purpose" to purpose.name, "user_id" to userId.toString()),
            )
        } catch (e: Exception) {
            log.error("Creating payment intent failed", e)
            db.tx { transitionPayment(paymentId, PaymentStatus.PENDING, PaymentStatus.FAILED) }
            throw ApiException.unavailable("Payments are temporarily unavailable. Please try again shortly.")
        }
        db.tx { setPaymentProviderRef(paymentId, intent.providerRef) }
        return PaymentIntentDto(
            paymentId = paymentId,
            mode = gateway.mode,
            amountCents = amountCents,
            currency = currency,
            description = description,
            clientSecret = intent.clientSecret,
            publishableKey = gateway.publishableKey,
        )
    }

    /** Called by the app after PaymentSheet (or the mock dialog) completes. Safe to call repeatedly. */
    suspend fun confirm(userId: Int, paymentId: Int): PaymentResultDto {
        val payment = owned(userId, paymentId)
        val fulfiller = fulfillers.getValue(payment.purpose)

        when (payment.status) {
            PaymentStatus.SUCCEEDED -> return fulfiller.describe(payment)
            PaymentStatus.REFUNDED -> return result(payment, PaymentStatus.REFUNDED, "This payment was refunded.")
            PaymentStatus.FAILED, PaymentStatus.CANCELLED -> {
                // Money taken after we'd given up on the payment (e.g. sheet closed mid-payment): give it back.
                val ref = payment.providerRef
                if (ref != null && gateway.status(ref) == GatewayStatus.SUCCEEDED &&
                    db.tx { transitionPayment(paymentId, payment.status, PaymentStatus.REFUNDED) }
                ) {
                    gateway.refund(ref)
                    return result(payment, PaymentStatus.REFUNDED, "This purchase had already been cancelled, so your payment was refunded.")
                }
                return result(payment, payment.status, "This payment didn't go through.")
            }
            PaymentStatus.PENDING -> Unit
        }

        val ref = payment.providerRef ?: throw ApiException.conflict("Payment was not started correctly")
        return when (gateway.status(ref)) {
            GatewayStatus.SUCCEEDED -> {
                // Only the request that wins this transition fulfils, so a retried confirm can't double-book.
                if (!db.tx { transitionPayment(paymentId, PaymentStatus.PENDING, PaymentStatus.SUCCEEDED) }) {
                    return confirm(userId, paymentId)
                }
                try {
                    fulfiller.fulfil(payment.copy(status = PaymentStatus.SUCCEEDED))
                } catch (e: ApiException) {
                    refund(payment.copy(status = PaymentStatus.SUCCEEDED))
                    fulfiller.abandon(payment)
                    result(payment, PaymentStatus.REFUNDED, "${e.message} Your payment has been refunded.")
                }
            }
            GatewayStatus.PROCESSING -> result(payment, PaymentStatus.PENDING, "Your payment is still processing.")
            GatewayStatus.REQUIRES_ACTION -> result(payment, PaymentStatus.PENDING, "This payment hasn't been completed yet.")
            GatewayStatus.FAILED, GatewayStatus.CANCELLED -> {
                db.tx { transitionPayment(paymentId, PaymentStatus.PENDING, PaymentStatus.FAILED) }
                fulfiller.abandon(payment)
                result(payment, PaymentStatus.FAILED, "The payment didn't go through. You haven't been charged.")
            }
        }
    }

    /** The user closed the payment sheet without paying. */
    suspend fun cancel(userId: Int, paymentId: Int) {
        val payment = owned(userId, paymentId)
        if (db.tx { transitionPayment(paymentId, PaymentStatus.PENDING, PaymentStatus.CANCELLED) }) {
            payment.providerRef?.let { gateway.cancel(it) }
            fulfillers[payment.purpose]?.abandon(payment)
        }
    }

    /** Refunds a successful payment. Returns false if it couldn't be refunded (already refunded, or the provider failed). */
    suspend fun refund(payment: PaymentRecord): Boolean {
        if (!db.tx { transitionPayment(payment.id, PaymentStatus.SUCCEEDED, PaymentStatus.REFUNDED) }) return false
        return try {
            payment.providerRef?.let { gateway.refund(it) }
            true
        } catch (e: Exception) {
            log.error("Refund failed for payment {}", payment.id, e)
            db.tx { transitionPayment(payment.id, PaymentStatus.REFUNDED, PaymentStatus.SUCCEEDED) }
            false
        }
    }

    suspend fun find(paymentId: Int): PaymentRecord? = db.tx { findPayment(paymentId) }

    private suspend fun owned(userId: Int, paymentId: Int): PaymentRecord {
        val payment = db.tx { findPayment(paymentId) }
        if (payment == null || payment.userId != userId) throw ApiException.notFound("Payment not found")
        return payment
    }

    private fun result(payment: PaymentRecord, status: PaymentStatus, message: String) =
        PaymentResultDto(paymentId = payment.id, status = status, message = message)
}
