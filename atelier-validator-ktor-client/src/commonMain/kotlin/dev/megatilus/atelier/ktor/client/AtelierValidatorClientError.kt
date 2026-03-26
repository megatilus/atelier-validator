/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.ktor.client

import dev.megatilus.atelier.validator.results.ErrorDetail
import dev.megatilus.atelier.validator.results.ValidationError
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlinx.serialization.Serializable

/**
 * Exception thrown when response validation fails on the client side.
 *
 * Example:
 * ```kotlin
 * try {
 *     val user = response.bodyAndValidate<User>()
 * } catch (e: AtelierClientValidationException) {
 *     e.validationResult.errors.forEach { println("${it.fieldName}: ${it.message}") }
 * }
 * ```
 *
 * @property validationResult The validation failure containing detailed error information
 * @property url The URL of the request that produced the invalid response
 */
public class AtelierClientValidationException(
    public val validationResult: ValidationResult.Failure,
    public val url: String? = null
) : Exception(
    buildString {
        append("Response validation failed with ${validationResult.errorCount} error(s)")
        if (url != null) append(" for $url")
    }
) {
    /**
     * Checks if this validation failure contains an error for the specified field.
     */
    public fun hasErrorFor(fieldName: String): Boolean =
        validationResult.errorsFor(fieldName).isNotEmpty()

    /**
     * Returns all validation errors for a specific field.
     */
    public fun errorsFor(fieldName: String): List<ValidationError> =
        validationResult.errorsFor(fieldName)
}

/**
 * Data transfer object for a single client-side validation error.
 *
 * Serializable presentation layer wrapper around [ErrorDetail] from the core.
 * Adds [url] context specific to client-side HTTP responses.
 *
 * @property field The name of the field that failed validation
 * @property message Human-readable error message
 * @property code The validation error code
 * @property value The actual value that failed validation
 * @property url The URL of the request that produced the invalid response
 */
@Serializable
public data class ClientValidationErrorDto(
    val field: String,
    val message: String,
    val code: String,
    val value: String?,
    val url: String? = null
) {
    public companion object {
        /**
         * Creates a DTO from an [ErrorDetail] produced by the core.
         *
         * @param detail The error detail from [ValidationResult.Failure.toDetailedList]
         * @param url Optional URL of the originating request
         */
        public fun from(detail: ErrorDetail, url: String? = null): ClientValidationErrorDto =
            ClientValidationErrorDto(
                field = detail.field,
                message = detail.message,
                code = detail.code,
                value = detail.actualValue,
                url = url
            )
    }
}

/**
 * Client-side validation error response format.
 *
 * Provides a structured representation for logging, metrics, or display.
 *
 * @property message A general message indicating validation failure
 * @property errors List of detailed validation errors per field
 * @property url The URL of the request that produced the invalid response
 */
@Serializable
public data class ClientValidationErrorResponse(
    val message: String,
    val errors: List<ClientValidationErrorDto>,
    val url: String? = null
) {
    public companion object {
        /**
         * Creates a response from an [AtelierClientValidationException].
         *
         * Delegates to [ValidationResult.Failure.toDetailedList] from the core.
         */
        public fun from(exception: AtelierClientValidationException): ClientValidationErrorResponse =
            ClientValidationErrorResponse(
                message = "Response validation failed",
                errors = exception.validationResult.toDetailedList()
                    .map { ClientValidationErrorDto.from(it, exception.url) },
                url = exception.url
            )

        /**
         * Creates a response from a [ValidationResult.Failure].
         *
         * Delegates to [ValidationResult.Failure.toDetailedList] from the core.
         *
         * @param failure The validation failure
         * @param url Optional URL of the originating request
         */
        public fun from(
            failure: ValidationResult.Failure,
            url: String? = null
        ): ClientValidationErrorResponse =
            ClientValidationErrorResponse(
                message = "Response validation failed",
                errors = failure.toDetailedList()
                    .map { ClientValidationErrorDto.from(it, url) },
                url = url
            )
    }
}
