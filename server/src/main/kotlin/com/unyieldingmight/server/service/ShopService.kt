package com.unyieldingmight.server.service

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.db.Database
import com.unyieldingmight.server.email.GymEvent
import com.unyieldingmight.server.email.NotificationCenter
import com.unyieldingmight.server.repo.PaymentRecord
import com.unyieldingmight.server.repo.deleteOrHideShopItem
import com.unyieldingmight.server.repo.findOrder
import com.unyieldingmight.server.repo.findOrderOwner
import com.unyieldingmight.server.repo.findShopItem
import com.unyieldingmight.server.repo.findUserById
import com.unyieldingmight.server.repo.insertOrder
import com.unyieldingmight.server.repo.insertShopItem
import com.unyieldingmight.server.repo.listShopItems
import com.unyieldingmight.server.repo.listUserOrders
import com.unyieldingmight.server.repo.setOrderPayment
import com.unyieldingmight.server.repo.setOrderStatus
import com.unyieldingmight.server.repo.takeStock
import com.unyieldingmight.server.repo.updateShopItem
import com.unyieldingmight.server.validate
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.CheckoutRequest
import com.unyieldingmight.shared.model.CheckoutResponse
import com.unyieldingmight.shared.model.OrderDto
import com.unyieldingmight.shared.model.OrderLineDto
import com.unyieldingmight.shared.model.OrderStatus
import com.unyieldingmight.shared.model.PaymentPurpose
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import com.unyieldingmight.shared.model.ShopItemDto
import com.unyieldingmight.shared.model.ShopItemUpsertRequest

class ShopService(
    private val db: Database,
    private val payments: PaymentService,
    private val notifications: NotificationCenter,
) : PaymentService.Fulfiller {

    override val purpose = PaymentPurpose.SHOP_ORDER

    suspend fun items(includeInactive: Boolean = false): List<ShopItemDto> = db.tx { listShopItems(includeInactive) }

    /**
     * Prices are taken from the database, never from the app, and snapshotted onto the order lines.
     * Stock is checked now and taken atomically when the payment succeeds.
     */
    suspend fun checkout(userId: Int, req: CheckoutRequest): CheckoutResponse {
        val quantities = req.lines.groupBy { it.itemId }.mapValues { (_, lines) -> lines.sumOf { it.quantity } }
        if (quantities.isEmpty()) throw ApiException.badRequest("Your cart is empty")
        if (quantities.values.any { it !in 1..20 }) throw ApiException.badRequest("Quantities must be between 1 and 20")

        val order = db.tx {
            val lines = quantities.map { (itemId, quantity) ->
                val item = findShopItem(itemId)?.takeIf { it.active } ?: throw ApiException.notFound("An item in your cart is no longer available")
                if (item.stock < quantity) throw ApiException.conflict("Only ${item.stock} × ${item.name} left in stock", "out_of_stock")
                OrderLineDto(item.id, item.name, quantity, item.priceCents)
            }
            val total = lines.sumOf { it.unitPriceCents * it.quantity }
            findOrder(insertOrder(userId, total, lines))!!
        }
        val itemCount = order.lines.sumOf { it.quantity }
        val payment = payments.start(userId, PaymentPurpose.SHOP_ORDER, order.id, order.totalCents, "Shop order #${order.id} ($itemCount item${if (itemCount == 1) "" else "s"})")
        db.tx { setOrderPayment(order.id, payment.paymentId) }
        return CheckoutResponse(order, payment)
    }

    override suspend fun fulfil(payment: PaymentRecord): PaymentResultDto {
        val order = db.tx {
            val order = findOrder(payment.referenceId) ?: throw ApiException.notFound("Order not found")
            order.lines.forEach { line ->
                if (!takeStock(line.itemId, line.quantity)) {
                    throw ApiException.conflict("${line.name} sold out before your payment finished.")
                }
            }
            setOrderStatus(order.id, OrderStatus.PAID)
            order.copy(status = OrderStatus.PAID)
        }
        db.tx { findUserById(payment.userId) }?.let { notifications.publish(GymEvent.OrderPaid(it.email, it.firstName, order)) }
        return PaymentResultDto(payment.id, PaymentStatus.SUCCEEDED, "Order #${order.id} confirmed! Collect it at the front desk.", order = order)
    }

    override suspend fun describe(payment: PaymentRecord) =
        PaymentResultDto(payment.id, PaymentStatus.SUCCEEDED, "Order confirmed!", order = db.tx { findOrder(payment.referenceId) })

    override suspend fun abandon(payment: PaymentRecord) {
        db.tx {
            if (findOrderOwner(payment.referenceId)?.status == OrderStatus.PENDING) setOrderStatus(payment.referenceId, OrderStatus.CANCELLED)
        }
    }

    suspend fun orders(userId: Int): List<OrderDto> = db.tx { listUserOrders(userId) }

    // ---------- Admin ----------

    suspend fun create(req: ShopItemUpsertRequest): ShopItemDto {
        validateItem(req)
        return db.tx { findShopItem(insertShopItem(req))!! }
    }

    suspend fun update(id: Int, req: ShopItemUpsertRequest): ShopItemDto {
        validateItem(req)
        return db.tx {
            if (updateShopItem(id, req) == 0) throw ApiException.notFound("Item not found")
            findShopItem(id)!!
        }
    }

    suspend fun delete(id: Int) {
        db.tx { if (deleteOrHideShopItem(id) == 0) throw ApiException.notFound("Item not found") }
    }

    private fun validateItem(req: ShopItemUpsertRequest) = validate(
        Validation.required(req.name, "Name"),
        Validation.required(req.description, "Description", 1000),
        if (req.priceCents !in 0..1_000_000) "Price must be between $0 and $10,000" else null,
        if (req.stock !in 0..100_000) "Stock must be between 0 and 100,000" else null,
        req.imageUrl?.takeIf { it.isNotBlank() && !it.startsWith("https://") }?.let { "Image URL must start with https://" },
    )
}
