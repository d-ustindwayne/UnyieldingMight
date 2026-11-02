package com.unyieldingmight.server

import com.unyieldingmight.shared.model.ActiveUpdateRequest
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.AdminCreateUserRequest
import com.unyieldingmight.shared.model.AdminUpdateUserRequest
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.ApiError
import com.unyieldingmight.shared.model.CartLine
import com.unyieldingmight.shared.model.CheckoutRequest
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.ImageUploadResponse
import com.unyieldingmight.shared.model.ShopItemDto
import com.unyieldingmight.shared.model.UserDto
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminAccountsApiTest {

    private val address = AddressDto("1 Queen Street", "Auckland", "Auckland", "1010", "New Zealand")

    private fun newMember(email: String, password: String?) = AdminCreateUserRequest(
        email, "Front", "Desk", "1990-01-01", Gender.OTHER, address, 170.0, 70.0, ActivityLevel.LIGHT, password,
    )

    @Test
    fun `admin creates an account with a temporary password the member can log in with`() = apiTest { env, client ->
        val admin = client.login(ADMIN)
        val r = client.postJson("/api/admin/users", newMember("walkin@example.com", "TempPass123"), admin)
        assertEquals(HttpStatusCode.Created, r.status)
        assertEquals("walkin@example.com", r.body<UserDto>().email)
        assertTrue(client.login("walkin@example.com", "TempPass123").isNotBlank())
        assertTrue(env.email.sent.none { it.to == "walkin@example.com" })
    }

    @Test
    fun `without a password the member is emailed how to set one`() = apiTest { env, client ->
        val r = client.postJson("/api/admin/users", newMember("emailed@example.com", null), client.login(ADMIN))
        assertEquals(HttpStatusCode.Created, r.status)
        assertTrue(env.email.sent.any { it.to == "emailed@example.com" && it.subject.contains("account is ready") })
    }

    @Test
    fun `admin edits a member's details`() = apiTest { _, client ->
        val admin = client.login(ADMIN)
        val id = client.postJson("/api/admin/users", newMember("edit@example.com", "TempPass123"), admin).body<UserDto>().id
        val updated = client.put("/api/admin/users/$id") {
            bearerAuth(admin); contentType(ContentType.Application.Json)
            setBody(AdminUpdateUserRequest("renamed@example.com", "New", "Name", "1991-02-03", Gender.FEMALE, address, 165.0, 60.0, ActivityLevel.MODERATE))
        }.body<UserDto>()
        assertEquals("renamed@example.com", updated.email)
        assertEquals("New", updated.firstName)

        // Another account's email can't be taken.
        val clash = client.put("/api/admin/users/$id") {
            bearerAuth(admin); contentType(ContentType.Application.Json)
            setBody(AdminUpdateUserRequest(CUSTOMER, "New", "Name", "1991-02-03", Gender.FEMALE, address))
        }
        assertEquals(HttpStatusCode.Conflict, clash.status)
    }

    @Test
    fun `deactivated accounts can't log in and lose their open sessions, but can be reactivated`() = apiTest { _, client ->
        val admin = client.login(ADMIN)
        val customerToken = client.login(CUSTOMER)
        val customerId = client.get("/api/admin/users") { bearerAuth(admin) }.body<List<AdminUserDto>>().first { it.email == CUSTOMER }.id

        fun setActive(active: Boolean) = suspend {
            client.put("/api/admin/users/$customerId/active") {
                bearerAuth(admin); contentType(ContentType.Application.Json); setBody(ActiveUpdateRequest(active))
            }
        }
        assertEquals(HttpStatusCode.OK, setActive(false)().status)

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/me") { bearerAuth(customerToken) }.status)
        val login = client.postJson("/api/auth/login", com.unyieldingmight.shared.model.LoginRequest(CUSTOMER, com.unyieldingmight.server.db.DevSeeder.DEMO_PASSWORD))
        assertEquals("account_deactivated", login.body<ApiError>().code)

        val active = client.get("/api/admin/users") { bearerAuth(admin) }.body<List<AdminUserDto>>()
        val deactivated = client.get("/api/admin/users?deactivated=true") { bearerAuth(admin) }.body<List<AdminUserDto>>()
        assertTrue(active.none { it.id == customerId })
        assertTrue(deactivated.single().id == customerId)

        setActive(true)()
        assertTrue(client.login(CUSTOMER).isNotBlank())
    }

    @Test
    fun `admins can't deactivate themselves`() = apiTest { _, client ->
        val admin = client.login(ADMIN)
        val adminId = client.get("/api/me") { bearerAuth(admin) }.body<UserDto>().id
        val r = client.put("/api/admin/users/$adminId/active") {
            bearerAuth(admin); contentType(ContentType.Application.Json); setBody(ActiveUpdateRequest(false))
        }
        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `admin accounts can't book classes or shop`() = apiTest { env, client ->
        val admin = client.login(ADMIN)
        val classId = env.services.classes.list(null, java.time.Instant.now()).first().id
        assertEquals("admin_account", client.postJson("/api/classes/$classId/book", token = admin).body<ApiError>().code)

        val item = client.get("/api/shop/items").body<List<ShopItemDto>>().first()
        val checkout = client.postJson("/api/shop/checkout", CheckoutRequest(listOf(CartLine(item.id, 1))), admin)
        assertEquals(HttpStatusCode.Forbidden, checkout.status)
    }

    @Test
    fun `photos that nothing uses are cleaned up after a day`() = apiTest { env, _ ->
        val admin = env.services.admin
        val used = admin.uploadImage("image/jpeg", byteArrayOf(1)).url
        val unused = admin.uploadImage("image/jpeg", byteArrayOf(2)).url
        val itemId = env.services.shop.items().first().id
        env.db.tx { update("UPDATE shop_item SET image_url = ? WHERE id = ?", used, itemId) }

        // Fresh uploads are kept, in case the admin is still filling in the form.
        assertEquals(0, admin.deleteUnusedImages())
        assertEquals(1, admin.deleteUnusedImages(java.time.Instant.now().plus(java.time.Duration.ofDays(2))))
        admin.image(used.substringAfterLast('/').toInt())
        assertTrue(runCatching { admin.image(unused.substringAfterLast('/').toInt()) }.isFailure)
    }

    @Test
    fun `uploaded photos are stored and served back`() = apiTest { _, client ->
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 1, 2, 3)
        val r = client.post("/api/admin/images") {
            bearerAuth(client.login(ADMIN)); contentType(ContentType.Image.JPEG); setBody(jpeg)
        }
        assertEquals(HttpStatusCode.Created, r.status)
        val url = r.body<ImageUploadResponse>().url
        val served = client.get(url)
        assertEquals(ContentType.Image.JPEG, served.contentType()?.withoutParameters())
        assertContentEquals(jpeg, served.readRawBytes())

        val text = client.post("/api/admin/images") {
            bearerAuth(client.login(ADMIN)); contentType(ContentType.Text.Plain); setBody("not a photo")
        }
        assertEquals(HttpStatusCode.BadRequest, text.status)

        val customer = client.post("/api/admin/images") {
            bearerAuth(client.login(CUSTOMER)); contentType(ContentType.Image.JPEG); setBody(jpeg)
        }
        assertEquals(HttpStatusCode.Forbidden, customer.status)
    }
}
