package com.unyieldingmight.server.routes

import com.unyieldingmight.server.AUTH_JWT
import com.unyieldingmight.server.AUTH_RATE_LIMIT
import com.unyieldingmight.server.ApiException
import com.unyieldingmight.server.Services
import com.unyieldingmight.server.auth.CurrentUser
import com.unyieldingmight.server.auth.currentUser
import com.unyieldingmight.shared.model.AppConfigDto
import com.unyieldingmight.shared.model.ChangePasswordRequest
import com.unyieldingmight.shared.model.CheckoutRequest
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.EmailRequest
import com.unyieldingmight.shared.model.LinkMembershipRequest
import com.unyieldingmight.shared.model.LoginRequest
import com.unyieldingmight.shared.model.MembershipLookupRequest
import com.unyieldingmight.shared.model.MessageResponse
import com.unyieldingmight.shared.model.NewsTopic
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.PurchaseMembershipRequest
import com.unyieldingmight.shared.model.RegisterRequest
import com.unyieldingmight.shared.model.ResetPasswordRequest
import com.unyieldingmight.shared.model.RoleUpdateRequest
import com.unyieldingmight.shared.model.ShopItemUpsertRequest
import com.unyieldingmight.shared.model.TrainerUpsertRequest
import com.unyieldingmight.shared.model.UpdateProfileRequest
import com.unyieldingmight.shared.model.VerifyOtpRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.time.Duration
import java.time.Instant

private fun ApplicationCall.intParam(name: String): Int =
    parameters[name]?.toIntOrNull() ?: throw ApiException.badRequest("Invalid $name")

private fun ApplicationCall.optionalUserId(): Int? = principal<JWTPrincipal>()?.subject?.toIntOrNull()

private suspend fun ApplicationCall.requireAdmin(services: Services): CurrentUser {
    val user = currentUser()
    if (!services.admin.isAdmin(user.id)) throw ApiException.forbidden()
    return user
}

fun Route.publicRoutes(services: Services) {
    get("/health") { call.respond(mapOf("status" to "ok")) }

    get("/config") {
        call.respond(
            AppConfigDto(
                currency = services.currency,
                paymentMode = services.payments.mode,
                stripePublishableKey = services.payments.publishableKey,
                newsEnabled = services.news.enabled,
            )
        )
    }

    get("/trainers") { call.respond(services.admin.trainers()) }
    get("/membership-plans") { call.respond(services.users.plans()) }
    get("/shop/items") { call.respond(services.shop.items()) }
}

fun Route.authRoutes(services: Services) = rateLimit(AUTH_RATE_LIMIT) {
    route("/auth") {
        post("/login") {
            val req = call.receive<LoginRequest>()
            call.respond(services.auth.login(req.email, req.password))
        }
        post("/register/start") {
            services.auth.startRegistration(call.receive<EmailRequest>().email)
            call.respond(MessageResponse("We've sent a 6-digit code to your email."))
        }
        post("/register/verify") {
            val req = call.receive<VerifyOtpRequest>()
            call.respond(services.auth.verifyRegistration(req.email, req.code))
        }
        post("/register/membership") {
            val req = call.receive<MembershipLookupRequest>()
            call.respond(services.auth.lookupMembership(req.registrationToken, req.membershipId))
        }
        post("/register") {
            call.respond(HttpStatusCode.Created, services.auth.register(call.receive<RegisterRequest>()))
        }
        post("/password/forgot") {
            services.auth.forgotPassword(call.receive<EmailRequest>().email)
            call.respond(MessageResponse("If an account exists for that email, we've sent it a reset code."))
        }
        post("/password/reset") {
            val req = call.receive<ResetPasswordRequest>()
            services.auth.resetPassword(req.email, req.code, req.newPassword)
            call.respond(MessageResponse("Your password has been reset. You can log in now."))
        }
    }
}

fun Route.classRoutes(services: Services) = route("/classes") {
    authenticate(AUTH_JWT, optional = true) {
        get {
            val from = call.request.queryParameters["from"]?.toLongOrNull()?.let(Instant::ofEpochMilli) ?: Instant.now()
            val to = call.request.queryParameters["to"]?.toLongOrNull()?.let(Instant::ofEpochMilli)
            call.respond(services.classes.list(call.optionalUserId(), from, to))
        }
        get("/{id}") { call.respond(services.classes.get(call.intParam("id"), call.optionalUserId())) }
    }
    authenticate(AUTH_JWT) {
        get("/recommended") { call.respond(services.classes.recommended(call.currentUser().id)) }
        post("/{id}/book") { call.respond(services.classes.book(call.currentUser().id, call.intParam("id"))) }
    }
}

fun Route.shopRoutes(services: Services) = authenticate(AUTH_JWT) {
    route("/shop") {
        post("/checkout") { call.respond(services.shop.checkout(call.currentUser().id, call.receive<CheckoutRequest>())) }
        get("/orders") { call.respond(services.shop.orders(call.currentUser().id)) }
    }
    route("/payments/{id}") {
        post("/confirm") { call.respond(services.payments.confirm(call.currentUser().id, call.intParam("id"))) }
        post("/cancel") {
            services.payments.cancel(call.currentUser().id, call.intParam("id"))
            call.respond(MessageResponse("Payment cancelled"))
        }
    }
    get("/news") {
        val topic = call.request.queryParameters["topic"]?.let { runCatching { NewsTopic.valueOf(it) }.getOrNull() } ?: NewsTopic.FITNESS
        call.respond(services.news.headlines(topic))
    }
}

fun Route.meRoutes(services: Services) = authenticate(AUTH_JWT) {
    route("/me") {
        get { call.respond(services.users.me(call.currentUser().id)) }
        patch { call.respond(services.users.update(call.currentUser().id, call.receive<UpdateProfileRequest>())) }
        post("/password") {
            val req = call.receive<ChangePasswordRequest>()
            services.auth.changePassword(call.currentUser().id, req.currentPassword, req.newPassword)
            call.respond(MessageResponse("Your password has been changed."))
        }
        post("/membership/link") {
            call.respond(services.users.linkMembership(call.currentUser().id, call.receive<LinkMembershipRequest>().membershipId))
        }
        post("/membership/purchase") {
            call.respond(services.users.purchaseMembership(call.currentUser().id, call.receive<PurchaseMembershipRequest>().planId))
        }
        get("/bookings") { call.respond(services.classes.myBookings(call.currentUser().id)) }
        delete("/bookings/{id}") { call.respond(services.classes.cancelBooking(call.currentUser().id, call.intParam("id"))) }
    }
}

fun Route.adminRoutes(services: Services) = authenticate(AUTH_JWT) {
    route("/admin") {
        get("/stats") {
            call.requireAdmin(services)
            call.respond(services.admin.stats())
        }

        route("/classes") {
            get {
                call.requireAdmin(services)
                // Admins also see the last 30 days so completed/cancelled classes stay visible.
                call.respond(services.classes.list(null, Instant.now().minus(Duration.ofDays(30))))
            }
            post {
                call.requireAdmin(services)
                call.respond(HttpStatusCode.Created, services.classes.create(call.receive<ClassUpsertRequest>()))
            }
            put("/{id}") {
                call.requireAdmin(services)
                call.respond(services.classes.update(call.intParam("id"), call.receive<ClassUpsertRequest>()))
            }
            delete("/{id}") {
                call.requireAdmin(services)
                services.classes.delete(call.intParam("id"))
                call.respond(MessageResponse("Class deleted"))
            }
            post("/{id}/cancel") {
                call.requireAdmin(services)
                call.respond(services.classes.cancelClass(call.intParam("id")))
            }
            get("/{id}/bookings") {
                call.requireAdmin(services)
                call.respond(services.classes.attendees(call.intParam("id")))
            }
        }

        route("/trainers") {
            post {
                call.requireAdmin(services)
                call.respond(HttpStatusCode.Created, services.admin.createTrainer(call.receive<TrainerUpsertRequest>()))
            }
            put("/{id}") {
                call.requireAdmin(services)
                call.respond(services.admin.updateTrainer(call.intParam("id"), call.receive<TrainerUpsertRequest>()))
            }
            delete("/{id}") {
                call.requireAdmin(services)
                services.admin.deleteTrainer(call.intParam("id"))
                call.respond(MessageResponse("Trainer removed"))
            }
        }

        route("/shop/items") {
            get {
                call.requireAdmin(services)
                call.respond(services.shop.items(includeInactive = true))
            }
            post {
                call.requireAdmin(services)
                call.respond(HttpStatusCode.Created, services.shop.create(call.receive<ShopItemUpsertRequest>()))
            }
            put("/{id}") {
                call.requireAdmin(services)
                call.respond(services.shop.update(call.intParam("id"), call.receive<ShopItemUpsertRequest>()))
            }
            delete("/{id}") {
                call.requireAdmin(services)
                services.shop.delete(call.intParam("id"))
                call.respond(MessageResponse("Item removed"))
            }
        }

        route("/users") {
            get {
                call.requireAdmin(services)
                call.respond(services.admin.users(call.request.queryParameters["q"]))
            }
            put("/{id}/role") {
                val admin = call.requireAdmin(services)
                services.admin.setRole(admin.id, call.intParam("id"), call.receive<RoleUpdateRequest>().role)
                call.respond(MessageResponse("Role updated"))
            }
        }

        post("/newsletter") {
            call.requireAdmin(services)
            call.respond(services.admin.sendNewsletter(call.receive<NewsletterRequest>()))
        }
    }
}
