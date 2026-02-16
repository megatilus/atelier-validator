/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.requestvalidation.ValidationResult as KtorValidationResult

/**
 * Atelier Validator plugin for Ktor Server.
 *
 * Provides automatic and manual validation for request bodies with customizable error responses.
 *
 * **IMPORTANT: Installation Order**
 * When using automatic validation with StatusPages, install StatusPages BEFORE AtelierValidator:
 *
 * ```kotlin
 * // 1. Install StatusPages first
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(validatorConfig)
 * }
 *
 * // 2. Then install AtelierValidator
 * install(AtelierValidator) {
 *     register(userValidator)
 *     useAutomaticValidation = true
 * }
 * ```
 *
 * **Manual validation (recommended for better control):**
 * ```kotlin
 * install(AtelierValidator) {
 *     register(userValidator)
 *     register(productValidator)
 *     useAutomaticValidation = false
 * }
 *
 * post("/users") {
 *     val user = call.receiveAndValidate<User>() ?: return@post
 *     userRepository.create(user)
 *     call.respond(HttpStatusCode.Created, user)
 * }
 * ```
 *
 * **Automatic validation:**
 * ```kotlin
 * val validatorConfig = AtelierValidatorConfig().apply {
 *     register(userValidator)
 *     useAutomaticValidation = true
 * }
 *
 * // StatusPages must be installed BEFORE AtelierValidator
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(validatorConfig)
 * }
 *
 * install(AtelierValidator) {
 *     validators.putAll(validatorConfig.validators)
 *     useAutomaticValidation = true
 * }
 * ```
 *
 * **Custom error handling:**
 * ```kotlin
 * install(AtelierValidator) {
 *     register(userValidator)
 *     errorStatusCode = HttpStatusCode.UnprocessableEntity
 *     errorResponseBuilder = { failure ->
 *         CustomErrorResponse(failure)
 *     }
 * }
 * ```
 */
public val AtelierValidatorPlugin: ApplicationPlugin<AtelierValidatorConfig> =
    createApplicationPlugin(
        name = "AtelierValidatorPlugin",
        createConfiguration = ::AtelierValidatorConfig
    ) {
        val config = pluginConfig

        // Validate configuration at startup
        config.validateConfiguration()

        application.attributes.put(AtelierValidatorConfigKey, config)

        onCall { call ->
            call.attributes.put(AtelierValidatorConfigKey, config)
        }

        // Automatic validation via RequestValidation plugin (opt-in)
        if (config.useAutomaticValidation) {
            // Install RequestValidation for automatic validation

            application.install(RequestValidation) {
                config.validators.forEach { (kClass, validatorAny) ->
                    validate(kClass) { value ->
                        when (val result = validatorAny.validate(value)) {
                            is ValidationResult.Success -> KtorValidationResult.Valid

                            is ValidationResult.Failure -> {
                                KtorValidationResult.Invalid(
                                    result.errors.map { "${it.fieldName}: ${it.message}" }
                                )
                            }
                        }
                    }
                }
            }

            // Auto-install StatusPages if not present (to handle RequestValidationException)
            setupStatusPagesForAutomatic(application, config)
        } else {
            // Manual validation mode - just log warning about StatusPages
            setupStatusPagesForManual(application)
        }
    }

/**
 * Sets up StatusPages for automatic validation mode.
 * Auto-installs StatusPages if not present to handle RequestValidationException.
 */
private fun setupStatusPagesForAutomatic(application: Application, config: AtelierValidatorConfig) {
    val existingStatusPages = application.pluginOrNull(StatusPages)

    if (existingStatusPages != null) {
        // StatusPages already installed - assume user has configured it correctly
        // If not configured, they'll see validation errors in production
        return
    }

    // Auto-install StatusPages with validation handlers for automatic mode
    application.install(StatusPages) {
        configureValidationExceptionHandlers(config)
    }

    application.log.info("StatusPages plugin auto-installed with validation handlers for automatic validation mode")
}

/**
 * Sets up StatusPages for manual validation mode.
 * Just logs a warning - no auto-install in manual mode.
 */
private fun setupStatusPagesForManual(application: Application) {
    // In manual validation mode, StatusPages is optional
    // Users can install it if they want global exception handling
    // No need to log anything - it's their choice
}

/**
 * Configures exception handlers for validation errors in an existing StatusPages configuration.
 *
 * Use this function when StatusPages is already installed, and you want to add
 * validation error handling to it.
 *
 * Example:
 * ```kotlin
 * val validatorConfig = AtelierValidatorConfig().apply {
 *     register(userValidator)
 * }
 *
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(validatorConfig)
 *
 *     // Your other exception handlers
 *     exception<IllegalArgumentException> { call, cause ->
 *         call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request")
 *     }
 * }
 * ```
 *
 * @param config The validator configuration containing error handling settings
 */
public fun StatusPagesConfig.configureValidationExceptionHandlers(
    config: AtelierValidatorConfig
) {
    exception<RequestValidationException> { call, cause ->
        @Serializable
        data class SimpleErrorResponse(
            val message: String,
            val errors: List<String>
        )

        call.respond(
            config.errorStatusCode,
            SimpleErrorResponse(
                message = "Request validation failed",
                errors = cause.reasons
            )
        )
    }

    exception<AtelierValidationException> { call, cause ->
        val errorResponse = config.errorResponseBuilder(cause.validationResult)
        call.respond(config.errorStatusCode, errorResponse)
    }
}

/**
 * Attribute key for storing the validator configuration in the application context.
 */
public val AtelierValidatorConfigKey: AttributeKey<AtelierValidatorConfig> =
    AttributeKey("dev.megatilus.atelier.AtelierValidatorConfig")

/**
 * Exception thrown when validation fails.
 *
 * This exception is caught by StatusPages and converted to an appropriate error response.
 *
 * @property validationResult The validation failure containing detailed error information
 */
public class AtelierValidationException(
    public val validationResult: ValidationResult.Failure
) : Exception(
    "Validation failed: ${validationResult.errorCount} error(s) found - " +
        validationResult.errors.joinToString(", ") { "${it.fieldName}: ${it.message}" }
)

/**
 * Retrieves the validator registered for type T from the application context.
 *
 * This function is used internally by validation extension functions to obtain
 * the appropriate validator for a given type.
 *
 * @return The validator for type T, or null if no validator is registered
 */
public inline fun <reified T : Any> ApplicationCall.getValidator(): AtelierValidatorContract<T>? {
    val config = attributes.getOrNull(AtelierValidatorConfigKey)
        ?: application.attributes.getOrNull(AtelierValidatorConfigKey)
        ?: return null

    val targetClass = T::class
    val anyValidator = config.validators[targetClass]
        ?: config.validators.entries.find { it.key.toString() == targetClass.toString() }?.value
        ?: return null

    return object : AtelierValidatorContract<T> {
        override fun validate(obj: T): ValidationResult =
            anyValidator.validate(obj)

        override fun validateFirst(obj: T): ValidationResult =
            anyValidator.validateFirst(obj)
    }
}
