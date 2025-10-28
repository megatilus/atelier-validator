/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "List must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it.isNotEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.isEmpty(
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "List must be empty",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.isEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "List size must be between $min and $max"
                min != null -> "List must contain at least $min items"
                max != null -> "List must contain at most $max items"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { list ->
            val size = list.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return size(min = min, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return size(max = max, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.exactSize(
    expectedSize: Int,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "Must contain exactly $expectedSize items",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.size == expectedSize }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.contains(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "Must contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.contains(element) }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.doesNotContain(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "Must not contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { !it.contains(element) }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.containsAll(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "Must contain all elements: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { list -> elements.all { it in list } }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, List<R>>.containsAny(
    vararg elements: R,
    message: String? = null
): FieldValidatorBuilder<T, List<R>> {
    return constraint(
        hint = message ?: "Must contain at least one of: ${elements.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { list -> elements.any { it in list } }
    )
}

@JvmName("notEmptyNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, List<R>?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, List<R>?> {
    return constraint(
        hint = message ?: "List must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it != null && it.isNotEmpty() }
    )
}

@JvmName("sizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, List<R>?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, List<R>?> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "List size must be between $min and $max"
                min != null -> "List must contain at least $min items"
                max != null -> "List must contain at most $max items"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { list ->
            if (list == null) return@constraint true
            val size = list.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

@JvmName("minSizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, List<R>?>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, List<R>?> {
    return size(min = min, message = message)
}

@JvmName("maxSizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, List<R>?>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, List<R>?> {
    return size(max = max, message = message)
}
