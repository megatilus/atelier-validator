/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return constraint(
        hint = message ?: "Array must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it.isNotEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.isEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return constraint(
        hint = message ?: "Array must be empty",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.isEmpty() }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Array size must be between $min and $max"
                min != null -> "Array must contain at least $min elements"
                max != null -> "Array must contain at most $max elements"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { array ->
            val size = array.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return size(min = min, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return size(max = max, message = message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.exactSize(
    expectedSize: Int,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return constraint(
        hint = message ?: "Array must contain exactly $expectedSize elements",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.size == expectedSize }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>>.contains(
    element: R,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>> {
    return constraint(
        hint = message ?: "Array must contain $element",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.contains(element) }
    )
}

@JvmName("notEmptyNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Array<R>?> {
    return constraint(
        hint = message ?: "Array must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it != null && it.isNotEmpty() }
    )
}

@JvmName("sizeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, Array<R>?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Array<R>?> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Array size must be between $min and $max"
                min != null -> "Array must contain at least $min elements"
                max != null -> "Array must contain at most $max elements"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { array ->
            if (array == null) return@constraint true
            val size = array.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}
