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
 * If validation fails, automatically responds with an error and returns null.
 * If validation succeeds, returns the validated object.
 *
 * This is the recommended approach for manual validation when automatic
 * validation is disabled.
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
 * @return The validated object, or null if validation failed
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(): T? {
    val obj = receive<T>()

    val validator = getValidator<T>()
    if (validator == null) {
        respond(HttpStatusCode.InternalServerError, "No validator registered for ${T::class.simpleName}")
        return null
    }

    when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> return obj

        is ValidationResult.Failure -> {
            respondValidationError(result)
            return null
        }
    }
}

/**
 * Receives and validates a request body with a custom error handler.
 *
 * Use this when you need custom error handling logic for a specific endpoint.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         if (failure.hasErrorFor("email")) {
 *             call.respond(HttpStatusCode.Conflict, mapOf(
 *                 "error" to "Email already exists"
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
 * @param onError Custom error handler for validation failures
 * @return The validated object, or null if validation failed
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidate(
    crossinline onError: suspend ApplicationCall.(ValidationResult.Failure) -> Unit
): T? {
    val obj = receive<T>()

    val validator = getValidator<T>()
    if (validator == null) {
        respond(HttpStatusCode.InternalServerError, "No validator registered for ${T::class.simpleName}")
        return null
    }

    when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> return obj

        is ValidationResult.Failure -> {
            onError(result)
            return null
        }
    }
}

/**
 * Validates multiple objects and returns valid and invalid ones separately.
 *
 * Useful for batch processing endpoints where some items may be valid
 * while others are not.
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
 *         call.respond(HttpStatusCode.BadRequest, mapOf(
 *             "message" to "Some users are invalid",
 *             "validCount" to valid.size,
 *             "invalidCount" to invalid.size,
 *             "errors" to invalid.map { it.second }
 *         ))
 *         return@post
 *     }
 *
 *     userRepository.createAll(valid)
 *     call.respond(HttpStatusCode.Created, valid)
 * }
 * ```
 *
 * @param objects List of objects to validate
 * @return Pair of (valid objects, invalid objects with their validation errors)
 */
public inline fun <reified T : Any> ApplicationCall.validateBatch(
    objects: List<T>
): Pair<List<T>, List<Pair<T, ValidationResult.Failure>>> {
    val validator = getValidator<T>() ?: return Pair(emptyList(), emptyList())

    val valid = mutableListOf<T>()
    val invalid = mutableListOf<Pair<T, ValidationResult.Failure>>()

    objects.forEach { obj ->
        when (val result = validator.validate(obj)) {
            is ValidationResult.Success -> valid.add(obj)
            is ValidationResult.Failure -> invalid.add(obj to result)
        }
    }

    return Pair(valid, invalid)
}
