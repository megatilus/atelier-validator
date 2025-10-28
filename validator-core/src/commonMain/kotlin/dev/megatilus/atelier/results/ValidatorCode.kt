/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

/** Semantic error codes for validation failures. */
public enum class ValidatorCode {
    NOT_NULL,
    NOT_BLANK,
    NOT_EMPTY,
    TOO_SHORT,
    TOO_LONG,
    OUT_OF_RANGE,
    INVALID_EMAIL,
    INVALID_FORMAT,
    INVALID_VALUE,
    NULL_VALUE,
    CUSTOM_ERROR,
    CROSS_FIELD_ERROR,
    WEAK_PASSWORD
}
