/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.constraints

import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidatorCode

/**
 * Represents a single validation constraint that can be applied to a field value.
 *
 * @param R The type of the field being validated
 * @param hint The error message if the constraint fails
 * @param code The error code associated with this constraint (default: CUSTOM_ERROR)
 * @param predicate Function returning true if the value passes the constraint
 */
internal data class Constraint<R>(
    val hint: String,
    val code: ValidatorCode = ValidatorCode.CUSTOM_ERROR,
    val predicate: (R) -> Boolean
) {
    /**
     * Validates the given value against this constraint.
     *
     * @param value The value of the field to validate
     * @param fieldName The name of the field (used in the error message)
     * @return ValidationErrorDetail if the constraint fails, null otherwise
     */
    internal fun validate(value: R, fieldName: String): ValidationErrorDetail? {
        return if (!predicate(value)) {
            ValidationErrorDetail(
                fieldName = fieldName,
                message = hint.replace("{value}", value?.toString() ?: "null"),
                code = code,
                actualValue = value.toString()
            )
        } else {
            null
        }
    }
}
