/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Collection must not be empty",
        code = ValidatorCode.REQUIRED,
        predicate = { it.isNotEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.isEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Collection must be empty",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.isEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    val hint =
        message
            ?: when {
                min != null && max != null ->
                    "Collection size must be between $min and $max"

                min != null -> "Collection must contain at least $min items"

                max != null -> "Collection must contain at most $max items"

                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { collection ->
            val size = collection.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return size(min = min, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return size(max = max, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.exactSize(
    expectedSize: Int,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Must contain exactly $expectedSize items",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.size == expectedSize }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.contains(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Must contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.contains(element) }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.doesNotContain(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Must not contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { !it.contains(element) }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.containsAll(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Must contain all elements: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { collection -> elements.all { it in collection } }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>>.containsAny(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>> {
    return constraint(
        hint = message ?: "Must contain at least one of: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { collection -> elements.any { it in collection } }
    )
}

@JvmName("notEmptyNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>?> {
    return constraint(
        hint = message ?: "Must not be empty",
        code = ValidatorCode.REQUIRED,
        predicate = { !it.isNullOrEmpty() }
    )
}

@JvmName("sizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Collection<R>?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Collection<R>?> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Size must be between $min and $max"
                min != null -> "Must contain at least $min items"
                max != null -> "Must contain at most $max items"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { collection ->
            if (collection == null) return@constraint true
            val size = collection.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}
