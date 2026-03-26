/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.rules

import dev.megatilus.atelier.validator.Rule
import dev.megatilus.atelier.validator.ValidationRule
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import kotlin.jvm.JvmName

/**
 * Validates minimum Int value.
 *
 * Example:
 * ```kotlin
 * User::age {
 *     min(18) hint "Must be at least 18 years old"
 * }
 *
 * validator.validate(User(age = 25)) // Success
 * validator.validate(User(age = 16)) // Failure
 * ```
 */
public fun ValidationRule<Int?>.min(minValue: Int): Rule = constrainIfNotNull(
    message = "Must be at least $minValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it >= minValue }
)

/**
 * Validates maximum Int value.
 *
 * Example:
 * ```kotlin
 * User::age {
 *     max(120) hint "Age cannot exceed 120"
 * }
 *
 * validator.validate(User(age = 25)) // Success
 * validator.validate(User(age = 150)) // Failure
 * ```
 */
public fun ValidationRule<Int?>.max(maxValue: Int): Rule = constrainIfNotNull(
    message = "Must be at most $maxValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it <= maxValue }
)

/**
 * Validates Int within a range.
 *
 * Example:
 * ```kotlin
 * User::rating {
 *     range(1..5) hint "Rating must be between 1 and 5"
 * }
 *
 * validator.validate(User(rating = 3)) // Success
 * validator.validate(User(rating = 6)) // Failure
 * ```
 */
public fun ValidationRule<Int?>.range(range: IntRange): Rule = constrainIfNotNull(
    message = "Must be between ${range.first} and ${range.last}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it in range }
)

/**
 * Validates that an Int is positive (> 0).
 *
 * Example:
 * ```kotlin
 * Product::price {
 *     isPositive() hint "Price must be positive"
 * }
 *
 * validator.validate(Product(price = 100)) // Success
 * validator.validate(Product(price = 0)) // Failure
 * validator.validate(Product(price = -10)) // Failure
 * ```
 */
@JvmName("isPositiveInt")
public fun ValidationRule<Int?>.isPositive(): Rule = constrainIfNotNull(
    message = "Must be positive",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it > 0 }
)

/**
 * Validates that an Int is negative (< 0).
 *
 * Example:
 * ```kotlin
 * Transaction::debit {
 *     isNegative() hint "Debit must be negative"
 * }
 *
 * validator.validate(Transaction(debit = -100)) // Success
 * validator.validate(Transaction(debit = 0)) // Failure
 * ```
 */
@JvmName("isNegativeInt")
public fun ValidationRule<Int?>.isNegative(): Rule = constrainIfNotNull(
    message = "Must be negative",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it < 0 }
)

/**
 * Validates minimum Long value.
 *
 * Example:
 * ```kotlin
 * File::size {
 *     min(1024L) hint "File must be at least 1KB"
 * }
 *
 * validator.validate(File(size = 2048L)) // Success
 * validator.validate(File(size = 512L)) // Failure
 * ```
 */
public fun ValidationRule<Long?>.min(minValue: Long): Rule = constrainIfNotNull(
    message = "Must be at least $minValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it >= minValue }
)

/**
 * Validates maximum Long value.
 *
 * Example:
 * ```kotlin
 * File::size {
 *     max(10_000_000L) hint "File cannot exceed 10MB"
 * }
 *
 * validator.validate(File(size = 5_000_000L)) // Success
 * validator.validate(File(size = 15_000_000L)) // Failure
 * ```
 */
public fun ValidationRule<Long?>.max(maxValue: Long): Rule = constrainIfNotNull(
    message = "Must be at most $maxValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it <= maxValue }
)

/**
 * Validates Long within a range.
 *
 * Example:
 * ```kotlin
 * User::timestamp {
 *     range(0L..System.currentTimeMillis())
 * }
 *
 * validator.validate(User(timestamp = 1000L)) // Success
 * validator.validate(User(timestamp = -1L)) // Failure
 * ```
 */
public fun ValidationRule<Long?>.range(range: LongRange): Rule = constrainIfNotNull(
    message = "Must be between ${range.first} and ${range.last}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it in range }
)

/**
 * Validates that a Long is positive (> 0).
 *
 * Example:
 * ```kotlin
 * Transaction::amount {
 *     isPositive() hint "Amount must be positive"
 * }
 *
 * validator.validate(Transaction(amount = 100L)) // Success
 * validator.validate(Transaction(amount = -10L)) // Failure
 * ```
 */
@JvmName("isPositiveLong")
public fun ValidationRule<Long?>.isPositive(): Rule = constrainIfNotNull(
    message = "Must be positive",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it > 0 }
)

/**
 * Validates that a Long is negative (< 0).
 *
 * Example:
 * ```kotlin
 * Adjustment::value {
 *     isNegative() hint "Adjustment must be negative"
 * }
 *
 * validator.validate(Adjustment(value = -100L)) // Success
 * validator.validate(Adjustment(value = 100L)) // Failure
 * ```
 */
@JvmName("isNegativeLong")
public fun ValidationRule<Long?>.isNegative(): Rule = constrainIfNotNull(
    message = "Must be negative",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it < 0 }
)

/**
 * Validates minimum Float value.
 *
 * Example:
 * ```kotlin
 * Product::weight {
 *     min(0.1f) hint "Weight must be at least 0.1kg"
 * }
 *
 * validator.validate(Product(weight = 1.5f)) // Success
 * validator.validate(Product(weight = 0.05f)) // Failure
 * ```
 */
public fun ValidationRule<Float?>.min(minValue: Float): Rule = constrainIfNotNull(
    message = "Must be at least $minValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it >= minValue }
)

/**
 * Validates maximum Float value.
 *
 * Example:
 * ```kotlin
 * Product::discount {
 *     max(0.9f) hint "Discount cannot exceed 90%"
 * }
 *
 * validator.validate(Product(discount = 0.5f)) // Success
 * validator.validate(Product(discount = 0.95f)) // Failure
 * ```
 */
public fun ValidationRule<Float?>.max(maxValue: Float): Rule = constrainIfNotNull(
    message = "Must be at most $maxValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it <= maxValue }
)

/**
 * Validates Float within a range.
 *
 * Example:
 * ```kotlin
 * Sensor::temperature {
 *     range(-40.0f..85.0f) hint "Temperature out of sensor range"
 * }
 *
 * validator.validate(Sensor(temperature = 25.0f)) // Success
 * validator.validate(Sensor(temperature = 100.0f)) // Failure
 * ```
 */
@JvmName("rangeFloat")
public fun ValidationRule<Float?>.range(range: ClosedFloatingPointRange<Float>): Rule = constrainIfNotNull(
    message = "Must be between ${range.start} and ${range.endInclusive}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it in range }
)

/**
 * Validates that a Float is positive (> 0).
 *
 * Example:
 * ```kotlin
 * Product::price {
 *     isPositive() hint "Price must be positive"
 * }
 *
 * validator.validate(Product(price = 19.99f)) // Success
 * validator.validate(Product(price = -5.0f)) // Failure
 * ```
 */
@JvmName("isPositiveFloat")
public fun ValidationRule<Float?>.isPositive(): Rule = constrainIfNotNull(
    message = "Must be positive",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it > 0 }
)

/**
 * Validates that a Float is negative (< 0).
 *
 * Example:
 * ```kotlin
 * Temperature::celsius {
 *     isNegative() hint "Temperature must be below freezing"
 * }
 *
 * validator.validate(Temperature(celsius = -10.0f)) // Success
 * validator.validate(Temperature(celsius = 5.0f)) // Failure
 * ```
 */
@JvmName("isNegativeFloat")
public fun ValidationRule<Float?>.isNegative(): Rule = constrainIfNotNull(
    message = "Must be negative",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it < 0 }
)

/**
 * Validates minimum Double value.
 *
 * Example:
 * ```kotlin
 * Coordinate::latitude {
 *     min(-90.0) hint "Latitude must be >= -90"
 * }
 *
 * validator.validate(Coordinate(latitude = 48.8566)) // Success
 * validator.validate(Coordinate(latitude = -100.0)) // Failure
 * ```
 */
public fun ValidationRule<Double?>.min(minValue: Double): Rule = constrainIfNotNull(
    message = "Must be at least $minValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it >= minValue }
)

/**
 * Validates maximum Double value.
 *
 * Example:
 * ```kotlin
 * Coordinate::latitude {
 *     max(90.0) hint "Latitude must be <= 90"
 * }
 *
 * validator.validate(Coordinate(latitude = 48.8566)) // Success
 * validator.validate(Coordinate(latitude = 100.0)) // Failure
 * ```
 */
public fun ValidationRule<Double?>.max(maxValue: Double): Rule = constrainIfNotNull(
    message = "Must be at most $maxValue",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it <= maxValue }
)

/**
 * Validates Double within a range.
 *
 * Example:
 * ```kotlin
 * Coordinate::latitude {
 *     range(-90.0..90.0) hint "Latitude must be between -90 and 90"
 * }
 *
 * validator.validate(Coordinate(latitude = 48.8566)) // Success
 * validator.validate(Coordinate(latitude = 100.0)) // Failure
 * ```
 */
@JvmName("rangeDouble")
public fun ValidationRule<Double?>.range(range: ClosedFloatingPointRange<Double>): Rule = constrainIfNotNull(
    message = "Must be between ${range.start} and ${range.endInclusive}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it in range }
)

/**
 * Validates that a Double is positive (> 0).
 *
 * Example:
 * ```kotlin
 * Account::balance {
 *     isPositive() hint "Balance must be positive"
 * }
 *
 * validator.validate(Account(balance = 1000.50)) // Success
 * validator.validate(Account(balance = -50.0)) // Failure
 * ```
 */
@JvmName("isPositiveDouble")
public fun ValidationRule<Double?>.isPositive(): Rule = constrainIfNotNull(
    message = "Must be positive",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it > 0 }
)

/**
 * Validates that a Double is negative (< 0).
 *
 * Example:
 * ```kotlin
 * Transaction::debit {
 *     isNegative() hint "Debit must be negative"
 * }
 *
 * validator.validate(Transaction(debit = -100.0)) // Success
 * validator.validate(Transaction(debit = 50.0)) // Failure
 * ```
 */
@JvmName("isNegativeDouble")
public fun ValidationRule<Double?>.isNegative(): Rule = constrainIfNotNull(
    message = "Must be negative",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it < 0 }
)
