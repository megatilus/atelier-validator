/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.ktor.server

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.reflect.KClass

/**
 * Configuration for the Atelier Validator plugin.
 *
 * This class allows registration of validators for different types and
 * customization of validation behavior and error responses.
 *
 * Example:
 * ```kotlin
 * install(AtelierValidatorServer) {
 *     // Register validators
 *     register(userValidator)
 *     register(productValidator)
 *
 *     // Custom error response format
 *     errorResponseBuilder = { failure ->
 *         CustomErrorResponse(
 *             errors = failure.errors.map { it.message }
 *         )
 *     }
 * }
 * ```
 */
public class AtelierValidatorServerConfig {
    /**
     * Map of registered validators by class type.
     *
     * Each validator is stored with its corresponding KClass as the key,
     * allowing type-safe retrieval during validation.
     */
    public val validators: MutableMap<KClass<*>, AtelierValidator<Any>> = mutableMapOf()

    /**
     * Determines whether to integrate automatically with Ktor's RequestValidation plugin.
     *
     * When true (default): Validation occurs automatically before route handlers are invoked.
     * Use case: Simple APIs, prototyping, consistent validation across all routes.
     *
     * When false (default): Validation must be performed manually using [receiveAndValidate].
     * Use case: Custom error handling, conditional validation, complex business logic.
     *
     * Default: true
     */
    public var useAutomaticValidation: Boolean = true

    /**
     * When true, validates at startup that all registered validators are properly configured.
     *
     * This performs basic sanity checks:
     * - At least one validator is registered
     * - Configuration is not empty
     *
     * Enabling this helps catch configuration errors early at startup rather than
     * at runtime when requests arrive.
     *
     * Default: true
     */
    public var validateAtStartup: Boolean = true

    /**
     * Builder function to customize validation error responses.
     *
     * This function is called when validation fails to create the error response
     * that will be sent to the client. Override this to provide custom error
     * formats that match your API's response structure.
     *
     * By default, returns [AtelierValidationErrorResponse] which provides a
     * standard error format with detailed field-level errors.
     *
     * Example:
     * ```kotlin
     * errorResponseBuilder = { failure ->
     *     CustomApiError(
     *         status = "error",
     *         code = 422,
     *         message = "Validation failed",
     *         details = failure.errors.map {
     *             ErrorDetail(it.fieldName, it.message)
     *         }
     *     )
     * }
     * ```
     */
    public var errorResponseBuilder: (ValidationResult.Failure) -> Any = { failure ->
        AtelierValidationErrorResponse.from(failure)
    }

    /**
     * Registers a validator for a specific type.
     *
     * This function associates a validator with a data type, enabling automatic
     * or manual validation of that type throughout the application.
     *
     * The validator is wrapped to handle type checking and provide clear error
     * messages if the wrong type is passed.
     *
     * Example:
     * ```kotlin
     * val userValidator = AtelierValidator<User> {
     *     User::name { notBlank(); minLength(2) }
     *     User::email { email() }
     *     User::age { min(18) }
     * }
     *
     * install(AtelierValidatorServer) {
     *     register(userValidator)
     *     register(productValidator)
     * }
     * ```
     *
     * @param T The type to validate
     * @param validator The validator instance for this type
     * @throws IllegalArgumentException if an object of incorrect type is validated
     */
    public inline fun <reified T : Any> register(validator: AtelierValidator<T>) {
        val kClass = T::class
        val wrapper = object : AtelierValidator<Any> {
            override fun validate(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        """
                        | Type mismatch in validator:
                        |   Expected: ${kClass.simpleName}
                        |   Received: ${obj::class.simpleName}
                        |
                        | Make sure you're validating the correct object type.
                        """.trimMargin()
                    )
                }
                return validator.validate(obj as T)
            }

            override fun validateFirst(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        """
                        | Type mismatch in validator:
                        |   Expected: ${kClass.simpleName}
                        |   Received: ${obj::class.simpleName}
                        |
                        | Make sure you're validating the correct object type.
                        """.trimMargin()
                    )
                }
                return validator.validateFirst(obj as T)
            }
        }

        validators[kClass] = wrapper
    }

    /**
     * Validates the configuration at startup.
     *
     * This is called automatically by the plugin if [validateAtStartup] is true.
     * It performs basic sanity checks to catch configuration errors early.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    internal fun validateConfiguration() {
        if (!validateAtStartup) return

        if (validators.isEmpty()) {
            throw IllegalStateException(
                """
                |===============================================================================
                | AtelierValidator Configuration Error: No validators registered
                |===============================================================================
                |
                | You must register at least one validator before using the plugin.
                |
                | Example:
                |
                | val userValidator = AtelierValidator<User> {
                |     User::name { notBlank(); minLength(2) }
                |     User::email { email() }
                | }
                |
                | install(AtelierValidatorServer) {
                |     register(userValidator)
                |     register(productValidator)  // Add more validators as needed
                | }
                |===============================================================================
                """.trimMargin()
            )
        }
    }
}
