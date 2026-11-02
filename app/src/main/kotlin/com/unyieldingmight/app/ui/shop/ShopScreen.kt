package com.unyieldingmight.app.ui.shop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.unyieldingmight.app.ui.common.resolveImageUrl
import com.unyieldingmight.app.ui.navigation.AdminShopItemEditRoute
import com.unyieldingmight.app.ui.navigation.CartRoute
import com.unyieldingmight.app.ui.navigation.OrdersRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.Warning
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Admins also see hidden items, so they can bring them back. */
class ShopViewModel(container: AppContainer, private val isAdmin: Boolean) : LoadViewModel<List<ShopItemDto>>(container) {
    override suspend fun load() = if (isAdmin) container.api.adminShopItems() else container.api.shopItems()
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
        ShopCategory.SUPPLEMENTS -> listOf(Color(0xFF3D2604), Color(0xFFF7971D))
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
        val photo = resolveImageUrl(item.imageUrl)
        if (photo != null) {
            // Product shots have white studio backgrounds, so show the whole photo on white rather than cropping it.
            SubcomposeAsyncImage(
                model = photo,
                contentDescription = item.name,
                contentScale = ContentScale.Fit,
                loading = { placeholder() },
                error = { placeholder() },
                modifier = Modifier.fillMaxSize().background(Color.White),
            )
        } else placeholder()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(isAdmin: Boolean) {
    val vm = containerViewModel { ShopViewModel(it, isAdmin) }
    val state by vm.state.collectAsState()
    val container = LocalContainer.current
    val cart by container.cart.entries.collectAsState()
    val shell = LocalShell.current
    var category by rememberSaveable { mutableStateOf<ShopCategory?>(null) }
    var selected by remember { mutableStateOf<ShopItemDto?>(null) }
    val flyToCart = rememberFlyToCartState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { vm.refresh() }

    val onAdd: (ShopItemDto, Offset?) -> Boolean = { item, from ->
        addToCart(container, item).also { added ->
            if (added) {
                flyToCart.launch(from, item.category.icon)
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar("${item.name} added to cart", actionLabel = "View cart", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) shell.nav.navigate(CartRoute)
                }
            }
        }
    }

    Box {
        ScreenScaffold(
            title = "Shop",
            topLevel = true,
            snackbarHost = { SnackbarHost(snackbar) },
            floatingActionButton = {
                if (isAdmin) {
                    ExtendedFloatingActionButton(
                        onClick = { shell.nav.navigate(AdminShopItemEditRoute()) },
                        icon = { Icon(Icons.Rounded.Add, null) },
                        text = { Text("New item") },
                    )
                }
            },
            actions = {
                if (isAdmin) return@ScreenScaffold
                IconButton(onClick = { shell.nav.navigate(OrdersRoute) }) { Icon(Icons.Rounded.Receipt, "My orders") }
                IconButton(
                    onClick = { shell.nav.navigate(CartRoute) },
                    modifier = Modifier.onGloballyPositioned { flyToCart.target = it.boundsInRoot().center },
                ) {
                    val badgeScale = cartBadgeScale(flyToCart)
                    BadgedBox(badge = {
                        if (cart.isNotEmpty()) Badge(Modifier.graphicsLayer { scaleX = badgeScale; scaleY = badgeScale }) { Text("${Cart.count(cart)}") }
                    }) {
                        Icon(Icons.Rounded.ShoppingCart, "Cart")
                    }
                }
            },
        ) { padding ->
            LoadContent(state, vm::refresh, Modifier.padding(padding)) { items ->
                val shown = items.filter { category == null || it.category == category }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(170.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (isAdmin) 96.dp else 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                if (isAdmin) "Tap an item to edit it. Hidden items aren't shown to members." else "Pick up from the front desk on your next visit.",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { FilterChip(category == null, onClick = { category = null }, label = { Text("All") }) }
                                items(ShopCategory.entries) { c ->
                                    FilterChip(category == c, onClick = { category = if (category == c) null else c }, label = { Text(c.label) }, leadingIcon = { Icon(c.icon, null) })
                                }
                            }
                        }
                    }
                    items(shown, key = { it.id }) { item ->
                        if (isAdmin) {
                            val edit = { shell.nav.navigate(AdminShopItemEditRoute(item.id)) }
                            ShopItemCard(item, onClick = edit) {
                                FilledTonalIconButton(onClick = edit) { Icon(Icons.Rounded.Edit, "Edit ${item.name}") }
                            }
                        } else {
                            ShopItemCard(item, onClick = { selected = item }) { AddToCartButton(item) { from -> onAdd(item, from) } }
                        }
                    }
                }
            }
        }
        FlyToCartOverlay(flyToCart)
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
                    "Add to cart", { onAdd(item, null); selected = null }, Modifier.fillMaxWidth(),
                    enabled = item.stock > 0, icon = Icons.Rounded.AddShoppingCart,
                )
            }
        }
    }
}

/** Returns true if the item was added (false when the cart already holds all the stock). */
private fun addToCart(container: AppContainer, item: ShopItemDto): Boolean {
    val inCart = container.cart.entries.value.firstOrNull { it.item.id == item.id }?.quantity ?: 0
    if (inCart >= item.stock) {
        container.messenger.info("That's all we have", "There are only ${item.stock} × ${item.name} in stock.")
        return false
    }
    container.cart.add(item)
    return true
}

@Composable
private fun StockLabel(item: ShopItemDto) {
    when {
        item.stock == 0 -> Pill("Sold out", MaterialTheme.colorScheme.error)
        item.stock <= 5 -> Pill("Only ${item.stock} left", Warning)
        else -> Pill("In stock", MaterialTheme.colorScheme.tertiary)
    }
}

/** [action] is the card's button: add to cart for customers, edit for admins. */
@Composable
private fun ShopItemCard(item: ShopItemDto, onClick: () -> Unit, action: @Composable () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            Box {
                ShopItemArt(item, Modifier.fillMaxWidth().aspectRatio(1.3f))
                if (!item.active) {
                    // Solid so it reads on top of white product photos.
                    Surface(shape = MaterialTheme.shapes.small, color = Color.Black.copy(alpha = 0.75f), modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                        Text("Hidden from shop", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
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
                    action()
                }
            }
        }
    }
}

@Composable
private fun AddToCartButton(item: ShopItemDto, onAdd: (from: Offset) -> Boolean) {
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    var justAdded by remember { mutableStateOf(false) }
    LaunchedEffect(justAdded) {
        if (justAdded) {
            delay(1200)
            justAdded = false
        }
    }
    FilledTonalIconButton(
        onClick = { if (onAdd(buttonCenter)) justAdded = true },
        enabled = item.stock > 0,
        modifier = Modifier.onGloballyPositioned { buttonCenter = it.boundsInRoot().center },
    ) {
        // Briefly swap to a tick so the tap is acknowledged right on the button.
        AnimatedContent(justAdded, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = "added") { added ->
            if (added) Icon(Icons.Rounded.Check, "Added to cart", tint = MaterialTheme.colorScheme.primary)
            else Icon(Icons.Rounded.AddShoppingCart, "Add ${item.name} to cart")
        }
    }
}
