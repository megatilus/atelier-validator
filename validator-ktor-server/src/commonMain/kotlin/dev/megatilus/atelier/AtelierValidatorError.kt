/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/**
 * Data transfer object for validation errors.
 *
 * This class represents a single validation error in a format suitable for
 * JSON serialization. It is separated from the core domain model ([ValidationErrorDetail])
 * to maintain clean architecture and allow for customized API responses.
 *
 * @property field The name of the field that failed validation
 * @property message Human-readable error message describing the validation failure
 * @property code The validation error code (e.g., "REQUIRED", "INVALID_FORMAT")
 * @property value The actual value that failed validation (as a string)
 */
@Serializable
public data class ValidationErrorDetailDto(
    val field: String,
    val message: String,
    val code: String,
    val value: String
) {
    public companion object {
        /**
         * Creates a DTO from a domain validation error detail.
         *
         * @param error The validation error detail from the domain model
         * @return A serializable DTO representation of the error
         */
        public fun from(error: ValidationErrorDetail): ValidationErrorDetailDto {
            return ValidationErrorDetailDto(
                field = error.fieldName,
                message = error.message,
                code = error.code.toString(),
                value = error.actualValue
            )
        }
    }
}

/**
 * Standard validation error response format.
 *
 * This is the default error format returned when validation fails.
 * It provides a consistent API response structure for validation errors.
 *
 * @property message A general message indicating validation failure
 * @property errors List of detailed validation errors for each field
 */
@Serializable
public data class AtelierValidationErrorResponse(
    val message: String,
    val errors: List<ValidationErrorDetailDto>
) {
    public companion object {
        /**
         * Creates an error response from a validation failure.
         *
         * @param failure The validation failure containing error details
         * @return A standardized validation error response
         */
        public fun from(failure: ValidationResult.Failure): AtelierValidationErrorResponse {
            return AtelierValidationErrorResponse(
                message = "Request validation failed: ${failure.errorCount} error(s) detected",
                errors = failure.errors.map { ValidationErrorDetailDto.from(it) }
            )
        }
    }
}

/**
 * Checks if a validation failure contains an error for the specified field.
 *
 * This is a convenience extension that makes it easy to check for field-specific
 * errors in custom error handlers.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         if (failure.hasErrorFor("email")) {
 *             call.respond(HttpStatusCode.Conflict, mapOf(
 *                 "error" to "Email validation failed"
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
 * @param fieldName The name of the field to check
 * @return true if the field has validation errors, false otherwise
 */
public fun ValidationResult.Failure.hasErrorFor(fieldName: String): Boolean {
    return errorsFor(fieldName).isNotEmpty()
}

/**
 * Sends a validation error response in the standard format.
 *
 * This function automatically retrieves the validator configuration from the application
 * context and uses it to format and send the error response. The response includes
 * detailed validation errors and uses the configured status code.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     val user = call.receive<User>()
 *     val validator = call.getValidator<User>()
 *
 *     when (val result = validator?.validate(user)) {
 *         is ValidationResult.Failure -> {
 *             call.respondValidationError(result)
 *             return@post
 *         }
 *         else -> {
 *             // Process valid user
 *         }
 *     }
 * }
 * ```
 *
 * @param failure The validation failure containing error details
 * @param status Optional HTTP status code to override the configured default
 */
public suspend fun ApplicationCall.respondValidationError(
    failure: ValidationResult.Failure,
    status: HttpStatusCode? = null
) {
    val config = attributes.getOrNull(AtelierValidatorConfigKey)
        ?: application.attributes.getOrNull(AtelierValidatorConfigKey)

    val responseStatus = status ?: config?.errorStatusCode ?: HttpStatusCode.BadRequest
    val errorResponse = config?.errorResponseBuilder?.invoke(failure)
        ?: AtelierValidationErrorResponse.from(failure)

    if (!response.isCommitted) {
        respond(responseStatus, errorResponse)
    }
}

/**
 * Sends a custom validation error response.
 *
 * Use this function when you want to provide a completely custom error format
 * that differs from the standard [AtelierValidationErrorResponse] structure.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class CustomError(
 *     val success: Boolean = false,
 *     val errorCode: String,
 *     val details: String
 * )
 *
 * post("/users") {
 *     val user = call.receiveAndValidate<User> { failure ->
 *         call.respondCustomValidationError(
 *             CustomError(
 *                 errorCode = "VALIDATION_ERROR",
 *                 details = failure.errors.joinToString { it.message }
 *             ),
 *             HttpStatusCode.UnprocessableEntity
 *         )
 *     } ?: return@post
 *
 *     // Process valid user
 * }
 * ```
 *
 * @param errorResponse Custom error response object (must be serializable)
 * @param status HTTP status code for the error response (defaults to 400 Bad Request)
 */
public suspend fun ApplicationCall.respondCustomValidationError(
    errorResponse: Any,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, errorResponse)
}
