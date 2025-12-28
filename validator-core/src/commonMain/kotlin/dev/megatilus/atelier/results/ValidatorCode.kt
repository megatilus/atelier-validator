/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

/** Semantic error codes for validation failures. */
public enum class ValidatorCode {
    REQUIRED,
    MIN,
    MAX,
    OUT_OF_RANGE,
    TOO_SHORT,
    TOO_LONG,
    INVALID_EMAIL,
    INVALID_FORMAT,
    PATTERN,
    INVALID_VALUE,
    WEAK_PASSWORD,
    CUSTOM_ERROR,
    CROSS_FIELD_ERROR
}
