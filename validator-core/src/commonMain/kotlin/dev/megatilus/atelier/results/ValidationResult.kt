/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

/**
 * Represents the result of a validation operation.
 */
public sealed class ValidationResult {
    public data object Success : ValidationResult()

    public data class Failure(val errors: List<ValidationErrorDetail>) : ValidationResult() {
        public constructor(error: ValidationErrorDetail) : this(listOf(error))

        val errorCount: Int get() = errors.size
        val errorsByField: Map<String, List<ValidationErrorDetail>>
            get() = errors.groupBy { it.fieldName }

        public fun errorsFor(fieldName: String): List<ValidationErrorDetail> {
            return errors.filter { it.fieldName == fieldName }
        }

        public fun firstErrorFor(fieldName: String): ValidationErrorDetail? {
            return errors.firstOrNull { it.fieldName == fieldName }
        }
    }

    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Failure
}
