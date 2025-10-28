/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult

/**
 * Contract interface for validators in the Atelier validation framework.
 *
 * This interface defines the core validation operations that all validators must implement. It
 * provides two validation modes:
 * - `validate()`: Returns all validation errors
 * - `validateFirst()` : Returns only the first validation error encountered
 *
 * @param T The type of object this validator can validate
 */
public interface AtelierValidatorContract<T : Any> {
    /**
     * Validates the given object against all configured validation rules.
     *
     * @param obj The object to validate
     * @return [ValidationResult] containing either success or all validation errors
     */
    public fun validate(obj: T): ValidationResult

    /**
     * Validates the given object and returns only the first error encountered.
     *
     * @param obj The object to validate
     * @return [ValidationResult] containing either success or the first validation error
     */
    public fun validateFirst(obj: T): ValidationResult
}
