/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.HttpStatusCode
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
 * install(AtelierValidatorServer) {
 *     register(userValidator)
 *     useAutomaticValidation = true // Default
 * }
 * ```
 *
 * **Manual validation:**
 * ```kotlin
 * install(AtelierValidatorServer) {
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
 * // StatusPages must be installed BEFORE AtelierValidatorServer
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(validatorConfig)
 * }
 *
 * install(AtelierValidatorServer) {
 *     validators.putAll(validatorConfig.validators)
 *     useAutomaticValidation = true
 * }
 * ```
 *
 * **Custom error handling:**
 * ```kotlin
 * install(AtelierValidatorServer) {
 *     register(userValidator)
 *     errorResponseBuilder = { failure ->
 *         CustomErrorResponse(failure)
 *     }
 * }
 * ```
 */
public val AtelierValidatorServer: ApplicationPlugin<AtelierValidatorServerConfig> =
    createApplicationPlugin(
        name = "AtelierValidatorServer",
        createConfiguration = ::AtelierValidatorServerConfig
    ) {
        val config = pluginConfig

        // Validate configuration at startup
        config.validateConfiguration()

        application.attributes.put(AtelierValidatorServerConfigKey, config)

        onCall { call ->
            call.attributes.put(AtelierValidatorServerConfigKey, config)
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
        }
    }

/**
 * Sets up StatusPages for automatic validation mode.
 * Auto-installs StatusPages if not present to handle RequestValidationException.
 */
private fun setupStatusPagesForAutomatic(application: Application, config: AtelierValidatorServerConfig) {
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
 * Configures exception handlers for validation errors in Ktor's StatusPages plugin.
 *
 * This function sets up handlers for two types of validation exceptions:
 * - [RequestValidationException]: Thrown when request parameters or body fail validation
 * - [AtelierValidationException]: Thrown when custom validation logic fails
 *
 * All validation errors are returned with HTTP 400 Bad Request status code.
 *
 * Example:
 * ```kotlin
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(config)
 * }
 * ```
 *
 * @param config The validator configuration containing error response builder
 */
public fun StatusPagesConfig.configureValidationExceptionHandlers(
    config: AtelierValidatorServerConfig
) {
    exception<RequestValidationException> { call, cause ->
        @Serializable
        data class SimpleErrorResponse(
            val message: String,
            val errors: List<String>
        )

        call.respond(
            HttpStatusCode.BadRequest,
            SimpleErrorResponse(
                message = "Request validation failed",
                errors = cause.reasons
            )
        )
    }

    exception<AtelierValidationException> { call, cause ->
        val errorResponse = config.errorResponseBuilder(cause.validationResult)
        call.respond(
            HttpStatusCode.BadRequest,
            errorResponse
        )
    }
}

/**
 * Attribute key for storing the validator configuration in the application context.
 */
public val AtelierValidatorServerConfigKey: AttributeKey<AtelierValidatorServerConfig> =
    AttributeKey("dev.megatilus.atelier.AtelierValidatorServerConfig")

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
public inline fun <reified T : Any> ApplicationCall.getValidator(): AtelierValidator<T>? {
    val config = attributes.getOrNull(AtelierValidatorServerConfigKey)
        ?: application.attributes.getOrNull(AtelierValidatorServerConfigKey)
        ?: return null

    val targetClass = T::class
    val anyValidator = config.validators[targetClass]
        ?: config.validators.entries.find { it.key.toString() == targetClass.toString() }?.value
        ?: return null

    return object : AtelierValidator<T> {
        override fun validate(obj: T): ValidationResult =
            anyValidator.validate(obj)

        override fun validateFirst(obj: T): ValidationResult =
            anyValidator.validateFirst(obj)
    }
}
