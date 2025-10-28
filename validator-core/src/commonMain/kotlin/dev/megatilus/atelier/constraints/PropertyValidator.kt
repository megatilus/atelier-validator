/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.constraints

import dev.megatilus.atelier.results.ValidationErrorDetail

/**
 * Interface for a validator of a single property.
 *
 * @param R The type of the property being validated
 *
 * Implementations should return a list of ValidationErrorDetail describing
 * all validation errors found for the value.
 */
public fun interface PropertyValidator<R> {
    /**
     * Validates a value for a given field.
     *
     * @param value The property value to validate
     * @param fieldName The name of the property (used in error reporting)
     * @return A list of ValidationErrorDetail objects; empty if valid
     */
    public fun validate(value: R, fieldName: String): List<ValidationErrorDetail>
}
