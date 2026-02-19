/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * Retrieves and validates the response body.
 *
 * This is the recommended approach for validating external API responses.
 * It deserializes the response body, validates it against the registered validator,
 * and throws an exception if validation fails.
 *
 * **Use Case**: Validate responses from third-party APIs to ensure they match
 * your expected contract.
 *
 * Example:
 * ```kotlin
 * try {
 *     val user = response.bodyAndValidate<User>()
 *     // user is guaranteed to be valid here
 *     println("Valid user: ${user.name}")
 * } catch (e: AtelierClientValidationException) {
 *     println("API contract violation:")
 *     e.validationResult.errors.forEach { error ->
 *         println("  ${error.fieldName}: ${error.message}")
 *     }
 * }
 * ```
 *
 * @param T The type of object to deserialize and validate
 * @return The validated object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpResponse.bodyAndValidate(): T {
    val obj = body<T>()
    val validator = getValidator<T>()
        ?: throw IllegalStateException("No validator registered for ${T::class.simpleName}")

    return when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> obj

        is ValidationResult.Failure -> throw AtelierClientValidationException(
            validationResult = result,
            url = request.url.toString()
        )
    }
}

/**
 * Retrieves and validates the response body with a custom error handler.
 *
 * Use this function when you need custom error handling logic for a specific request.
 * The error handler is invoked if validation fails, allowing you to handle errors
 * without throwing exceptions.
 *
 * Example:
 * ```kotlin
 * val user = response.bodyAndValidate<User> { failure ->
 *     logger.warn("API response validation failed: ${failure.errors}")
 *     metrics.increment("api.contract.violations")
 * }
 *
 * if (user != null) {
 *     println("Valid user: ${user.name}")
 * } else {
 *     // Use fallback data or retry
 * }
 * ```
 *
 * @param T The type of object to deserialize and validate
 * @param onError Custom error handler invoked when validation fails
 * @return The validated object, or null if validation failed
 */
public suspend inline fun <reified T : Any> HttpResponse.bodyAndValidate(
    noinline onError: suspend (ValidationResult.Failure) -> Unit
): T? {
    val obj = body<T>()
    val validator = getValidator<T>() ?: return obj

    return when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> obj

        is ValidationResult.Failure -> {
            onError(result)
            null
        }
    }
}

/**
 * Retrieves the response body or null if validation fails.
 *
 * Use this function when you want to handle validation failures gracefully
 * without throwing exceptions and without custom error handling.
 *
 * Example:
 * ```kotlin
 * val user = response.bodyAndValidateOrNull<User>()
 * if (user != null) {
 *     println("Valid user: ${user.name}")
 * } else {
 *     println("Invalid response, using fallback data")
 *     val user = getFallbackUser()
 * }
 * ```
 *
 * @param T The type of object to deserialize and validate
 * @return The validated object, or null if validation or status validation failed
 */
public suspend inline fun <reified T : Any> HttpResponse.bodyAndValidateOrNull(): T? {
    return try {
        val obj = body<T>()
        val validator = getValidator<T>() ?: return obj

        when (validator.validate(obj)) {
            is ValidationResult.Success -> obj
            is ValidationResult.Failure -> null
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Makes a GET request and validates the response.
 *
 * Convenience function that combines the HTTP request and validation in one call.
 *
 * Example:
 * ```kotlin
 * val user = client.getAndValidate<User>("https://api.example.com/users/1")
 * println("Valid user: ${user.name}")
 * ```
 *
 * @param T The expected response type
 * @param urlString The URL to request
 * @param block Optional configuration for the request
 * @return The validated response object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpClient.getAndValidate(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    return get(urlString, block).bodyAndValidate()
}

/**
 * Makes a POST request and validates the response.
 *
 * Example:
 * ```kotlin
 * val createdUser = client.postAndValidate<User>("https://api.example.com/users") {
 *     setBody(newUser)
 * }
 * ```
 *
 * @param T The expected response type
 * @param urlString The URL to request
 * @param block Optional configuration for the request
 * @return The validated response object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpClient.postAndValidate(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    return post(urlString, block).bodyAndValidate()
}

/**
 * Makes a PUT request and validates the response.
 *
 * Example:
 * ```kotlin
 * val updatedUser = client.putAndValidate<User>("https://api.example.com/users/1") {
 *     setBody(user)
 * }
 * ```
 *
 * @param T The expected response type
 * @param urlString The URL to request
 * @param block Optional configuration for the request
 * @return The validated response object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpClient.putAndValidate(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    return put(urlString, block).bodyAndValidate()
}

/**
 * Makes a PATCH request and validates the response.
 *
 * Example:
 * ```kotlin
 * val patchedUser = client.patchAndValidate<User>("https://api.example.com/users/1") {
 *     setBody(partialUpdate)
 * }
 * ```
 *
 * @param T The expected response type
 * @param urlString The URL to request
 * @param block Optional configuration for the request
 * @return The validated response object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpClient.patchAndValidate(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    return patch(urlString, block).bodyAndValidate()
}

/**
 * Makes a DELETE request and validates the response.
 *
 * Example:
 * ```kotlin
 * val response = client.deleteAndValidate<DeleteResponse>("https://api.example.com/users/1")
 * ```
 *
 * @param T The expected response type
 * @param urlString The URL to request
 * @param block Optional configuration for the request
 * @return The validated response object
 * @throws AtelierClientValidationException if validation fails
 */
public suspend inline fun <reified T : Any> HttpClient.deleteAndValidate(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {}
): T {
    return delete(urlString, block).bodyAndValidate()
}
