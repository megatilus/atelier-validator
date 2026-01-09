/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.requestvalidation.ValidationResult as KtorValidationResult

/**
 * Atelier Validator plugin for Ktor Server.
 *
 * Provides automatic and manual validation for request bodies with customizable error responses.
 *
 * Basic usage:
 * ```kotlin
 * install(AtelierValidator) {
 *     register(userValidator)
 *     register(productValidator)
 * }
 * ```
 *
 * With custom error handling:
 * ```kotlin
 * install(AtelierValidator) {
 *     register(userValidator)
 *     errorStatusCode = HttpStatusCode.UnprocessableEntity
 *     errorResponseBuilder = { failure ->
 *         CustomErrorResponse(failure)
 *     }
 * }
 * ```
 *
 * When StatusPages is already installed:
 * ```kotlin
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(validatorConfig)
 *     // ... other exception handlers
 * }
 *
 * install(AtelierValidator) {
 *     register(userValidator)
 * }
 * ```
 */
public val AtelierValidatorPlugin: ApplicationPlugin<AtelierValidatorConfig> =
    createApplicationPlugin(
        name = "AtelierValidatorPlugin",
        createConfiguration = ::AtelierValidatorConfig
    ) {
        val config = pluginConfig

        application.attributes.put(AtelierValidatorConfigKey, config)

        onCall { call ->
            call.attributes.put(AtelierValidatorConfigKey, config)
        }

        // Setup StatusPages for validation exception handling
        setupStatusPages(application, config)

        // Automatic validation via RequestValidation plugin
        application.install(RequestValidation) {
            config.validators.forEach { (kClass, validatorAny) ->
                validate(kClass) { value ->
                    when (val result = validatorAny.validate(value)) {
                        is ValidationResult.Success -> KtorValidationResult.Valid

                        is ValidationResult.Failure -> {
                            // Only return Invalid (which throws RequestValidationException)
                            // if automatic validation is enabled in config
                            if (config.useAutomaticValidation) {
                                KtorValidationResult.Invalid(
                                    result.errors.map { "${it.fieldName}: ${it.message}" }
                                )
                            } else {
                                KtorValidationResult.Valid
                            }
                        }
                    }
                }
            }
        }
    }

/**
 * Receives and validates a request body, throwing [AtelierValidationException] if validation fails.
 *
 * Use this when you want exception-based error handling instead of null-based handling.
 *
 * Example:
 * ```kotlin
 * post("/users") {
 *     try {
 *         val user = call.receiveAndValidateAndThrow<User>()
 *         userRepository.create(user)
 *         call.respond(HttpStatusCode.Created, user)
 *     } catch (e: AtelierValidationException) {
 *         // Custom error handling
 *         call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
 *     }
 * }
 * ```
 *
 * @return The validated object
 * @throws AtelierValidationException if validation fails
 * @throws IllegalStateException if no validator is registered for type T
 */
public suspend inline fun <reified T : Any> ApplicationCall.receiveAndValidateAndThrow(): T {
    val obj = receive<T>()
    val validator = getValidator<T>()
        ?: throw IllegalStateException("No validator registered for ${T::class.simpleName}")

    return when (val result = validator.validate(obj)) {
        is ValidationResult.Success -> obj
        is ValidationResult.Failure -> throw AtelierValidationException(result)
    }
}

/**
 * Sets up StatusPages plugin to handle validation exceptions.
 *
 * Automatically installs StatusPages if not already present and configures handlers
 * for both Ktor's [RequestValidationException] and Atelier's [AtelierValidationException].
 *
 * If StatusPages is already installed, logs a warning with instructions on how to
 * manually configure validation exception handlers.
 */
private fun setupStatusPages(
    application: Application,
    config: AtelierValidatorConfig
) {
    val existingStatusPages = application.pluginOrNull(StatusPages)

    if (existingStatusPages != null) {
        application.log.warn(
            """
            StatusPages plugin is already installed.
            
            To enable automatic validation error handling, add this to your StatusPages configuration:
            
            install(StatusPages) {
                configureValidationExceptionHandlers(validatorConfig)
                // ... your other exception handlers
            }
            
            Where 'validatorConfig' is your AtelierValidatorConfig instance.
            """.trimIndent()
        )
        return
    }

    // StatusPages not yet installed - install it with validation handlers
    application.install(StatusPages) {
        configureValidationExceptionHandlers(config)
    }
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
 * Use [receiveAndValidateAndThrow] to trigger this exception on validation failure.
 *
 * @property validationResult The validation failure containing detailed error information
 */
public class AtelierValidationException(
    public val validationResult: ValidationResult.Failure
) : Exception("Validation failed with ${validationResult.errorCount} error(s)") {
    /**
     * Checks if this validation failure contains an error for the specified field.
     *
     * @param fieldName The name of the field to check
     * @return true if the field has validation errors, false otherwise
     */
    public fun hasErrorFor(fieldName: String): Boolean {
        return validationResult.errorsFor(fieldName).isNotEmpty()
    }
}

/**
 * Extension function to check if a validation failure contains errors for a specific field.
 *
 * @param fieldName The name of the field to check
 * @return true if the field has validation errors, false otherwise
 */
public fun ValidationResult.Failure.hasErrorFor(fieldName: String): Boolean {
    return errorsFor(fieldName).isNotEmpty()
}

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
