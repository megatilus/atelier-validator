/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

import kotlin.jvm.JvmInline

/** * Semantic error codes for validation failures.
 * * Implemented as a value class to provide standard codes while
 * allowing developers to create their own custom error codes.
 */
@JvmInline
public value class ValidationErrorCode(public val code: String) {
    public companion object {
        public val REQUIRED: ValidationErrorCode = ValidationErrorCode("required")
        public val TOO_SHORT: ValidationErrorCode = ValidationErrorCode("too_short")
        public val TOO_LONG: ValidationErrorCode = ValidationErrorCode("too_long")
        public val OUT_OF_RANGE: ValidationErrorCode = ValidationErrorCode("out_of_range")
        public val INVALID_EMAIL: ValidationErrorCode = ValidationErrorCode("invalid_email")
        public val INVALID_FORMAT: ValidationErrorCode = ValidationErrorCode("invalid_format")
        public val INVALID_VALUE: ValidationErrorCode = ValidationErrorCode("invalid_value")
        public val WEAK_PASSWORD: ValidationErrorCode = ValidationErrorCode("weak_password")
    }
}
