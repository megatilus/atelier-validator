/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

/**
 * Represents a single validation error with detailed information about what failed.
 */
public data class ValidationErrorDetail(
    val fieldName: String,
    val message: String,
    val code: ValidatorCode,
    val actualValue: String
) {
    override fun toString(): String = "$fieldName: $message"
}
