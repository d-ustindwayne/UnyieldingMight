package com.unyieldingmight.server

import com.unyieldingmight.server.db.Migrations
import com.unyieldingmight.shared.model.AdminStatsDto
import com.unyieldingmight.shared.model.ApiError
import com.unyieldingmight.shared.model.CartLine
import com.unyieldingmight.shared.model.CheckoutRequest
import com.unyieldingmight.shared.model.CheckoutResponse
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.NewsletterResult
import com.unyieldingmight.shared.model.NewsletterType
import com.unyieldingmight.shared.model.OrderDto
import com.unyieldingmight.shared.model.OrderStatus
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import com.unyieldingmight.shared.model.PurchaseMembershipRequest
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.RoleUpdateRequest
import com.unyieldingmight.shared.model.ShopCategory
import com.unyieldingmight.shared.model.ShopItemDto
import com.unyieldingmight.shared.model.ShopItemUpsertRequest
import com.unyieldingmight.shared.model.UserDto
import com.unyieldingmight.shared.model.AdminUserDto
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShopAndAdminApiTest {

    @Test
    fun `checkout charges database prices and takes stock on payment`() = apiTest { env, client ->
        val token = client.login(CUSTOMER)
        val items = client.get("/api/shop/items").body<List<ShopItemDto>>()
        val tee = items.first { it.name == "UM Performance Tee" }

        val checkout = client.postJson("/api/shop/checkout", CheckoutRequest(listOf(CartLine(tee.id, 2))), token).body<CheckoutResponse>()
        assertEquals(tee.priceCents * 2, checkout.order.totalCents)
        assertEquals(tee.priceCents * 2, checkout.payment.amountCents)

        val result = client.postJson("/api/payments/${checkout.payment.paymentId}/confirm", token = token).body<PaymentResultDto>()
        assertEquals(OrderStatus.PAID, result.order!!.status)
        val after = client.get("/api/shop/items").body<List<ShopItemDto>>().first { it.id == tee.id }
        assertEquals(tee.stock - 2, after.stock)

        val orders = client.get("/api/shop/orders") { bearerAuth(token) }.body<List<OrderDto>>()
        assertEquals(1, orders.size)
        assertTrue(env.email.sent.any { it.subject.startsWith("Your Unyielding Might order") })
    }

    @Test
    fun `checkout refuses more than is in stock`() = apiTest { _, client ->
        val token = client.login(CUSTOMER)
        val towel = client.get("/api/shop/items").body<List<ShopItemDto>>().first { it.name == "Gym Towel" }
        val r = client.postJson("/api/shop/checkout", CheckoutRequest(listOf(CartLine(towel.id, towel.stock + 1))), token)
        assertEquals("out_of_stock", r.body<ApiError>().code)
    }

    @Test
    fun `abandoned checkout cancels the order`() = apiTest { _, client ->
        val token = client.login(CUSTOMER)
        val item = client.get("/api/shop/items").body<List<ShopItemDto>>().first()
        val checkout = client.postJson("/api/shop/checkout", CheckoutRequest(listOf(CartLine(item.id, 1))), token).body<CheckoutResponse>()
        client.postJson("/api/payments/${checkout.payment.paymentId}/cancel", token = token)
        val result = client.postJson("/api/payments/${checkout.payment.paymentId}/confirm", token = token).body<PaymentResultDto>()
        assertEquals(PaymentStatus.CANCELLED, result.status)
    }

    @Test
    fun `buying a membership activates it`() = apiTest { _, client ->
        val token = client.login(CUSTOMER)
        val intent = client.postJson("/api/me/membership/purchase", PurchaseMembershipRequest(1), token).body<PaymentIntentDto>()
        val result = client.postJson("/api/payments/${intent.paymentId}/confirm", token = token).body<PaymentResultDto>()
        assertNotNull(result.membership)
        val me = client.get("/api/me") { bearerAuth(token) }.body<UserDto>()
        assertTrue(me.isMember)
        assertEquals("Monthly", me.membership!!.planName)
    }

    @Test
    fun `customers cannot use admin endpoints`() = apiTest { _, client ->
        val r = client.get("/api/admin/stats") { bearerAuth(client.login(CUSTOMER)) }
        assertEquals(HttpStatusCode.Forbidden, r.status)
    }

    @Test
    fun `admin manages shop items`() = apiTest { _, client ->
        val admin = client.login(ADMIN)
        val created = client.postJson(
            "/api/admin/shop/items",
            ShopItemUpsertRequest("Resistance Bands", "Set of 5", ShopCategory.EQUIPMENT, 2900, 10, null),
            admin,
        ).body<ShopItemDto>()
        assertTrue(client.get("/api/shop/items").body<List<ShopItemDto>>().any { it.id == created.id })

        val bad = client.postJson("/api/admin/shop/items", ShopItemUpsertRequest("", "x", ShopCategory.EQUIPMENT, 100, 1, null), admin)
        assertEquals(HttpStatusCode.BadRequest, bad.status)
    }

    @Test
    fun `role changes apply immediately and admins cannot demote themselves`() = apiTest { _, client ->
        val admin = client.login(ADMIN)
        val users = client.get("/api/admin/users") { bearerAuth(admin) }.body<List<AdminUserDto>>()
        val customerId = users.first { it.email == CUSTOMER }.id
        val adminId = users.first { it.email == ADMIN }.id

        val customerToken = client.login(CUSTOMER)
        client.put("/api/admin/users/$customerId/role") {
            bearerAuth(admin); contentType(ContentType.Application.Json); setBody(RoleUpdateRequest(Role.ADMIN))
        }
        // The customer's existing token now has admin access, because the role is checked in the database.
        assertEquals(HttpStatusCode.OK, client.get("/api/admin/stats") { bearerAuth(customerToken) }.status)

        val self = client.put("/api/admin/users/$adminId/role") {
            bearerAuth(admin); contentType(ContentType.Application.Json); setBody(RoleUpdateRequest(Role.CUSTOMER))
        }
        assertEquals(HttpStatusCode.BadRequest, self.status)
    }

    @Test
    fun `newsletter goes to opted-in users`() = apiTest { env, client ->
        val result = client.postJson(
            "/api/admin/newsletter",
            NewsletterRequest(NewsletterType.UPDATES, "New rowers have arrived", listOf("6 new rowers", "Longer weekend hours")),
            client.login(ADMIN),
        ).body<NewsletterResult>()
        assertEquals(3, result.recipients)
        assertTrue(env.email.sent.any { it.subject == "New Update Alert!" && "6 new rowers" in it.htmlBody })
    }

    @Test
    fun `admin stats`() = apiTest { _, client ->
        val stats = client.get("/api/admin/stats") { bearerAuth(client.login(ADMIN)) }.body<AdminStatsDto>()
        assertEquals(3, stats.totalUsers)
        assertEquals(1, stats.activeMembers)
        assertTrue(stats.upcomingClasses > 0)
        assertEquals(2, stats.lowStockItems)
    }

    @Test
    fun `sql splitter ignores comments`() {
        val statements = Migrations.splitStatements("-- header; ignored\nCREATE TABLE a (id INT);\n\nCREATE TABLE b (id INT); -- trailing")
        assertEquals(listOf("CREATE TABLE a (id INT)", "CREATE TABLE b (id INT)"), statements)
    }
}
