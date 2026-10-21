package com.unyieldingmight.app

import com.unyieldingmight.app.data.Cart
import com.unyieldingmight.shared.model.CartLine
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartTest {
    private fun item(id: Int, price: Int, stock: Int = 10) =
        ShopItemDto(id, "Item $id", "", ShopCategory.EQUIPMENT, price, stock, null, true)

    @Test
    fun `adding the same item increases its quantity`() {
        val cart = Cart()
        cart.add(item(1, 1000))
        cart.add(item(1, 1000))
        cart.add(item(2, 250))
        assertEquals(listOf(CartLine(1, 2), CartLine(2, 1)), cart.lines())
        assertEquals(2250, Cart.total(cart.entries.value))
        assertEquals(3, Cart.count(cart.entries.value))
    }

    @Test
    fun `quantity never exceeds stock`() {
        val cart = Cart()
        val scarce = item(1, 500, stock = 2)
        repeat(5) { cart.add(scarce) }
        assertEquals(2, cart.entries.value.single().quantity)
        cart.setQuantity(1, 9)
        assertEquals(2, cart.entries.value.single().quantity)
    }

    @Test
    fun `setting quantity to zero removes the line`() {
        val cart = Cart()
        cart.add(item(1, 500))
        cart.setQuantity(1, 0)
        assertTrue(cart.entries.value.isEmpty())
    }
}
