/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.ktor.server

import dev.megatilus.atelier.validator.results.ErrorDetail
import dev.megatilus.atelier.validator.results.ValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/**
 * Data transfer object for a single validation error.
 *
 * Serializable presentation layer wrapper around [ErrorDetail] from the core.
 *
 * @property field The name of the field that failed validation
 * @property message Human-readable error message
 * @property code The validation error code (e.g., "required", "invalid_email")
 * @property value The actual value that failed validation
 */
@Serializable
public data class ValidationErrorDto(
    val field: String,
    val message: String,
    val code: String,
    val value: String?
) {
    public companion object {
        /**
         * Creates a DTO from an [ErrorDetail] produced by the core.
         */
        public fun from(detail: ErrorDetail): ValidationErrorDto = ValidationErrorDto(
            field = detail.field,
            message = detail.message,
            code = detail.code,
            value = detail.actualValue
        )
    }
}

/**
 * Standard validation error response format for Ktor server responses.
 *
 * @property message A general message indicating validation failure
 * @property errors List of detailed validation errors per field
 */
@Serializable
public data class AtelierValidationErrorResponse(
    val message: String,
    val errors: List<ValidationErrorDto>
) {
    public companion object {
        /**
         * Creates a response from a [ValidationResult.Failure].
         *
         * Delegates to [ValidationResult.Failure.toDetailedList] from the core
         * to avoid duplicating error mapping logic.
         */
        public fun from(failure: ValidationResult.Failure): AtelierValidationErrorResponse =
            AtelierValidationErrorResponse(
                message = "Request validation failed: ${failure.errorCount} error(s) detected",
                errors = failure.toDetailedList().map { ValidationErrorDto.from(it) }
            )
    }
}

/**
 * Checks if a validation failure contains an error for the specified field.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         if (failure.hasErrorFor("email")) {
 *             call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email validation failed"))
 *         } else {
 *             call.respondValidationError(failure)
 *         }
 *     } ?: return@post
 * }
 * ```
 *
 * @param fieldName The name of the field to check
 * @return true if the field has validation errors, false otherwise
 */
public fun ValidationResult.Failure.hasErrorFor(fieldName: String): Boolean =
    errorsFor(fieldName).isNotEmpty()

/**
 * Sends a validation error response using the configured error format.
 *
 * Uses the [AtelierValidatorServerConfig.errorResponseBuilder] if the plugin
 * is installed, otherwise falls back to the default [AtelierValidationErrorResponse] format.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receive<User>()
 *     when (val result = call.getValidator<User>()?.validate(user)) {
 *         is ValidationResult.Failure -> {
 *             call.respondValidationError(result)
 *             return@post
 *         }
 *         else -> { /* process valid user */ }
 *     }
 * }
 * ```
 *
 * @param failure The validation failure containing error details
 * @param status Optional HTTP status code override (defaults to 400 Bad Request)
 */
public suspend fun ApplicationCall.respondValidationError(
    failure: ValidationResult.Failure,
    status: HttpStatusCode? = null
) {
    val config = attributes.getOrNull(AtelierValidatorServerConfigKey)
        ?: application.attributes.getOrNull(AtelierValidatorServerConfigKey)

    val responseStatus = status ?: HttpStatusCode.BadRequest
    val errorResponse = config?.errorResponseBuilder?.invoke(failure)
        ?: AtelierValidationErrorResponse.from(failure)

    if (!response.isCommitted) {
        respond(responseStatus, errorResponse)
    }
}
