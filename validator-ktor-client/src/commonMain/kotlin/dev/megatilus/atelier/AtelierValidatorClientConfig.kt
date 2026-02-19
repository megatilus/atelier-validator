/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.http.*
import kotlin.reflect.KClass

/**
 * Configuration for the Atelier Validator client plugin.
 *
 * This class allows registration of validators for different response types.
 *
 * **Use Case**: Validate responses from external/third-party APIs to ensure they
 * match your expected contract.
 *
 * Example:
 * ```kotlin
 * val client = HttpClient {
 *     install(AtelierValidatorClient) {
 *         register(userValidator)
 *         register(productValidator)
 *     }
 * }
 * ```
 */
public class AtelierValidatorClientConfig {

    /**
     * Map of registered validators by class type.
     *
     * Each validator is stored with its corresponding KClass as the key,
     * allowing type-safe retrieval during response validation.
     */
    public val validators: MutableMap<KClass<*>, AtelierValidatorContract<Any>> = mutableMapOf()

    /**
     * Determines whether to validate responses automatically.
     *
     * When true, all responses for registered types are validated automatically.
     *
     * When false, validation must be performed manually using extension functions.
     *
     * Default: true
     */
    public var useAutomaticValidation: Boolean = true

    /**
     * Registers a validator for a specific response type.
     *
     * This function associates a validator with a data type, enabling
     * validation of responses containing that type.
     *
     * Example:
     * ```kotlin
     * val userValidator = atelierValidator<User> {
     *     User::id { notBlank() }
     *     User::email { email() }
     *     User::age { min(0); max(150) }
     * }
     *
     * val client = HttpClient {
     *     install(AtelierValidatorClient) {
     *         register(userValidator)
     *     }
     * }
     * ```
     *
     * @param T The response type to validate
     * @param validator The validator instance for this type
     * @throws IllegalArgumentException if an object of incorrect type is validated
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
