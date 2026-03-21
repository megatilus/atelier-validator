/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.internal.AtelierValidatorImpl
import dev.megatilus.atelier.results.ValidationResult

/**
 * Validator interface for validating objects of type [T].
 */
public interface AtelierValidator<T : Any> {
    /**
     * Validates the given object against all configured validation rules.
     */
    public fun validate(obj: T): ValidationResult

    /**
     * Validates the given object and returns only the first error encountered.
     */
    public fun validateFirst(obj: T): ValidationResult

    public companion object {
        /**
         * Creates a new validator with DSL configuration.
         *
         * Example:
         * ```kotlin
         * val userValidator = AtelierValidator<User> {
         *     User::name {
         *         notBlank() hint "Name is required"
         *         minLength(2)
         *     }
         * }
         * ```
         */
        public inline operator fun <reified T : Any> invoke(
            configure: ValidationScope<T>.() -> Unit
        ): AtelierValidator<T> {
            val validator = AtelierValidatorImpl<T>()
            val scope = ValidationScope(validator)
            scope.configure()
            return validator
        }
    }
}
