package com.unyieldingmight.app.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.data.Cart
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.navigation.CartRoute
import com.unyieldingmight.app.ui.navigation.OrdersRoute
import com.unyieldingmight.app.ui.payment.rememberPaymentLauncher
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.PaymentStatus
import kotlinx.coroutines.launch

@Composable
fun CartScreen() {
    val container = LocalContainer.current
    val shell = LocalShell.current
    val entries by container.cart.entries.collectAsState()
    val scope = rememberCoroutineScope()
    var startingCheckout by remember { mutableStateOf(false) }
    val payments = rememberPaymentLauncher { result ->
        if (result?.status == PaymentStatus.SUCCEEDED) {
            container.cart.clear()
            shell.nav.navigate(OrdersRoute) { popUpTo(CartRoute) { inclusive = true } }
        }
    }

    ScreenScaffold(
        title = "Your cart",
        bottomBar = {
            if (entries.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 2.dp) {
                    Centered(720.dp) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Money.format(Cart.total(entries)), style = MaterialTheme.typography.headlineSmall)
                            }
                            PrimaryButton(
                                "Checkout",
                                onClick = {
                                    scope.launch {
                                        startingCheckout = true
                                        try {
                                            payments.launch(container.api.checkout(container.cart.lines()).payment)
                                        } catch (e: Exception) {
                                            container.messenger.error(e, "Couldn't start checkout")
                                        } finally {
                                            startingCheckout = false
                                        }
                                    }
                                },
                                loading = startingCheckout || payments.busy,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyState(
                Icons.Rounded.ShoppingCart, "Your cart is empty", "Add gear, apparel or supplements from the shop.",
                Modifier.padding(padding),
            ) { OutlinedButton(onClick = { shell.nav.navigateUp() }) { Text("Back to shop") } }
        } else {
            Centered(720.dp) {
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    items(entries, key = { it.item.id }) { entry ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = MaterialTheme.shapes.medium) { ShopItemArt(entry.item, Modifier.size(64.dp), 28.dp) }
                            Column(Modifier.weight(1f)) {
                                Text(entry.item.name, style = MaterialTheme.typography.titleSmall)
                                Text(Money.format(entry.item.priceCents), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { container.cart.setQuantity(entry.item.id, entry.quantity - 1) }) {
                                Icon(Icons.Rounded.Remove, "Remove one")
                            }
                            Text("${entry.quantity}", style = MaterialTheme.typography.titleMedium)
                            IconButton(
                                onClick = { container.cart.setQuantity(entry.item.id, entry.quantity + 1) },
                                enabled = entry.quantity < entry.item.stock,
                            ) { Icon(Icons.Rounded.Add, "Add one") }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
