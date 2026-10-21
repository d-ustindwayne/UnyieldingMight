package com.unyieldingmight.server.auth

import com.unyieldingmight.server.ApiException
import com.unyieldingmight.shared.model.Role
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

data class CurrentUser(val id: Int, val role: Role) {
    val isAdmin: Boolean get() = role == Role.ADMIN
}

fun ApplicationCall.currentUser(): CurrentUser {
    val principal = principal<JWTPrincipal>() ?: throw ApiException.unauthorized()
    val id = principal.subject?.toIntOrNull() ?: throw ApiException.unauthorized()
    val role = principal.getClaim(JwtService.CLAIM_ROLE, String::class)?.let { runCatching { Role.valueOf(it) }.getOrNull() }
        ?: throw ApiException.unauthorized()
    return CurrentUser(id, role)
}

fun ApplicationCall.requireAdmin(): CurrentUser =
    currentUser().also { if (!it.isAdmin) throw ApiException.forbidden() }
