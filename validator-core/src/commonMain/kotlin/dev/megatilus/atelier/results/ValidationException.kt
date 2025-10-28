/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

public class ValidationException(validationResult: ValidationResult.Failure) :
    Exception(
        "Validation failed with ${validationResult.errorCount} " +
            "errors: ${validationResult.errors}"
    )
