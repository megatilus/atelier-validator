/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

/**
 * Receives and validates a request body in one call.
 *
 * If validation fails, this function automatically responds with an error using the
 * configured error format and returns null. If validation succeeds, it returns the
 * validated object.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User>() ?: return@post
 *     // user is guaranteed to be valid here
 *     userRepository.create(user)
 *     call.respond(HttpStatusCode.Created, user)
 * }
 * ```
 *
 * @param T The type of object to receive and validate
 * @return The validated object, or null if validation failed (error response already sent)
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(): T? {
    val obj = receive<T>()

    val validator = getValidator<T>()
    if (validator == null) {
        respond(
            HttpStatusCode.InternalServerError,
            "No validator registered for ${T::class.simpleName}"
        )
        return null
    }

    return when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> obj

        is ValidationResult.Failure -> {
            respondValidationError(result)
            null
        }
    }
}

/**
 * Receives and validates a request body with a custom error handler.
 *
 * Use this function when you need custom error handling logic for a specific endpoint.
 * The error handler is invoked if validation fails, allowing you to provide
 * context-specific error responses.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         if (failure.hasErrorFor("email")) {
 *             call.respond(HttpStatusCode.Conflict, mapOf(
 *                 "error" to "Email already exists or is invalid"
 *             ))
 *         } else {
 *             call.respondValidationError(failure)
 *         }
 *     } ?: return@post
 *
 *     userRepository.create(user)
 *     call.respond(HttpStatusCode.Created, user)
 * }
 * ```
 *
 * You can also throw exceptions from the error handler:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         // Throw if you want exception-based error handling
 *         throw AtelierValidationException(failure)
 *     } ?: return@post
 *
 *     userRepository.create(user)
 *     call.respond(HttpStatusCode.Created, user)
 * }
 * ```
 *
 * @param T The type of object to receive and validate
 * @param onError Custom error handler invoked when validation fails
 * @return The validated object, or null if validation failed
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(
    noinline onError: suspend (ValidationResult.Failure) -> Unit
): T? {
    val obj = receive<T>()

    val validator = getValidator<T>()
    if (validator == null) {
        respond(
            HttpStatusCode.InternalServerError,
            "No validator registered for ${T::class.simpleName}"
        )
        return null
    }

    return when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> obj

        is ValidationResult.Failure -> {
            onError(result)
            null
        }
    }
}
