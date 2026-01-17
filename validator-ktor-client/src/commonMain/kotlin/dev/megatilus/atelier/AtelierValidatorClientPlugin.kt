/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.client.*
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlin.reflect.KClass

/**
 * Atelier Validator plugin for Ktor Client.
 *
 * Provides manual validation for HTTP response bodies with customizable error handling
 * and status code validation.
 *
 * **Use Case**: Validate responses from external/third-party APIs to ensure they
 * match your expected contract. This is useful for defensive programming when
 * integrating with APIs you don't control.
 *
 * Basic usage (manual validation - recommended):
 * ```kotlin
 * val client = HttpClient {
 *     install(AtelierValidatorClient) {
 *         register(userValidator)
 *         register(productValidator)
 *     }
 * }
 *
 * // Validate explicitly when needed
 * val user = client.get("https://api.example.com/users/1").bodyAndValidate<User>()
 * ```
 *
 * With custom configuration:
 * ```kotlin
 * val client = HttpClient {
 *     install(AtelierValidatorClient) {
 *         register(userValidator)
 *
 *         // Only accept specific status codes
 *         acceptStatusCodes(HttpStatusCode.OK, HttpStatusCode.Created)
 *     }
 * }
 * ```
 *
 * Automatic validation (opt-in, not recommended):
 * ```kotlin
 * val client = HttpClient {
 *     install(AtelierValidatorClient) {
 *         register(userValidator)
 *         useAutomaticValidation = true  // Validate all responses automatically
 *     }
 * }
 * ```
 */
public val AtelierValidatorClient: ClientPlugin<AtelierValidatorClientConfig> =
    createClientPlugin(
        "AtelierValidatorClient",
        ::AtelierValidatorClientConfig
    ) {
        val config = pluginConfig

        // Store config in client attributes for access in extensions
        client.attributes.put(AtelierValidatorClientConfigKey, config)

        // Add config to each request for manual validation access
        onRequest { request, _ ->
            request.attributes.put(AtelierValidatorClientConfigKey, config)
        }

        // Intercept responses for automatic validation (opt-in only)
        if (config.useAutomaticValidation) {
            transformResponseBody { response, body, requestedType ->
                // Check if we have a validator for this type
                val kClass = requestedType.type
                val validator = config.validators[kClass] ?: return@transformResponseBody body

                // First, validate status code
                if (response.status !in config.acceptedStatusCodes) {
                    val responseBody = try {
                        response.bodyAsText()
                    } catch (_: Exception) {
                        null
                    }

                    throw AtelierClientStatusException(
                        statusCode = response.status,
                        url = response.request.url.toString(),
                        responseBody = responseBody
                    )
                }

                // Body is already deserialized at this point, validate it
                when (val result = validator.validate(body)) {
                    is ValidationResult.Success -> body

                    is ValidationResult.Failure -> {
                        throw AtelierClientValidationException(
                            validationResult = result,
                            url = response.request.url.toString(),
                            statusCode = response.status
                        )
                    }
                }
            }
        }
    }

/**
 * Attribute key for storing the validator configuration in the client context.
 */
public val AtelierValidatorClientConfigKey: AttributeKey<AtelierValidatorClientConfig> =
    AttributeKey("dev.megatilus.atelier.AtelierValidatorClientConfig")

/**
 * Retrieves the validator registered for type T from the client configuration.
 *
 * This function is used internally by validation extension functions to obtain
 * the appropriate validator for a given type.
 *
 * @return The validator for type T, or null if no validator is registered
 */
public inline fun <reified T : Any> HttpClient.getValidator(): AtelierValidatorContract<T>? {
    val config = attributes.getOrNull(AtelierValidatorClientConfigKey) ?: return null
    return getValidatorFromConfig(config, T::class)
}

/**
 * Retrieves the validator registered for type T from a response.
 *
 * @return The validator for type T, or null if no validator is registered
 */
public inline fun <reified T : Any> HttpResponse.getValidator(): AtelierValidatorContract<T>? {
    val config = call.request.attributes.getOrNull(AtelierValidatorClientConfigKey)
        ?: return null
    return getValidatorFromConfig(config, T::class)
}

/**
 * Retrieves the validator configuration from a response.
 *
 * @return The validator configuration, or null if not found
 */
public fun HttpResponse.getValidatorConfig(): AtelierValidatorClientConfig? {
    return call.request.attributes.getOrNull(AtelierValidatorClientConfigKey)
}

/**
 * Internal function to retrieve a validator from config for a specific type.
 */
@PublishedApi
internal fun <T : Any> getValidatorFromConfig(
    config: AtelierValidatorClientConfig,
    kClass: KClass<T>
): AtelierValidatorContract<T>? {
    val anyValidator = config.validators[kClass]
        ?: config.validators.entries.find { it.key.toString() == kClass.toString() }?.value
        ?: return null

    return object : AtelierValidatorContract<T> {
        override fun validate(obj: T): ValidationResult =
            anyValidator.validate(obj)

        override fun validateFirst(obj: T): ValidationResult =
            anyValidator.validateFirst(obj)
    }
}

/**
 * Validates a status code against the configured accepted status codes.
 *
 * @throws AtelierClientStatusException if the status code is not accepted
 */
public suspend fun HttpResponse.validateStatusCode() {
    val config = getValidatorConfig() ?: return

    if (status !in config.acceptedStatusCodes) {
        val responseBody = try {
            bodyAsText()
        } catch (_: Exception) {
            null
        }

        throw AtelierClientStatusException(
            statusCode = status,
            url = request.url.toString(),
            responseBody = responseBody
        )
    }
}
