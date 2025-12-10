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
 * Usage:
 * ```kotlin
 * install(AtelierValidator) {
 *     register(userValidator)
 *     register(productValidator)
 *
 *     // Optional: customize error handling
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

        application.attributes.put(AtelierValidatorConfigKey, config)

        // Option 1: Automatic validation via RequestValidation (si l'utilisateur le veut)
        if (config.useAutomaticValidation) {
            setupAutomaticValidation(application, config)
        }

        setupStatusPages(application, config)
    }

/**
 * Sets up automatic validation using Ktor's RequestValidation plugin.
 *
 * When enabled, all registered validators will automatically validate
 * incoming requests before they reach the route handler.
 */
private fun setupAutomaticValidation(
    application: Application,
    config: AtelierValidatorConfig
) {
    application.install(RequestValidation) {
        // config.validators: Map<KClass<*>, AtelierValidatorContract<Any>>
        config.validators.forEach { (kClass, validatorAny) ->

            validate(kClass) { value ->
                // On appelle notre validator (qui retourne dev.megatilus.atelier.results.ValidationResult)
                when (val result = validatorAny.validate(value)) {
                    is ValidationResult.Success ->
                        // on mappe vers le type attendu par Ktor
                        KtorValidationResult.Valid

                    is ValidationResult.Failure ->
                        KtorValidationResult.Invalid(
                            // Ktor attend une liste de messages d'erreur (String)
                            result.errors.map { "${it.fieldName}: ${it.message}" }
                        )
                }
            }
        }
    }
}

/**
 * Sets up StatusPages plugin to handle validation exceptions.
 *
 * This provides automatic error responses for both Ktor's RequestValidationException
 * and our custom AtelierValidationException.
 */
private fun setupStatusPages(
    application: Application,
    config: AtelierValidatorConfig
) {
    val existingStatusPages = application.pluginOrNull(StatusPages)

    if (existingStatusPages != null) {
        application.log.warn(
            "StatusPages is already installed. " +
                "AtelierValidatorPlugin cannot add handlers automatically. " +
                "Please configure exception<AtelierValidationException> manually in your StatusPages block."
        )
        return
    }

    // StatusPages n'existe pas, on l'installe avec nos handlers
    application.install(StatusPages) {
        configureValidationExceptionHandlers(config)
    }
}

/**
 * Configures exception handlers for validation errors in an existing StatusPages configuration.
 *
 * Use this if you've already installed StatusPages and want to add validation error handling.
 *
 * Example:
 * ```kotlin
 * install(StatusPages) {
 *     configureValidationExceptionHandlers(config)
 *     // ... your other exception handlers
 * }
 * ```
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
 * Attribute key for storing the validator configuration in the application.
 */
public val AtelierValidatorConfigKey: AttributeKey<AtelierValidatorConfig> =
    AttributeKey("dev.megatilus.atelier.AtelierValidatorConfig")

/**
 * Custom exception thrown when validation fails.
 *
 * This exception is caught by StatusPages and converted to an error response.
 */
public class AtelierValidationException(
    public val validationResult: ValidationResult.Failure
) : Exception("Validation failed with ${validationResult.errorCount} error(s)")

public inline fun <reified T : Any> ApplicationCall.getValidator(): AtelierValidatorContract<T>? {
    val config = application.attributes.getOrNull(AtelierValidatorConfigKey) ?: return null
    val anyValidator = config.validators[T::class] ?: return null

    return object : AtelierValidatorContract<T> {
        override fun validate(obj: T): ValidationResult =
            anyValidator.validate(obj)

        override fun validateFirst(obj: T): ValidationResult =
            anyValidator.validateFirst(obj)
    }
}
