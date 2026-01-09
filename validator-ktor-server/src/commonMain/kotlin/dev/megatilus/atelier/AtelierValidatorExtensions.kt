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
 * This is the recommended approach for manual validation when automatic
 * validation is disabled. If validation fails, this function automatically
 * responds with an error using the configured error format and returns null.
 * If validation succeeds, it returns the validated object.
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

/**
 * Validates multiple objects and returns valid and invalid ones separately.
 *
 * This function is useful for batch processing endpoints where some items may be
 * valid while others are not. It allows you to process valid items and report
 * errors for invalid ones in a single request.
 *
 * Example:
 * ```kotlin
 * post("/users/batch") {
 *     @Serializable
 *     data class BatchRequest(val users: List<User>)
 *
 *     val request = call.receive<BatchRequest>()
 *     val (valid, invalid) = call.validateBatch(request.users)
 *
 *     if (invalid.isNotEmpty()) {
 *         call.respond(HttpStatusCode.MultiStatus, mapOf(
 *             "message" to "Some users failed validation",
 *             "validCount" to valid.size,
 *             "invalidCount" to invalid.size,
 *             "errors" to invalid.map { (user, failure) ->
 *                 mapOf(
 *                     "user" to user.name,
 *                     "errors" to failure.errors.map { it.message }
 *                 )
 *             }
 *         ))
 *         return@post
 *     }
 *
 *     userRepository.createAll(valid)
 *     call.respond(HttpStatusCode.Created, mapOf("created" to valid.size))
 * }
 * ```
 *
 * @param T The type of objects to validate
 * @param objects List of objects to validate
 * @return Pair of (valid objects, list of invalid objects paired with their validation errors)
 */
public inline fun <reified T : Any> ApplicationCall.validateBatch(
    objects: List<T>
): Pair<List<T>, List<Pair<T, ValidationResult.Failure>>> {
    val validator = getValidator<T>() ?: return Pair(emptyList(), emptyList())

    val (valid, invalidPairs) = objects
        .map { obj -> obj to validator.validate(obj) }
        .partition { (_, result) -> result is ValidationResult.Success }

    return Pair(
        valid.map { it.first },
        invalidPairs.mapNotNull { (obj, result) ->
            if (result is ValidationResult.Failure) obj to result else null
        }
    )
}
