package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.enum
import com.unyieldingmight.shared.model.PaymentMode
import com.unyieldingmight.shared.model.PaymentPurpose
import com.unyieldingmight.shared.model.PaymentStatus
import java.time.Instant

data class PaymentRecord(
    val id: Int,
    val userId: Int,
    val purpose: PaymentPurpose,
    val referenceId: Int,
    val amountCents: Int,
    val currency: String,
    val provider: PaymentMode,
    val providerRef: String?,
    val status: PaymentStatus,
)

fun Tx.insertPayment(
    userId: Int,
    purpose: PaymentPurpose,
    referenceId: Int,
    amountCents: Int,
    currency: String,
    provider: PaymentMode,
    now: Instant = Instant.now(),
): Int = insert(
    """
    INSERT INTO payment (user_id, purpose, reference_id, amount_cents, currency, provider, status, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)
    """,
    userId, purpose, referenceId, amountCents, currency, provider, now, now,
)

fun Tx.findPayment(id: Int): PaymentRecord? = queryOne("SELECT * FROM payment WHERE id = ?", id) {
    PaymentRecord(
        id = it.getInt("id"),
        userId = it.getInt("user_id"),
        purpose = it.enum("purpose"),
        referenceId = it.getInt("reference_id"),
        amountCents = it.getInt("amount_cents"),
        currency = it.getString("currency"),
        provider = it.enum("provider"),
        providerRef = it.getString("provider_ref"),
        status = it.enum("status"),
    )
}

fun Tx.setPaymentProviderRef(id: Int, ref: String) =
    update("UPDATE payment SET provider_ref = ?, updated_at = ? WHERE id = ?", ref, Instant.now(), id)

/**
 * Moves a payment from [from] to [to]. Returns false if another request already moved it,
 * which makes fulfilment idempotent when the app retries a confirmation.
 */
fun Tx.transitionPayment(id: Int, from: PaymentStatus, to: PaymentStatus): Boolean =
    update("UPDATE payment SET status = ?, updated_at = ? WHERE id = ? AND status = ?", to, Instant.now(), id, from) == 1

fun Tx.revenueCents(since: Instant): Long =
    count("SELECT COALESCE(SUM(amount_cents), 0) FROM payment WHERE status = 'SUCCEEDED' AND created_at >= ?", since)
