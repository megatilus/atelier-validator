/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.*
import kotlin.reflect.KClass

/**
 * Configuration for the Atelier Validator plugin.
 *
 * This class allows registration of validators for different types and
 * customization of validation behavior and error responses.
 *
 * Example:
 * ```kotlin
 * install(AtelierValidator) {
 *     // Register validators
 *     register(userValidator)
 *     register(productValidator)
 *
 *     // Customize error handling
 *     errorStatusCode = HttpStatusCode.UnprocessableEntity
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
public class AtelierValidatorConfig {
    /**
     * Map of registered validators by class type.
     *
     * Each validator is stored with its corresponding KClass as the key,
     * allowing type-safe retrieval during validation.
     */
    public val validators: MutableMap<KClass<*>, AtelierValidatorContract<Any>> = mutableMapOf()

    /**
     * HTTP status code to use for validation error responses.
     *
     * This status code is used by default when sending validation error responses.
     * Individual endpoints can override this by providing a custom status code.
     *
     * Default: 400 Bad Request
     */
    public var errorStatusCode: HttpStatusCode = HttpStatusCode.BadRequest

    /**
     * Determines whether to integrate automatically with Ktor's RequestValidation plugin.
     *
     * When true, validation occurs automatically before route handlers are invoked.
     * When false, validation must be performed manually using [receiveAndValidate].
     *
     * **Security Note**: Manual validation (false) is recommended as it makes validation
     * explicit and visible in your code. This prevents accidental security holes from
     * forgotten validators.
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
     * Default: true (recommended for production safety)
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
     * val userValidator = atelierValidator<User> {
     *     User::name { notBlank(); minLength(2) }
     *     User::email { email() }
     *     User::age { min(18) }
     * }
     *
     * install(AtelierValidator) {
     *     register(userValidator)
     *     register(productValidator)
     * }
     * ```
     *
     * @param T The type to validate
     * @param validator The validator instance for this type
     * @throws IllegalArgumentException if an object of incorrect type is validated
     */
    public inline fun <reified T : Any> register(validator: AtelierValidatorContract<T>) {
        val kClass = T::class
        val wrapper = object : AtelierValidatorContract<Any> {
            override fun validate(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        """
                        |Type mismatch in validator:
                        |  Expected: ${kClass.simpleName}
                        |  Received: ${obj::class.simpleName}
                        |
                        |Make sure you're validating the correct object type.
                        """.trimMargin()
                    )
                }
                return validator.validate(obj as T)
            }

            override fun validateFirst(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        """
                        |Type mismatch in validator:
                        |  Expected: ${kClass.simpleName}
                        |  Received: ${obj::class.simpleName}
                        |
                        |Make sure you're validating the correct object type.
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
                | val userValidator = atelierValidator<User> {
                |     User::name { notBlank(); minLength(2) }
                |     User::email { email() }
                | }
                |
                | install(AtelierValidator) {
                |     register(userValidator)
                |     register(productValidator)  // Add more validators as needed
                | }
                |===============================================================================
                """.trimMargin()
            )
        }
    }
}
