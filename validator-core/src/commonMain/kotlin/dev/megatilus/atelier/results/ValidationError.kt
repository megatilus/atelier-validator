/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

/**
 * Represents a single validation error with detailed information about what failed.
 */
public data class ValidationError(
    val fieldName: String,
    val message: String,
    val code: ValidationErrorCode,
    val actualValue: String?
) {
    /**
     * Returns the error code as a string (lowercase).
     *
     * Example: "invalid_email"
     */
    public val codeString: String
        get() = code.code

    /**
     * Returns a formatted error for logging/debugging.
     *
     * Example: "email: Must be a valid email"
     */
    override fun toString(): String = "$fieldName: $message [$codeString]"

    /**
     * Returns a simple field: message format.
     *
     * Example: "email: Must be a valid email"
     */
    public fun toSimpleString(): String = "$fieldName: $message"
}
