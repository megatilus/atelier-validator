/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any, R> FieldValidatorBuilder<T, R>.isGreaterThanTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be greater than $value",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > value }
    )
}

@JvmName("isGreaterThanNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.isGreaterThanTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be greater than $value",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > value }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.isGreaterThanOrEqualTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return min(value, message)
}

@JvmName("isGreaterThanOrEqualToNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.isGreaterThanOrEqualTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return min(value, message)
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.isLessThan(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be less than $value",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < value }
    )
}

@JvmName("isLessThanNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.isLessThan(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be less than $value",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < value }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.isLessThanOrEqualTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return max(value, message)
}

@JvmName("isLessThanOrEqualToNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.isLessThanOrEqualTo(
    value: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return max(value, message)
}

/** Validates that the field value is at least the specified minimum (inclusive). */
public fun <T : Any, R> FieldValidatorBuilder<T, R>.min(
    minimum: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be at least $minimum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it >= minimum }
    )
}

@JvmName("minNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.min(
    minimum: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be at least $minimum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it >= minimum }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.max(
    maximum: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be at most $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it <= maximum }
    )
}

@JvmName("maxNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.max(
    maximum: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be at most $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it <= maximum }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.range(
    min: R,
    max: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    require(min <= max) { "min must be <= max" }
    return constraint(
        hint = message ?: "Must be between $min and $max (inclusive)",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it in min..max }
    )
}

@JvmName("rangeNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.range(
    min: R,
    max: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    require(min <= max) { "min must be <= max" }
    return constraint(
        hint = message ?: "Must be between $min and $max (inclusive)",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it in min..max }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.between(
    min: R,
    max: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    require(min < max) { "min must be < max for exclusive between" }
    return constraint(
        hint = message ?: "Must be between $min and $max (exclusive)",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > min && it < max }
    )
}

@JvmName("betweenNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.between(
    min: R,
    max: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    require(min < max) { "min must be < max for exclusive between" }
    return constraint(
        hint = message ?: "Must be between $min and $max (exclusive)",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || (it > min && it < max) }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.oneOf(
    vararg values: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be one of: ${values.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it in values }
    )
}

@JvmName("oneOfNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.oneOf(
    vararg values: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must be one of: ${values.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it in values }
    )
}

public fun <T : Any, R> FieldValidatorBuilder<T, R>.notOneOf(
    vararg values: R,
    message: String? = null
): FieldValidatorBuilder<T, R> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must not be one of: ${values.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it !in values }
    )
}

@JvmName("notOneOfNullable")
public fun <T : Any, R> FieldValidatorBuilder<T, R?>.notOneOf(
    vararg values: R,
    message: String? = null
): FieldValidatorBuilder<T, R?> where R : Comparable<R>, R : Number {
    return constraint(
        hint = message ?: "Must not be one of: ${values.joinToString(", ")}",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it !in values }
    )
}

@JvmName("positiveByte")
public fun <T : Any> FieldValidatorBuilder<T, Byte>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Byte> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0 }
    )
}

@JvmName("negativeByte")
public fun <T : Any> FieldValidatorBuilder<T, Byte>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Byte> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0 }
    )
}

@JvmName("isZeroByte")
public fun <T : Any> FieldValidatorBuilder<T, Byte>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Byte> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0.toByte() }
    )
}

@JvmName("positiveShort")
public fun <T : Any> FieldValidatorBuilder<T, Short>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Short> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0 }
    )
}

@JvmName("negativeShort")
public fun <T : Any> FieldValidatorBuilder<T, Short>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Short> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0 }
    )
}

@JvmName("isZeroShort")
public fun <T : Any> FieldValidatorBuilder<T, Short>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Short> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0.toShort() }
    )
}

@JvmName("positiveInt")
public fun <T : Any> FieldValidatorBuilder<T, Int>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Int> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0 }
    )
}

@JvmName("positiveIntNullable")
public fun <T : Any> FieldValidatorBuilder<T, Int?>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Int?> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > 0 }
    )
}

@JvmName("negativeInt")
public fun <T : Any> FieldValidatorBuilder<T, Int>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Int> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0 }
    )
}

@JvmName("negativeIntNullable")
public fun <T : Any> FieldValidatorBuilder<T, Int?>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Int?> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < 0 }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, Int>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Int> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0 }
    )
}

@JvmName("isZeroIntNullable")
public fun <T : Any> FieldValidatorBuilder<T, Int?>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Int?> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == 0 }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, Int>.multipleOf(
    divisor: Int,
    message: String? = null
): FieldValidatorBuilder<T, Int> {
    require(divisor != 0) { "divisor must not be 0" }
    return constraint(
        hint = message ?: "Must be a multiple of $divisor",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it % divisor == 0 }
    )
}

@JvmName("multipleOfIntNullable")
public fun <T : Any> FieldValidatorBuilder<T, Int?>.multipleOf(
    divisor: Int,
    message: String? = null
): FieldValidatorBuilder<T, Int?> {
    require(divisor != 0) { "divisor must not be 0" }
    return constraint(
        hint = message ?: "Must be a multiple of $divisor",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it % divisor == 0 }
    )
}

@JvmName("positiveLong")
public fun <T : Any> FieldValidatorBuilder<T, Long>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Long> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0L }
    )
}

@JvmName("positiveLongNullable")
public fun <T : Any> FieldValidatorBuilder<T, Long?>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Long?> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > 0L }
    )
}

@JvmName("negativeLong")
public fun <T : Any> FieldValidatorBuilder<T, Long>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Long> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0L }
    )
}

@JvmName("negativeLongNullable")
public fun <T : Any> FieldValidatorBuilder<T, Long?>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Long?> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < 0L }
    )
}

@JvmName("isZeroLong")
public fun <T : Any> FieldValidatorBuilder<T, Long>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Long> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0L }
    )
}

@JvmName("isZeroLongNullable")
public fun <T : Any> FieldValidatorBuilder<T, Long?>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Long?> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == 0L }
    )
}

@JvmName("multipleOfLong")
public fun <T : Any> FieldValidatorBuilder<T, Long>.multipleOf(
    divisor: Long,
    message: String? = null
): FieldValidatorBuilder<T, Long> {
    require(divisor != 0L) { "divisor must not be 0" }
    return constraint(
        hint = message ?: "Must be a multiple of $divisor",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it % divisor == 0L }
    )
}

@JvmName("multipleOfLongNullable")
public fun <T : Any> FieldValidatorBuilder<T, Long?>.multipleOf(
    divisor: Long,
    message: String? = null
): FieldValidatorBuilder<T, Long?> {
    require(divisor != 0L) { "divisor must not be 0" }
    return constraint(
        hint = message ?: "Must be a multiple of $divisor",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it % divisor == 0L }
    )
}

@JvmName("positiveFloat")
public fun <T : Any> FieldValidatorBuilder<T, Float>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Float> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0f }
    )
}

@JvmName("positiveFloatNullable")
public fun <T : Any> FieldValidatorBuilder<T, Float?>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Float?> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > 0f }
    )
}

@JvmName("negativeFloat")
public fun <T : Any> FieldValidatorBuilder<T, Float>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Float> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0f }
    )
}

@JvmName("negativeFloatNullable")
public fun <T : Any> FieldValidatorBuilder<T, Float?>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Float?> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < 0f }
    )
}

@JvmName("isZeroFloat")
public fun <T : Any> FieldValidatorBuilder<T, Float>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Float> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0f }
    )
}

@JvmName("isZeroFloatNullable")
public fun <T : Any> FieldValidatorBuilder<T, Float?>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Float?> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == 0f }
    )
}

@JvmName("positiveDouble")
public fun <T : Any> FieldValidatorBuilder<T, Double>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Double> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > 0.0 }
    )
}

@JvmName("positiveDoubleNullable")
public fun <T : Any> FieldValidatorBuilder<T, Double?>.positive(
    message: String? = null
): FieldValidatorBuilder<T, Double?> {
    return constraint(
        hint = message ?: "Must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > 0.0 }
    )
}

@JvmName("negativeDouble")
public fun <T : Any> FieldValidatorBuilder<T, Double>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Double> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < 0.0 }
    )
}

@JvmName("negativeDoubleNullable")
public fun <T : Any> FieldValidatorBuilder<T, Double?>.negative(
    message: String? = null
): FieldValidatorBuilder<T, Double?> {
    return constraint(
        hint = message ?: "Must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < 0.0 }
    )
}

@JvmName("isZeroDouble")
public fun <T : Any> FieldValidatorBuilder<T, Double>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Double> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == 0.0 }
    )
}

@JvmName("isZeroDoubleNullable")
public fun <T : Any> FieldValidatorBuilder<T, Double?>.isZero(
    message: String? = null
): FieldValidatorBuilder<T, Double?> {
    return constraint(
        hint = message ?: "Must be zero",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == 0.0 }
    )
}
