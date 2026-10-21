package com.unyieldingmight.app.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.PaymentMode
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Takes a [PaymentIntentDto] from the server, collects payment (Stripe PaymentSheet, or a simulated
 * dialog when the server runs without Stripe keys), then asks the server to verify and fulfil it.
 */
@Stable
class PaymentLauncher internal constructor(
    private val container: AppContainer,
    private val scope: CoroutineScope,
    private val sheet: PaymentSheet,
    private val context: android.content.Context,
    private val pendingId: MutableState<Int?>,
    private val mockIntent: MutableState<PaymentIntentDto?>,
    private val onResult: (PaymentResultDto?) -> Unit,
) {
    var busy by mutableStateOf(false)
        internal set

    fun launch(intent: PaymentIntentDto) {
        pendingId.value = intent.paymentId
        when (intent.mode) {
            PaymentMode.STRIPE -> {
                val key = intent.publishableKey
                val secret = intent.clientSecret
                if (key == null || secret == null) {
                    container.messenger.error("Payments aren't set up correctly on the server.")
                    return
                }
                PaymentConfiguration.init(context, key)
                sheet.presentWithPaymentIntent(
                    secret,
                    PaymentSheet.Configuration.Builder("Unyielding Might").build(),
                )
            }
            PaymentMode.MOCK -> mockIntent.value = intent
        }
    }

    internal fun onSheetResult(result: PaymentSheetResult) {
        val id = pendingId.value ?: return
        when (result) {
            is PaymentSheetResult.Completed -> confirm(id)
            is PaymentSheetResult.Canceled -> cancel(id)
            is PaymentSheetResult.Failed -> {
                container.messenger.error(result.error.message ?: "The payment couldn't be completed.", "Payment failed")
                onResult(null)
            }
        }
    }

    internal fun confirm(id: Int) {
        scope.launch {
            busy = true
            try {
                val result = container.api.confirmPayment(id)
                pendingId.value = null
                when (result.status) {
                    PaymentStatus.SUCCEEDED -> container.messenger.success("Payment complete", result.message)
                    PaymentStatus.PENDING -> container.messenger.info("Payment processing", result.message)
                    else -> container.messenger.error(result.message, "Payment not completed")
                }
                onResult(result)
            } catch (e: Exception) {
                container.messenger.error(e, "Couldn't confirm payment")
                onResult(null)
            } finally {
                busy = false
            }
        }
    }

    internal fun cancel(id: Int) {
        pendingId.value = null
        scope.launch { runCatching { container.api.cancelPayment(id) } }
        onResult(null)
    }
}

@Composable
fun rememberPaymentLauncher(onResult: (PaymentResultDto?) -> Unit): PaymentLauncher {
    val container = LocalContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pendingId = rememberSaveable { mutableStateOf<Int?>(null) }
    val mockIntent = remember { mutableStateOf<PaymentIntentDto?>(null) }
    val holder = remember { arrayOfNulls<PaymentLauncher>(1) }
    val latestOnResult by rememberUpdatedState(onResult)

    val sheet = remember { PaymentSheet.Builder { result -> holder[0]?.onSheetResult(result) } }.build()
    val launcher = remember(sheet) {
        PaymentLauncher(container, scope, sheet, context, pendingId, mockIntent) { latestOnResult(it) }.also { holder[0] = it }
    }

    mockIntent.value?.let { intent ->
        MockPaymentDialog(
            intent = intent,
            onPay = { mockIntent.value = null; launcher.confirm(intent.paymentId) },
            onCancel = { mockIntent.value = null; launcher.cancel(intent.paymentId) },
        )
    }
    return launcher
}

@Composable
private fun MockPaymentDialog(intent: PaymentIntentDto, onPay: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Rounded.CreditCard, null) },
        title = { Text("Pay ${Money.format(intent.amountCents)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(intent.description, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "This server is running in demo payment mode, so no card is charged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onPay) { Text("Pay now") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}
