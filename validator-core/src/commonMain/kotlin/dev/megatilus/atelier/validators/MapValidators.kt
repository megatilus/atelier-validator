/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Map must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it.isNotEmpty() }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.isEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Map must be empty",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.isEmpty() }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Map size must be between $min and $max"
                min != null -> "Map must contain at least $min entries"
                max != null -> "Map must contain at most $max entries"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { map ->
            val size = map.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return size(min = min, message = message)
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return size(max = max, message = message)
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.exactSize(
    expectedSize: Int,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Must contain exactly $expectedSize entries",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.size == expectedSize }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.containsKey(
    key: K,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Map must contain key $key",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it.containsKey(key) }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.containsKeys(
    vararg keys: K,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Map must contain all keys: ${keys.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { map -> keys.all { it in map.keys } }
    )
}

public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>>.doesNotContainKey(
    key: K,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>> {
    return constraint(
        hint = message ?: "Map must not contain key $key",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { !it.containsKey(key) }
    )
}

@JvmName("notEmptyNullable")
public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>?> {
    return constraint(
        hint = message ?: "Map must not be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it != null && it.isNotEmpty() }
    )
}

@JvmName("sizeNullable")
public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>?> {
    val hint =
        message
            ?: when {
                min != null && max != null -> "Map size must be between $min and $max"
                min != null -> "Map must contain at least $min entries"
                max != null -> "Map must contain at most $max entries"
                else -> "Invalid size constraint"
            }

    return constraint(
        hint = hint,
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { map ->
            if (map == null) return@constraint true
            val size = map.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

@JvmName("minSizeNullable")
public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>?>.minSize(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>?> {
    return size(min = min, message = message)
}

@JvmName("maxSizeNullable")
public fun <T : Any, K, V> FieldValidatorBuilder<T, Map<K, V>?>.maxSize(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, Map<K, V>?> {
    return size(max = max, message = message)
}
