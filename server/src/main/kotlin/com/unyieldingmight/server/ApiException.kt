package com.unyieldingmight.server

import io.ktor.http.HttpStatusCode

/** Thrown by services; StatusPages turns it into an [com.unyieldingmight.shared.model.ApiError] response. */
class ApiException(val status: HttpStatusCode, val code: String, override val message: String) : RuntimeException(message) {
    companion object {
        fun badRequest(message: String, code: String = "bad_request") = ApiException(HttpStatusCode.BadRequest, code, message)
        fun unauthorized(message: String = "Please log in again", code: String = "unauthorized") = ApiException(HttpStatusCode.Unauthorized, code, message)
        fun forbidden(message: String = "You don't have permission to do that") = ApiException(HttpStatusCode.Forbidden, "forbidden", message)
        fun notFound(message: String) = ApiException(HttpStatusCode.NotFound, "not_found", message)
        fun conflict(message: String, code: String = "conflict") = ApiException(HttpStatusCode.Conflict, code, message)
        fun unavailable(message: String) = ApiException(HttpStatusCode.ServiceUnavailable, "unavailable", message)
    }
}

/** Runs a shared validator (which returns an error message or null) and throws a 400 if it fails. */
fun validate(vararg errors: String?) {
    errors.firstOrNull { it != null }?.let { throw ApiException.badRequest(it, "validation") }
}
