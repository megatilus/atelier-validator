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
 * DTO for serialization (separated from core domain model).
 *
 * This class represents a single validation error in a format suitable for JSON serialization.
 */
@Serializable
public data class ValidationErrorDetailDto(
    val field: String,
    val message: String,
    val code: String,
    val value: String
) {
    public companion object {
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
 */
@Serializable
public data class AtelierValidationErrorResponse(
    val message: String,
    val errors: List<ValidationErrorDetailDto>
) {
    public companion object {
        public fun from(failure: ValidationResult.Failure): AtelierValidationErrorResponse {
            return AtelierValidationErrorResponse(
                message = "Validation failed",
                errors = failure.errors.map { ValidationErrorDetailDto.from(it) }
            )
        }
    }
}

/**
 * Responds with a validation error in the standard format.
 *
 * @param failure The validation failure containing error details
 * @param status HTTP status code (defaults to 400 Bad Request)
 */
public suspend fun ApplicationCall.respondValidationError(
    failure: ValidationResult.Failure,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, AtelierValidationErrorResponse.from(failure))
}

/**
 * Responds with a custom validation error response.
 *
 * Use this when you want to provide a completely custom error format.
 *
 * @param errorResponse Custom error response object
 * @param status HTTP status code (defaults to 400 Bad Request)
 */
public suspend fun ApplicationCall.respondValidationError(
    errorResponse: Any,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, errorResponse)
}
