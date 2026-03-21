/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationErrorCode

/**
 * DSL interface for defining validation rules on a field.
 */
public interface ValidationRule<out R> {
    public val fieldName: String

    // Skip null automatically
    public fun constrain(
        message: String,
        code: ValidationErrorCode,
        predicate: (R) -> Boolean
    ): Rule

    /**
     * Validates ONLY if value is not null.
     * Null values are considered INVALID.
     */
    public fun constrainIfNotNull(
        message: String,
        code: ValidationErrorCode,
        predicate: (R & Any) -> Boolean
    ): Rule

    /**
     * Defines an inline custom validation rule directly in the DSL.
     *
     * Example:
     * ```kotlin
     * User::name {
     *     customRule { it?.startsWith("user_") == true } hint "Must start with user_"
     * }
     * ```
     */
    public fun customRule(predicate: (R) -> Boolean): Rule =
        constrain(
            message = "$fieldName validation failed",
            code = ValidationErrorCode("custom_error"),
            predicate = predicate
        )
}
