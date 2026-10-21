package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.DropdownField
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.digitsOnly
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.shop.ShopItemArt
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.Warning
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemDto
import com.unyieldingmight.shared.model.ShopItemUpsertRequest
import com.unyieldingmight.shared.model.TrainerDto
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import kotlinx.coroutines.flow.MutableStateFlow

// ---------- Trainers ----------

class AdminTrainersViewModel(container: AppContainer) : LoadViewModel<List<TrainerDto>>(container) {
    val saving = MutableStateFlow(false)
    override suspend fun load() = container.api.trainers()

    fun save(id: Int?, request: TrainerUpsertRequest, onDone: () -> Unit) = act(saving, {
        if (id == null) container.api.createTrainer(request) else container.api.updateTrainer(id, request)
    }) { onDone(); refresh() }

    fun delete(trainer: TrainerDto) = act(block = { container.api.deleteTrainer(trainer.id) }) {
        container.messenger.success("Trainer removed", "${trainer.fullName} won't appear on new classes. Past classes keep their history.")
        refresh()
    }
}

@Composable
fun AdminTrainersScreen() {
    val vm = containerViewModel { AdminTrainersViewModel(it) }
    val state by vm.state.collectAsState()
    var editing by remember { mutableStateOf<TrainerDto?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<TrainerDto?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        "Trainers",
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Add trainer") }) },
    ) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { trainers ->
            LazyVerticalGrid(
                GridCells.Adaptive(320.dp), Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(trainers, key = { it.id }) { t ->
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(t.specialty.uppercase(), style = Overline, color = MaterialTheme.colorScheme.primary)
                                Text(t.fullName, style = MaterialTheme.typography.titleMedium)
                                Text(t.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { editing = t }) { Icon(Icons.Rounded.Edit, "Edit ${t.fullName}") }
                            IconButton(onClick = { deleting = t }) { Icon(Icons.Rounded.Delete, "Remove ${t.fullName}") }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        TrainerDialog(editing, vm, onDismiss = { creating = false; editing = null })
    }
    deleting?.let { t ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove ${t.fullName}?") },
            text = { Text("They'll be unassigned from the trainer list. Existing classes keep their details.") },
            confirmButton = { TextButton(onClick = { vm.delete(t); deleting = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Back") } },
        )
    }
}

@Composable
private fun TrainerDialog(existing: TrainerDto?, vm: AdminTrainersViewModel, onDismiss: () -> Unit) {
    val saving by vm.saving.collectAsState()
    var first by remember { mutableStateOf(existing?.firstName.orEmpty()) }
    var last by remember { mutableStateOf(existing?.lastName.orEmpty()) }
    var specialty by remember { mutableStateOf(existing?.specialty.orEmpty()) }
    var bio by remember { mutableStateOf(existing?.bio.orEmpty()) }
    var showErrors by remember { mutableStateOf(false) }
    fun err(v: String) = if (showErrors && v.isBlank()) "Required" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add trainer" else "Edit trainer") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UmTextField(first, { first = it }, "First name", error = err(first))
                UmTextField(last, { last = it }, "Last name", error = err(last))
                UmTextField(specialty, { specialty = it }, "Specialty", error = err(specialty))
                UmTextField(bio, { bio = it }, "Bio", error = err(bio), singleLine = false, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                showErrors = true
                if (listOf(first, last, specialty, bio).any { it.isBlank() }) return@TextButton
                vm.save(existing?.id, TrainerUpsertRequest(first.trim(), last.trim(), bio.trim(), specialty.trim()), onDismiss)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ---------- Shop items ----------

class AdminShopViewModel(container: AppContainer) : LoadViewModel<List<ShopItemDto>>(container) {
    val saving = MutableStateFlow(false)
    override suspend fun load() = container.api.adminShopItems()

    fun save(id: Int?, request: ShopItemUpsertRequest, onDone: () -> Unit) = act(saving, {
        if (id == null) container.api.createShopItem(request) else container.api.updateShopItem(id, request)
    }) { onDone(); refresh() }

    fun delete(item: ShopItemDto) = act(block = { container.api.deleteShopItem(item.id) }) {
        container.messenger.success("Item removed", "${item.name} is no longer in the shop.")
        refresh()
    }
}

@Composable
fun AdminShopScreen() {
    val vm = containerViewModel { AdminShopViewModel(it) }
    val state by vm.state.collectAsState()
    var editing by remember { mutableStateOf<ShopItemDto?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ShopItemDto?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        "Shop items",
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Add item") }) },
    ) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { items ->
            LazyVerticalGrid(
                GridCells.Adaptive(320.dp), Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = MaterialTheme.shapes.medium) { ShopItemArt(item, Modifier.size(64.dp), 28.dp) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall)
                                Text("${Money.format(item.priceCents)} · ${item.category.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    when {
                                        !item.active -> Pill("Hidden", MaterialTheme.colorScheme.outline, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        item.stock == 0 -> Pill("Sold out", MaterialTheme.colorScheme.error)
                                        item.stock <= 5 -> Pill("${item.stock} left", Warning)
                                        else -> Pill("${item.stock} in stock", MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                            IconButton(onClick = { editing = item }) { Icon(Icons.Rounded.Edit, "Edit ${item.name}") }
                            IconButton(onClick = { deleting = item }) { Icon(Icons.Rounded.Delete, "Remove ${item.name}") }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) ShopItemDialog(editing, vm) { creating = false; editing = null }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove ${item.name}?") },
            text = { Text("Items that appear on past orders are hidden instead of deleted, so receipts stay intact.") },
            confirmButton = { TextButton(onClick = { vm.delete(item); deleting = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Back") } },
        )
    }
}

@Composable
private fun ShopItemDialog(existing: ShopItemDto?, vm: AdminShopViewModel, onDismiss: () -> Unit) {
    val saving by vm.saving.collectAsState()
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember { mutableStateOf(existing?.category ?: ShopCategory.APPAREL) }
    var price by remember { mutableStateOf(existing?.let { "%.2f".format(it.priceCents / 100.0) }.orEmpty()) }
    var stock by remember { mutableStateOf(existing?.stock?.toString().orEmpty()) }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl.orEmpty()) }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    var showErrors by remember { mutableStateOf(false) }

    val errors = mapOf(
        "name" to if (name.isBlank()) "Required" else null,
        "description" to if (description.isBlank()) "Required" else null,
        "price" to if ((price.toDoubleOrNull() ?: -1.0) < 0) "Enter a price" else null,
        "stock" to if (stock.toIntOrNull() == null) "Enter stock" else null,
        "image" to if (imageUrl.isNotBlank() && !imageUrl.startsWith("https://")) "Must start with https://" else null,
    )
    val shown = if (showErrors) errors else emptyMap()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add item" else "Edit item") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UmTextField(name, { name = it }, "Name", error = shown["name"])
                UmTextField(description, { description = it }, "Description", error = shown["description"], singleLine = false, minLines = 2)
                DropdownField("Category", ShopCategory.entries, category, { category = it }, { it.label })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UmTextField(price, { price = it.filter { c -> c.isDigit() || c == '.' }.take(8) }, "Price", Modifier.weight(1f), error = shown["price"], keyboardType = KeyboardType.Decimal)
                    UmTextField(stock, { stock = it.digitsOnly(6) }, "Stock", Modifier.weight(1f), error = shown["stock"], keyboardType = KeyboardType.Number)
                }
                UmTextField(imageUrl, { imageUrl = it.trim() }, "Image URL (optional)", error = shown["image"], keyboardType = KeyboardType.Uri)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Visible in shop", Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                showErrors = true
                if (errors.values.any { it != null }) return@TextButton
                vm.save(
                    existing?.id,
                    ShopItemUpsertRequest(name.trim(), description.trim(), category, Math.round(price.toDouble() * 100).toInt(), stock.toInt(), imageUrl.ifBlank { null }, active),
                    onDismiss,
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
