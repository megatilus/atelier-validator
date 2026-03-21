/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.internal

import dev.megatilus.atelier.results.ValidationError

/**
 * Interface for a validator of a single property.
 *
 * @param R The type of the property being validated
 *
 * Implementations should return a list of ValidationErrorDetail describing
 * all validation errors found for the value.
 */
internal fun interface FieldRule<R> {
    /**
     * Validates a value for a given field.
     *
     * @param value The property value to validate
     * @param fieldName The name of the property (used in error reporting)
     * @return A list of ValidationErrorDetail objects; empty if valid
     */
    fun validate(value: R, fieldName: String): List<ValidationError>
}
