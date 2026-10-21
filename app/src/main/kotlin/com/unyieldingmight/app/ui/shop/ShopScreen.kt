package com.unyieldingmight.app.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.data.Cart
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.navigation.CartRoute
import com.unyieldingmight.app.ui.navigation.OrdersRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.Warning
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemDto

class ShopViewModel(container: AppContainer) : LoadViewModel<List<ShopItemDto>>(container) {
    override suspend fun load() = container.api.shopItems()
}

val ShopCategory.icon: ImageVector
    get() = when (this) {
        ShopCategory.APPAREL -> Icons.Rounded.Checkroom
        ShopCategory.SUPPLEMENTS -> Icons.Rounded.Science
        ShopCategory.EQUIPMENT -> Icons.Rounded.FitnessCenter
        ShopCategory.ACCESSORIES -> Icons.Rounded.LocalDrink
    }

private val ShopCategory.gradient: List<Color>
    get() = when (this) {
        ShopCategory.APPAREL -> listOf(Color(0xFF1C1C1C), Color(0xFF4E4E4E))
        ShopCategory.SUPPLEMENTS -> listOf(Color(0xFF3A1A08), Color(0xFFFF6B2C))
        ShopCategory.EQUIPMENT -> listOf(Color(0xFF0E1A26), Color(0xFF3D6A8F))
        ShopCategory.ACCESSORIES -> listOf(Color(0xFF0E2219), Color(0xFF3C8F6A))
    }

/** Product image when the admin has set one, otherwise branded category artwork. */
@Composable
fun ShopItemArt(item: ShopItemDto, modifier: Modifier = Modifier, iconSize: Dp = 48.dp) {
    val placeholder: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(item.category.gradient)), contentAlignment = Alignment.Center) {
            Icon(item.category.icon, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(iconSize))
        }
    }
    Box(modifier) {
        if (item.imageUrl != null) {
            SubcomposeAsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                loading = { placeholder() },
                error = { placeholder() },
                modifier = Modifier.fillMaxSize(),
            )
        } else placeholder()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen() {
    val vm = containerViewModel { ShopViewModel(it) }
    val state by vm.state.collectAsState()
    val container = LocalContainer.current
    val cart by container.cart.entries.collectAsState()
    val shell = LocalShell.current
    var category by rememberSaveable { mutableStateOf<ShopCategory?>(null) }
    var selected by remember { mutableStateOf<ShopItemDto?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        title = "Shop",
        topLevel = true,
        actions = {
            IconButton(onClick = { shell.nav.navigate(OrdersRoute) }) { Icon(Icons.Rounded.Receipt, "My orders") }
            IconButton(onClick = { shell.nav.navigate(CartRoute) }) {
                BadgedBox(badge = { if (cart.isNotEmpty()) Badge { Text("${Cart.count(cart)}") } }) {
                    Icon(Icons.Rounded.ShoppingCart, "Cart")
                }
            }
        },
    ) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { items ->
            val shown = items.filter { category == null || it.category == category }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(170.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pick up from the front desk on your next visit.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { FilterChip(category == null, onClick = { category = null }, label = { Text("All") }) }
                            items(ShopCategory.entries) { c ->
                                FilterChip(category == c, onClick = { category = if (category == c) null else c }, label = { Text(c.label) }, leadingIcon = { Icon(c.icon, null) })
                            }
                        }
                    }
                }
                items(shown, key = { it.id }) { item ->
                    ShopItemCard(item, onClick = { selected = item }, onAdd = { addToCart(container, item) })
                }
            }
        }
    }

    selected?.let { item ->
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.large) { ShopItemArt(item, Modifier.fillMaxWidth().height(200.dp), 72.dp) }
                Text(item.category.label.uppercase(), style = Overline, color = MaterialTheme.colorScheme.primary)
                Text(item.name, style = MaterialTheme.typography.headlineSmall)
                Text(item.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Money.format(item.priceCents), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    StockLabel(item)
                }
                PrimaryButton(
                    "Add to cart", { addToCart(container, item); selected = null }, Modifier.fillMaxWidth(),
                    enabled = item.stock > 0, icon = Icons.Rounded.AddShoppingCart,
                )
            }
        }
    }
}

private fun addToCart(container: AppContainer, item: ShopItemDto) {
    val inCart = container.cart.entries.value.firstOrNull { it.item.id == item.id }?.quantity ?: 0
    if (inCart >= item.stock) {
        container.messenger.info("That's all we have", "There are only ${item.stock} × ${item.name} in stock.")
    } else {
        container.cart.add(item)
    }
}

@Composable
private fun StockLabel(item: ShopItemDto) {
    when {
        item.stock == 0 -> Pill("Sold out", MaterialTheme.colorScheme.error)
        item.stock <= 5 -> Pill("Only ${item.stock} left", Warning)
        else -> Pill("In stock", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun ShopItemCard(item: ShopItemDto, onClick: () -> Unit, onAdd: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            ShopItemArt(item, Modifier.fillMaxWidth().aspectRatio(1.3f))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.height(40.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(Money.format(item.priceCents), style = MaterialTheme.typography.titleMedium)
                        if (item.stock <= 5) {
                            Text(
                                if (item.stock == 0) "Sold out" else "${item.stock} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.stock == 0) MaterialTheme.colorScheme.error else Warning,
                            )
                        }
                    }
                    FilledTonalIconButton(onClick = onAdd, enabled = item.stock > 0) { Icon(Icons.Rounded.AddShoppingCart, "Add ${item.name} to cart") }
                }
            }
        }
    }
}
