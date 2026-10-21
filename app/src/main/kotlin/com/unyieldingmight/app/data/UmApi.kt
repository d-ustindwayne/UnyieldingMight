package com.unyieldingmight.app.data

import com.unyieldingmight.app.BuildConfig
import com.unyieldingmight.shared.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/** A failed API call, already translated into a message that can be shown to the user. */
class ApiException(
    override val message: String,
    val code: String = "error",
    val status: Int = 0,
) : Exception(message) {
    val isUnauthorized: Boolean get() = status == HttpStatusCode.Unauthorized.value
}

/**
 * Typed client for the Unyielding Might API. Every endpoint returns shared DTOs, so the app and
 * server can never disagree about a field name.
 */
class UmApi(
    private val tokenProvider: () -> String?,
    private val onUnauthorized: () -> Unit,
    engine: HttpClientEngine = OkHttp.create(),
    baseUrl: String = BuildConfig.API_BASE_URL,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val http = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 8_000
            requestTimeoutMillis = 20_000
        }
        defaultRequest {
            url(baseUrl.trimEnd('/') + "/api/")
            contentType(ContentType.Application.Json)
        }
    }

    private suspend inline fun <reified T> call(authenticated: Boolean = true, block: HttpClient.(HttpRequestBuilder.() -> Unit) -> HttpResponse): T {
        val response = try {
            http.block { if (authenticated) tokenProvider()?.let { bearerAuth(it) } }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ApiException("Can't reach Unyielding Might right now. Check your connection and try again.", "network")
        }
        if (response.status.isSuccess()) return response.body()

        val error = runCatching { response.body<ApiError>() }.getOrNull()
        if (response.status == HttpStatusCode.Unauthorized && authenticated && tokenProvider() != null && error?.code == "unauthorized") {
            onUnauthorized()
        }
        throw ApiException(error?.message ?: "Something went wrong (${response.status.value}).", error?.code ?: "http", response.status.value)
    }

    // ---------- Public ----------
    suspend fun config(): AppConfigDto = call(false) { get("config", it) }
    suspend fun trainers(): List<TrainerDto> = call(false) { get("trainers", it) }
    suspend fun membershipPlans(): List<MembershipPlanDto> = call(false) { get("membership-plans", it) }
    suspend fun shopItems(): List<ShopItemDto> = call(false) { get("shop/items", it) }

    // ---------- Auth ----------
    suspend fun login(email: String, password: String): AuthResponse =
        call(false) { post("auth/login") { it(); setBody(LoginRequest(email.trim(), password)) } }
    suspend fun startRegistration(email: String): MessageResponse =
        call(false) { post("auth/register/start") { it(); setBody(EmailRequest(email.trim())) } }
    suspend fun verifyRegistration(email: String, code: String): RegistrationTicket =
        call(false) { post("auth/register/verify") { it(); setBody(VerifyOtpRequest(email.trim(), code)) } }
    suspend fun lookupMembership(token: String, membershipId: Int): MembershipPreview =
        call(false) { post("auth/register/membership") { it(); setBody(MembershipLookupRequest(token, membershipId)) } }
    suspend fun register(request: RegisterRequest): AuthResponse =
        call(false) { post("auth/register") { it(); setBody(request) } }
    suspend fun forgotPassword(email: String): MessageResponse =
        call(false) { post("auth/password/forgot") { it(); setBody(EmailRequest(email.trim())) } }
    suspend fun resetPassword(email: String, code: String, newPassword: String): MessageResponse =
        call(false) { post("auth/password/reset") { it(); setBody(ResetPasswordRequest(email.trim(), code, newPassword)) } }

    // ---------- Me ----------
    suspend fun me(): UserDto = call { get("me", it) }
    suspend fun updateProfile(request: UpdateProfileRequest): UserDto = call { patch("me") { it(); setBody(request) } }
    suspend fun changePassword(current: String, new: String): MessageResponse =
        call { post("me/password") { it(); setBody(ChangePasswordRequest(current, new)) } }
    suspend fun linkMembership(membershipId: Int): UserDto =
        call { post("me/membership/link") { it(); setBody(LinkMembershipRequest(membershipId)) } }
    suspend fun purchaseMembership(planId: Int): PaymentIntentDto =
        call { post("me/membership/purchase") { it(); setBody(PurchaseMembershipRequest(planId)) } }
    suspend fun myBookings(): List<BookingDto> = call { get("me/bookings", it) }
    suspend fun cancelBooking(bookingId: Int): MessageResponse = call { delete("me/bookings/$bookingId", it) }

    // ---------- Classes ----------
    suspend fun classes(from: Long? = null, to: Long? = null): List<GymClassDto> = call {
        get("classes") { it(); from?.let { f -> parameter("from", f) }; to?.let { t -> parameter("to", t) } }
    }
    suspend fun gymClass(id: Int): GymClassDto = call { get("classes/$id", it) }
    suspend fun recommendedClasses(): List<RecommendationDto> = call { get("classes/recommended", it) }
    suspend fun bookClass(id: Int): BookClassResponse = call { post("classes/$id/book", it) }

    // ---------- Shop & payments ----------
    suspend fun checkout(lines: List<CartLine>): CheckoutResponse = call { post("shop/checkout") { it(); setBody(CheckoutRequest(lines)) } }
    suspend fun orders(): List<OrderDto> = call { get("shop/orders", it) }
    suspend fun confirmPayment(paymentId: Int): PaymentResultDto = call { post("payments/$paymentId/confirm", it) }
    suspend fun cancelPayment(paymentId: Int): MessageResponse = call { post("payments/$paymentId/cancel", it) }

    // ---------- News ----------
    suspend fun news(topic: NewsTopic): List<NewsArticleDto> = call { get("news") { it(); parameter("topic", topic.name) } }

    // ---------- Admin ----------
    suspend fun adminStats(): AdminStatsDto = call { get("admin/stats", it) }
    suspend fun adminClasses(): List<GymClassDto> = call { get("admin/classes", it) }
    suspend fun createClass(request: ClassUpsertRequest): GymClassDto = call { post("admin/classes") { it(); setBody(request) } }
    suspend fun updateClass(id: Int, request: ClassUpsertRequest): GymClassDto = call { put("admin/classes/$id") { it(); setBody(request) } }
    suspend fun deleteClass(id: Int): MessageResponse = call { delete("admin/classes/$id", it) }
    suspend fun cancelClass(id: Int): MessageResponse = call { post("admin/classes/$id/cancel", it) }
    suspend fun classAttendees(id: Int): List<AdminBookingDto> = call { get("admin/classes/$id/bookings", it) }
    suspend fun createTrainer(request: TrainerUpsertRequest): TrainerDto = call { post("admin/trainers") { it(); setBody(request) } }
    suspend fun updateTrainer(id: Int, request: TrainerUpsertRequest): TrainerDto = call { put("admin/trainers/$id") { it(); setBody(request) } }
    suspend fun deleteTrainer(id: Int): MessageResponse = call { delete("admin/trainers/$id", it) }
    suspend fun adminShopItems(): List<ShopItemDto> = call { get("admin/shop/items", it) }
    suspend fun createShopItem(request: ShopItemUpsertRequest): ShopItemDto = call { post("admin/shop/items") { it(); setBody(request) } }
    suspend fun updateShopItem(id: Int, request: ShopItemUpsertRequest): ShopItemDto = call { put("admin/shop/items/$id") { it(); setBody(request) } }
    suspend fun deleteShopItem(id: Int): MessageResponse = call { delete("admin/shop/items/$id", it) }
    suspend fun adminUsers(query: String?): List<AdminUserDto> = call { get("admin/users") { it(); query?.let { q -> parameter("q", q) } } }
    suspend fun setRole(userId: Int, role: Role): MessageResponse = call { put("admin/users/$userId/role") { it(); setBody(RoleUpdateRequest(role)) } }
    suspend fun sendNewsletter(request: NewsletterRequest): NewsletterResult = call { post("admin/newsletter") { it(); setBody(request) } }
}
