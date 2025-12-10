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
 * Allows registration of validators and customization of error responses.
 */
public class AtelierValidatorConfig {
    /**
     * Map of registered validators by class type.
     */
    public val validators: MutableMap<KClass<*>, AtelierValidatorContract<Any>> = mutableMapOf()

    /**
     * HTTP status code to use for validation error responses.
     * Defaults to 400 Bad Request.
     */
    public var errorStatusCode: HttpStatusCode = HttpStatusCode.BadRequest

    /**
     * If true, integrates automatically with Ktor's RequestValidation plugin.
     * If false, validation must be done manually via receiveAndValidate().
     *
     * Default: true (automatic validation recommended)
     */
    public var useAutomaticValidation: Boolean = true

    /**
     * Builder function to customize validation error responses.
     *
     * By default, returns [AtelierValidationErrorResponse].
     * Override this to provide custom error formats.
     */
    public var errorResponseBuilder: (ValidationResult.Failure) -> Any = { failure ->
        AtelierValidationErrorResponse.from(failure)
    }

    /**
     * Registers a validator for a specific type.
     *
     * Example:
     * ```kotlin
     * install(AtelierValidator) {
     *     register(userValidator)
     *     register(productValidator)
     * }
     * ```
     *
     * @param T The type to validate
     * @param validator The validator instance for this type
     */
    public inline fun <reified T : Any> register(validator: AtelierValidatorContract<T>) {
        val kClass = T::class
        val wrapper = object : AtelierValidatorContract<Any> {
            override fun validate(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        "Expected instance of ${kClass.simpleName} but got ${obj::class.simpleName}"
                    )
                }

                return validator.validate(obj as T)
            }

            override fun validateFirst(obj: Any): ValidationResult {
                if (!kClass.isInstance(obj)) {
                    throw IllegalArgumentException(
                        "Expected instance of ${kClass.simpleName} but got ${obj::class.simpleName}"
                    )
                }

                return validator.validateFirst(obj as T)
            }
        }

        validators[kClass] = wrapper
    }
}
