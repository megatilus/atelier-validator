/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

/* ======================== SET VALIDATORS ======================== */

/**
 * Validates that the set is not empty.
 *
 * Example:
 * ```kotlin
 * field(User::roles).notEmpty("User must have at least one role")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Set must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it.isNotEmpty() }
    )
}

/**
 * Validates that the set is empty.
 *
 * Example:
 * ```kotlin
 * field(User::pendingActions).isEmpty("No pending actions allowed")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.isEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Set must be empty",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.isEmpty() }
    )
}

/**
 * Validates that the set size is within the specified range.
 *
 * Example:
 * ```kotlin
 * field(User::skills).size(min = 1, max = 5)
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Set size must be between $min and $max"
                min != null -> "Set must contain at least $min items"
                max != null -> "Set must contain at most $max items"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { set ->
            val size = set.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

/**
 * Validates that the set contains at least the specified number of items.
 *
 * Example:
 * ```kotlin
 * field(User::roles).minSize(1)
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return size(min = min, message = message)
}

/**
 * Validates that the set contains at most the specified number of items.
 *
 * Example:
 * ```kotlin
 * field(User::categories).maxSize(3)
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return size(max = max, message = message)
}

/**
 * Validates that the set contains exactly the specified number of items.
 *
 * Example:
 * ```kotlin
 * field(Team::members).exactSize(5)
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.exactSize(
    expectedSize: Int,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Must contain exactly $expectedSize items",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.size == expectedSize }
    )
}

/**
 * Validates that the set contains the specified element.
 *
 * Example:
 * ```kotlin
 * field(User::permissions).contains("READ")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.contains(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Must contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.contains(element) }
    )
}

/**
 * Validates that the set does not contain the specified element.
 *
 * Example:
 * ```kotlin
 * field(User::permissions).doesNotContain("DELETE")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.doesNotContain(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Must not contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { !it.contains(element) }
    )
}

/**
 * Validates that the set contains all specified elements.
 *
 * Example:
 * ```kotlin
 * field(User::permissions).containsAll("READ", "WRITE")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.containsAll(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Must contain all elements: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { set -> elements.all { it in set } }
    )
}

/**
 * Validates that the set contains at least one of the specified elements.
 *
 * Example:
 * ```kotlin
 * field(User::roles).containsAny("ADMIN", "MODERATOR")
 * ```
 */
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>>.containsAny(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>> {
    return constraint(
        hint = message ?: "Must contain at least one of: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { set -> elements.any { it in set } }
    )
}

/* ======================== NULLABLE SET VALIDATORS ======================== */

@JvmName("notEmptyNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Set<R>?> {
    return constraint(
        hint = message ?: "Set must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it != null && it.isNotEmpty() }
    )
}

@JvmName("sizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>?> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Set size must be between $min and $max"
                min != null -> "Set must contain at least $min items"
                max != null -> "Set must contain at most $max items"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { set ->
            if (set == null) return@constraint true
            val size = set.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

@JvmName("minSizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>?>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>?> {
    return size(min = min, message = message)
}

@JvmName("maxSizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Set<R>?>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Set<R>?> {
    return size(max = max, message = message)
}
