/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Exception thrown when response validation fails.
 *
 * This exception is thrown when the response body fails validation against
 * the registered validator for its type.
 *
 * Example:
 * ```kotlin
 * try {
 *     val user = client.get("/users/1").bodyAndValidate<User>()
 * } catch (e: AtelierClientValidationException) {
 *     println("Validation failed:")
 *     e.validationResult.errors.forEach { error ->
 *         println("  ${error.fieldName}: ${error.message}")
 *     }
 * }
 * ```
 *
 * @property validationResult The validation failure containing detailed error information
 * @property url The URL of the request that produced the invalid response
 * @property statusCode The HTTP status code of the response
 */
public class AtelierClientValidationException(
    public val validationResult: ValidationResult.Failure,
    public val url: String? = null,
    public val statusCode: HttpStatusCode? = null
) : Exception(
    buildString {
        append("Response validation failed with ${validationResult.errorCount} error(s)")
        if (url != null) append(" for $url")
        if (statusCode != null) append(" (status: ${statusCode.value})")
    }
) {
    /**
     * Checks if this validation failure contains an error for the specified field.
     *
     * @param fieldName The name of the field to check
     * @return true if the field has validation errors, false otherwise
     */
    public fun hasErrorFor(fieldName: String): Boolean {
        return validationResult.errorsFor(fieldName).isNotEmpty()
    }

    /**
     * Returns all validation errors for a specific field.
     *
     * @param fieldName The name of the field
     * @return List of validation errors for the field, or empty list if no errors
     */
    public fun errorsFor(fieldName: String): List<ValidationErrorDetail> {
        return validationResult.errorsFor(fieldName)
    }
}

/**
 * Exception thrown when an unexpected HTTP status code is received.
 *
 * This exception is thrown before body validation when the response status code
 * is not in the configured [AtelierValidatorClientConfig.acceptedStatusCodes].
 *
 * Example:
 * ```kotlin
 * try {
 *     val user = client.get("/users/1").bodyAndValidate<User>()
 * } catch (e: AtelierClientStatusException) {
 *     when (e.statusCode) {
 *         HttpStatusCode.NotFound -> println("User not found")
 *         HttpStatusCode.Unauthorized -> println("Not authorized")
 *         else -> println("Unexpected status: ${e.statusCode}")
 *     }
 * }
 * ```
 *
 * @property statusCode The unexpected HTTP status code received
 * @property url The URL of the request
 * @property responseBody The response body as a string (maybe null if not available)
 */
public class AtelierClientStatusException(
    public val statusCode: HttpStatusCode,
    public val url: String? = null,
    public val responseBody: String? = null
) : Exception(
    buildString {
        append("Unexpected status code: ${statusCode.value} ${statusCode.description}")
        if (url != null) append(" for $url")
    }
) {
    /**
     * Checks if this exception represents a client error (4xx status code).
     */
    public val isClientError: Boolean
        get() = statusCode.value in 400..499

    /**
     * Checks if this exception represents a server error (5xx status code).
     */
    public val isServerError: Boolean
        get() = statusCode.value in 500..599

    /**
     * Returns the response body if available, or a default message.
     */
    public fun getResponseBodyOrDefault(default: String = "No response body"): String {
        return responseBody ?: default
    }
}

/**
 * Data transfer object for client validation errors.
 *
 * This class represents a validation error in a format suitable for logging,
 * metrics, or custom error handling on the client side.
 *
 * @property field The name of the field that failed validation
 * @property message Human-readable error message describing the validation failure
 * @property code The validation error code
 * @property value The actual value that failed validation (as a string)
 * @property url The URL of the request that produced the invalid response
 */
@Serializable
public data class ClientValidationErrorDetail(
    val field: String,
    val message: String,
    val code: String,
    val value: String,
    val url: String? = null
) {
    public companion object {
        /**
         * Creates a DTO from a domain validation error detail.
         *
         * @param error The validation error detail from the domain model
         * @param url Optional URL of the request
         * @return A serializable DTO representation of the error
         */
        public fun from(
            error: ValidationErrorDetail,
            url: String? = null
        ): ClientValidationErrorDetail {
            return ClientValidationErrorDetail(
                field = error.fieldName,
                message = error.message,
                code = error.code.toString(),
                value = error.actualValue,
                url = url
            )
        }
    }
}

/**
 * Client validation error response format.
 *
 * This class provides a structured representation of validation errors
 * for client-side error handling, logging, or display.
 *
 * @property message A general message indicating validation failure
 * @property errors List of detailed validation errors for each field
 * @property url The URL of the request that produced the invalid response
 * @property statusCode The HTTP status code of the response
 */
@Serializable
public data class ClientValidationErrorResponse(
    val message: String,
    val errors: List<ClientValidationErrorDetail>,
    val url: String? = null,
    val statusCode: Int? = null
) {
    public companion object {
        /**
         * Creates an error response from a validation exception.
         *
         * @param exception The validation exception
         * @return A structured error response
         */
        public fun from(exception: AtelierClientValidationException): ClientValidationErrorResponse {
            return ClientValidationErrorResponse(
                message = "Response validation failed",
                errors = exception.validationResult.errors.map {
                    ClientValidationErrorDetail.from(it, exception.url)
                },
                url = exception.url,
                statusCode = exception.statusCode?.value
            )
        }

        /**
         * Creates an error response from a validation failure.
         *
         * @param failure The validation failure
         * @param url Optional URL of the request
         * @param statusCode Optional HTTP status code
         * @return A structured error response
         */
        public fun from(
            failure: ValidationResult.Failure,
            url: String? = null,
            statusCode: HttpStatusCode? = null
        ): ClientValidationErrorResponse {
            return ClientValidationErrorResponse(
                message = "Response validation failed",
                errors = failure.errors.map {
                    ClientValidationErrorDetail.from(it, url)
                },
                url = url,
                statusCode = statusCode?.value
            )
        }
    }
}
