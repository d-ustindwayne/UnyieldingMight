package com.unyieldingmight.app.data

import com.unyieldingmight.shared.model.CartLine
import com.unyieldingmight.shared.model.ShopItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory shopping cart. Prices shown here are for display; the server recalculates totals at checkout. */
class Cart {
    data class Entry(val item: ShopItemDto, val quantity: Int)

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    fun add(item: ShopItemDto) = _entries.update { list ->
        val existing = list.firstOrNull { it.item.id == item.id }
        if (existing == null) list + Entry(item, 1)
        else list.map { if (it.item.id == item.id) it.copy(quantity = (it.quantity + 1).coerceAtMost(item.stock)) else it }
    }

    fun setQuantity(itemId: Int, quantity: Int) = _entries.update { list ->
        if (quantity <= 0) list.filterNot { it.item.id == itemId }
        else list.map { if (it.item.id == itemId) it.copy(quantity = quantity.coerceAtMost(it.item.stock)) else it }
    }

    fun clear() {
        _entries.value = emptyList()
    }

    fun lines(): List<CartLine> = _entries.value.map { CartLine(it.item.id, it.quantity) }

    companion object {
        fun total(entries: List<Entry>): Int = entries.sumOf { it.item.priceCents * it.quantity }
        fun count(entries: List<Entry>): Int = entries.sumOf { it.quantity }
    }
}
