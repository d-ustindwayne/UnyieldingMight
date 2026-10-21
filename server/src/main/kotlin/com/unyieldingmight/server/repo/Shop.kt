package com.unyieldingmight.server.repo

import com.unyieldingmight.server.db.Tx
import com.unyieldingmight.server.db.enum
import com.unyieldingmight.server.db.epochMillis
import com.unyieldingmight.server.db.intOrNull
import com.unyieldingmight.shared.model.OrderDto
import com.unyieldingmight.shared.model.OrderLineDto
import com.unyieldingmight.shared.model.OrderStatus
import com.unyieldingmight.shared.model.ShopItemDto
import com.unyieldingmight.shared.model.ShopItemUpsertRequest
import java.sql.ResultSet
import java.time.Instant

private fun ResultSet.toShopItem() = ShopItemDto(
    id = getInt("id"),
    name = getString("name"),
    description = getString("description"),
    category = enum("category"),
    priceCents = getInt("price_cents"),
    stock = getInt("stock"),
    imageUrl = getString("image_url"),
    active = getBoolean("active"),
)

fun Tx.listShopItems(includeInactive: Boolean = false): List<ShopItemDto> =
    query(
        if (includeInactive) "SELECT * FROM shop_item ORDER BY category, name"
        else "SELECT * FROM shop_item WHERE active = TRUE ORDER BY category, name"
    ) { it.toShopItem() }

fun Tx.findShopItem(id: Int): ShopItemDto? = queryOne("SELECT * FROM shop_item WHERE id = ?", id) { it.toShopItem() }

fun Tx.insertShopItem(req: ShopItemUpsertRequest, now: Instant = Instant.now()): Int = insert(
    """
    INSERT INTO shop_item (name, description, category, price_cents, stock, image_url, active, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """,
    req.name.trim(), req.description.trim(), req.category, req.priceCents, req.stock, req.imageUrl?.trim()?.ifBlank { null }, req.active, now, now,
)

fun Tx.updateShopItem(id: Int, req: ShopItemUpsertRequest): Int = update(
    """
    UPDATE shop_item SET name = ?, description = ?, category = ?, price_cents = ?, stock = ?, image_url = ?, active = ?, updated_at = ?
    WHERE id = ?
    """,
    req.name.trim(), req.description.trim(), req.category, req.priceCents, req.stock, req.imageUrl?.trim()?.ifBlank { null }, req.active, Instant.now(), id,
)

/** Items that appear on past orders are hidden rather than deleted. */
fun Tx.deleteOrHideShopItem(id: Int): Int {
    val referenced = count("SELECT COUNT(*) FROM shop_order_line WHERE item_id = ?", id) > 0
    return if (referenced) update("UPDATE shop_item SET active = FALSE, updated_at = ? WHERE id = ?", Instant.now(), id)
    else update("DELETE FROM shop_item WHERE id = ?", id)
}

/** Decrements stock only if enough is left; returns false when the item sold out meanwhile. */
fun Tx.takeStock(itemId: Int, quantity: Int): Boolean =
    update("UPDATE shop_item SET stock = stock - ? WHERE id = ? AND stock >= ?", quantity, itemId, quantity) == 1

fun Tx.lowStockCount(threshold: Int = 5): Int =
    count("SELECT COUNT(*) FROM shop_item WHERE active = TRUE AND stock <= ?", threshold).toInt()

// ---------- Orders ----------

fun Tx.insertOrder(userId: Int, totalCents: Int, lines: List<OrderLineDto>, now: Instant = Instant.now()): Int {
    val orderId = insert(
        "INSERT INTO shop_order (user_id, status, total_cents, created_at) VALUES (?, 'PENDING', ?, ?)",
        userId, totalCents, now,
    )
    lines.forEach {
        update(
            "INSERT INTO shop_order_line (order_id, item_id, item_name, quantity, unit_price_cents) VALUES (?, ?, ?, ?, ?)",
            orderId, it.itemId, it.name, it.quantity, it.unitPriceCents,
        )
    }
    return orderId
}

fun Tx.setOrderPayment(orderId: Int, paymentId: Int) = update("UPDATE shop_order SET payment_id = ? WHERE id = ?", paymentId, orderId)

fun Tx.setOrderStatus(orderId: Int, status: OrderStatus) = update("UPDATE shop_order SET status = ? WHERE id = ?", status, orderId)

data class OrderOwner(val userId: Int, val status: OrderStatus, val paymentId: Int?)

fun Tx.findOrderOwner(orderId: Int): OrderOwner? =
    queryOne("SELECT user_id, status, payment_id FROM shop_order WHERE id = ?", orderId) {
        OrderOwner(it.getInt("user_id"), it.enum("status"), it.intOrNull("payment_id"))
    }

fun Tx.findOrder(orderId: Int): OrderDto? = listOrders("o.id = ?", orderId).firstOrNull()

fun Tx.listUserOrders(userId: Int): List<OrderDto> = listOrders("o.user_id = ? AND o.status <> 'PENDING'", userId)

private fun Tx.listOrders(where: String, param: Any): List<OrderDto> {
    val rows = query(
        """
        SELECT o.id, o.status, o.total_cents, o.created_at, l.item_id, l.item_name, l.quantity, l.unit_price_cents
        FROM shop_order o JOIN shop_order_line l ON l.order_id = o.id
        WHERE $where
        ORDER BY o.created_at DESC, l.item_name
        """,
        param,
    ) { rs ->
        Triple(
            rs.getInt("id"),
            OrderDto(rs.getInt("id"), rs.enum("status"), rs.getInt("total_cents"), rs.epochMillis("created_at"), emptyList()),
            OrderLineDto(rs.getInt("item_id"), rs.getString("item_name"), rs.getInt("quantity"), rs.getInt("unit_price_cents")),
        )
    }
    return rows.groupBy { it.first }.map { (_, group) -> group.first().second.copy(lines = group.map { it.third }) }
}

fun Tx.orderLines(orderId: Int): List<OrderLineDto> = findOrder(orderId)?.lines.orEmpty()
