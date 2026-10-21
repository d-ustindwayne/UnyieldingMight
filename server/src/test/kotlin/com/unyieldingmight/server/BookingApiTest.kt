package com.unyieldingmight.server

import com.unyieldingmight.server.repo.updateClass
import com.unyieldingmight.shared.model.ApiError
import com.unyieldingmight.shared.model.BookClassResponse
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.MessageResponse
import com.unyieldingmight.shared.model.PaymentMode
import com.unyieldingmight.shared.model.PaymentResultDto
import com.unyieldingmight.shared.model.PaymentStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookingApiTest {
    private val day = 86_400_000L

    /** A paid class starting [startsIn] ms from now with the given capacity. */
    private suspend fun TestEnv.newClass(startsIn: Long = 3 * day, capacity: Int = 10, priceCents: Int = 1500): Int {
        val start = System.currentTimeMillis() + startsIn
        return services.classes.create(
            ClassUpsertRequest("Test Class", "desc", ClassCategory.CARDIO, null, start, start + 3_600_000, capacity, 400, priceCents)
        ).id
    }

    private suspend fun HttpClient.book(classId: Int, token: String) =
        postJson("/api/classes/$classId/book", token = token)

    @Test
    fun `members book paid classes for free`() = apiTest { env, client ->
        val classId = env.newClass()
        val token = client.login(MEMBER)
        val result = client.book(classId, token).body<BookClassResponse>()
        assertNotNull(result.booking)
        assertNull(result.payment)
        assertEquals(1, env.services.classes.get(classId, null).bookedCount)
        assertTrue(env.email.sent.any { it.to == MEMBER && it.subject == "Booking Confirmed!" })
    }

    @Test
    fun `non-members pay before the spot is taken`() = apiTest { env, client ->
        val classId = env.newClass()
        val token = client.login(CUSTOMER)
        val result = client.book(classId, token).body<BookClassResponse>()
        val payment = assertNotNull(result.payment)
        assertEquals(PaymentMode.MOCK, payment.mode)
        assertEquals(1500, payment.amountCents)
        assertEquals(0, env.services.classes.get(classId, null).bookedCount)

        val confirmed = client.postJson("/api/payments/${payment.paymentId}/confirm", token = token).body<PaymentResultDto>()
        assertEquals(PaymentStatus.SUCCEEDED, confirmed.status)
        assertEquals(BookingStatus.BOOKED, confirmed.booking!!.status)

        // Confirming twice must not book twice.
        client.postJson("/api/payments/${payment.paymentId}/confirm", token = token)
        assertEquals(1, env.services.classes.get(classId, null).bookedCount)
    }

    @Test
    fun `full classes cannot be booked`() = apiTest { env, client ->
        val classId = env.newClass(capacity = 1)
        client.book(classId, client.login(MEMBER))
        val r = client.book(classId, client.login(CUSTOMER))
        assertEquals("class_full", r.body<ApiError>().code)
    }

    @Test
    fun `paying for a class that filled meanwhile is refunded`() = apiTest { env, client ->
        val classId = env.newClass(capacity = 1)
        val customer = client.login(CUSTOMER)
        val payment = client.book(classId, customer).body<BookClassResponse>().payment!!
        client.book(classId, client.login(MEMBER)) // member takes the last spot first

        val result = client.postJson("/api/payments/${payment.paymentId}/confirm", token = customer).body<PaymentResultDto>()
        assertEquals(PaymentStatus.REFUNDED, result.status)
        assertEquals(1, env.gateway.refunded.size)
    }

    @Test
    fun `cancelling well ahead refunds and frees the spot`() = apiTest { env, client ->
        val classId = env.newClass(startsIn = 3 * day)
        val token = client.login(CUSTOMER)
        val payment = client.book(classId, token).body<BookClassResponse>().payment!!
        val booking = client.postJson("/api/payments/${payment.paymentId}/confirm", token = token).body<PaymentResultDto>().booking!!

        val message = client.delete("/api/me/bookings/${booking.id}") { bearerAuth(token) }.body<MessageResponse>()
        assertTrue("refunded" in message.message)
        assertEquals(0, env.services.classes.get(classId, null).bookedCount)
        assertEquals(1, env.gateway.refunded.size)

        // The same class can be booked again afterwards.
        assertNotNull(client.book(classId, token).body<BookClassResponse>().payment)
    }

    @Test
    fun `late cancellations are not refunded`() = apiTest { env, client ->
        val classId = env.newClass(startsIn = 2 * 3_600_000L)
        val token = client.login(CUSTOMER)
        val payment = client.book(classId, token).body<BookClassResponse>().payment!!
        val booking = client.postJson("/api/payments/${payment.paymentId}/confirm", token = token).body<PaymentResultDto>().booking!!

        val message = client.delete("/api/me/bookings/${booking.id}") { bearerAuth(token) }.body<MessageResponse>()
        assertTrue("aren't refunded" in message.message)
        assertTrue(env.gateway.refunded.isEmpty())
    }

    @Test
    fun `admin cancelling a class notifies and refunds attendees`() = apiTest { env, client ->
        val classId = env.newClass()
        val customer = client.login(CUSTOMER)
        val payment = client.book(classId, customer).body<BookClassResponse>().payment!!
        client.postJson("/api/payments/${payment.paymentId}/confirm", token = customer)
        client.book(classId, client.login(MEMBER))

        val r = client.postJson("/api/admin/classes/$classId/cancel", token = client.login(ADMIN))
        assertEquals(HttpStatusCode.OK, r.status)
        assertEquals(1, env.gateway.refunded.size)
        assertEquals(2, env.email.sent.count { it.subject == "Urgent: Class Cancellation Notice" })

        val bookings = client.get("/api/me/bookings") { bearerAuth(customer) }.body<List<BookingDto>>()
        assertEquals(BookingStatus.CLASS_CANCELLED, bookings.single { it.gymClass.id == classId }.status)
    }

    @Test
    fun `finished classes are completed and attendees thanked`() = apiTest { env, client ->
        val classId = env.newClass()
        client.book(classId, client.login(MEMBER))
        // Move the class into the past.
        val past = System.currentTimeMillis() - 2 * 3_600_000L
        env.db.tx {
            updateClass(classId, ClassUpsertRequest("Test Class", "desc", ClassCategory.CARDIO, null, past, past + 3_600_000, 10, 400, 1500))
        }
        assertTrue(env.services.classes.completeFinishedClasses() >= 1)
        val bookings = client.get("/api/me/bookings") { bearerAuth(client.login(MEMBER)) }.body<List<BookingDto>>()
        assertEquals(BookingStatus.ATTENDED, bookings.single { it.gymClass.id == classId }.status)
        assertTrue(env.email.sent.any { it.subject == "Class Completed: Well Done!" })
    }

    @Test
    fun `listing shows which classes the viewer booked`() = apiTest { env, client ->
        val classId = env.newClass()
        val token = client.login(MEMBER)
        client.book(classId, token)
        val classes = client.get("/api/classes") { bearerAuth(token) }.body<List<GymClassDto>>()
        assertTrue(classes.single { it.id == classId }.bookedByMe)
        val anonymous = client.get("/api/classes").body<List<GymClassDto>>()
        assertTrue(anonymous.none { it.bookedByMe })
    }

    @Test
    fun `recommendations exclude booked classes`() = apiTest { env, client ->
        val token = client.login(CUSTOMER)
        val recs = client.get("/api/classes/recommended") { bearerAuth(token) }
            .body<List<com.unyieldingmight.shared.model.RecommendationDto>>()
        assertTrue(recs.isNotEmpty())
        assertTrue(recs.all { !it.gymClass.bookedByMe && it.gymClass.startsAt > System.currentTimeMillis() })
    }
}
