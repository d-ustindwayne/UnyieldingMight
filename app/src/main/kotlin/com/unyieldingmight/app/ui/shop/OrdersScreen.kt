package com.unyieldingmight.app.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Success
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.OrderDto
import com.unyieldingmight.shared.model.OrderStatus

class OrdersViewModel(container: AppContainer) : LoadViewModel<List<OrderDto>>(container) {
    override suspend fun load() = container.api.orders()
}

@Composable
fun OrdersScreen() {
    val vm = containerViewModel { OrdersViewModel(it) }
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold("Orders") { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { orders ->
            if (orders.isEmpty()) {
                EmptyState(Icons.Rounded.Receipt, "No orders yet", "Things you buy in the shop will show up here.")
                return@LoadContent
            }
            Centered(720.dp) {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(orders, key = { it.id }) { order ->
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium)
                                        Text(Format.fullDate(order.createdAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    when (order.status) {
                                        OrderStatus.PAID -> Pill("Paid · ready to collect", Success)
                                        OrderStatus.CANCELLED -> Pill("Cancelled", MaterialTheme.colorScheme.error)
                                        OrderStatus.PENDING -> Pill("Pending", MaterialTheme.colorScheme.outline)
                                    }
                                }
                                HorizontalDivider()
                                order.lines.forEach { line ->
                                    Row {
                                        Text("${line.quantity} × ${line.name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Text(Money.format(line.unitPriceCents * line.quantity), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                HorizontalDivider()
                                Row {
                                    Text("Total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Text(Money.format(order.totalCents), style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
