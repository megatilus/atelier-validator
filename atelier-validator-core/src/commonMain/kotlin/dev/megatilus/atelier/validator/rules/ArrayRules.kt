/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.rules

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.Rule
import dev.megatilus.atelier.validator.ValidationRule
import dev.megatilus.atelier.validator.ValidationScope
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.collections.contains
import kotlin.collections.isNotEmpty
import kotlin.jvm.JvmName

/**
 * Validates that an array is not empty.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     isNotEmpty() hint "At least one tag required"
 * }
 *
 * validator.validate(User(tags = arrayOf("kotlin"))) // Success
 * validator.validate(User(tags = emptyArray())) // Failure
 * validator.validate(User(tags = null)) // Failure
 * ```
 */
@JvmName("isNotEmptyArray")
public fun <T> ValidationRule<Array<out T>?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/**
 * Validates that an array is empty.
 *
 * Example:
 * ```kotlin
 * User::errors {
 *     isEmpty() hint "Should have no errors"
 * }
 *
 * validator.validate(User(errors = emptyArray())) // Success
 * validator.validate(User(errors = null)) // Success
 * validator.validate(User(errors = arrayOf("error"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.isEmpty(): Rule = constrain(
    message = "Must be empty",
    code = ValidationErrorCode("must_be_empty"),
    predicate = { it.isNullOrEmpty() }
)

/**
 * Validates array size within a range.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     size(1..10) hint "Must have 1-10 tags"
 * }
 *
 * validator.validate(User(tags = arrayOf("kotlin", "java"))) // Success
 * validator.validate(User(tags = emptyArray())) // Failure
 * validator.validate(User(tags = Array(15) { "tag$it" })) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.size(range: IntRange): Rule = constrainIfNotNull(
    message = "Size must be between ${range.first} and ${range.last}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size in range }
)

/**
 * Validates minimum array size.
 *
 * Example:
 * ```kotlin
 * User::authors {
 *     minSize(1) hint "At least one author required"
 * }
 *
 * validator.validate(User(authors = arrayOf("John"))) // Success
 * validator.validate(User(authors = emptyArray())) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.minSize(min: Int): Rule = constrainIfNotNull(
    message = "Must contain at least $min items",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size >= min }
)

/**
 * Validates maximum array size.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     maxSize(10) hint "Maximum 10 tags allowed"
 * }
 *
 * validator.validate(User(tags = arrayOf("kotlin"))) // Success
 * validator.validate(User(tags = Array(15) { "tag$it" })) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.maxSize(max: Int): Rule = constrainIfNotNull(
    message = "Must contain at most $max items",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size <= max }
)

/**
 * Validates exact array size.
 *
 * Example:
 * ```kotlin
 * User::rgb {
 *     exactSize(3) hint "RGB must have exactly 3 values"
 * }
 *
 * validator.validate(User(rgb = arrayOf(255, 128, 0))) // Success
 * validator.validate(User(rgb = arrayOf(255, 128))) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.exactSize(size: Int): Rule = constrainIfNotNull(
    message = "Must contain exactly $size items",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size == size }
)

/**
 * Validates that an array contains a specific element.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     contains("admin") hint "Must have admin role"
 * }
 *
 * validator.validate(User(roles = arrayOf("admin", "user"))) // Success
 * validator.validate(User(roles = arrayOf("user"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.contains(element: T): Rule = constrainIfNotNull(
    message = "Must contain $element",
    code = ValidationErrorCode("missing_element"),
    predicate = { it.contains(element) }
)

/**
 * Validates that an array does not contain a specific element.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     doesNotContain("guest") hint "Guest role not allowed"
 * }
 *
 * validator.validate(User(roles = arrayOf("admin", "user"))) // Success
 * validator.validate(User(roles = arrayOf("guest", "user"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Array<out T>?>.doesNotContain(element: T): Rule = constrainIfNotNull(
    message = "Must not contain $element",
    code = ValidationErrorCode("forbidden_element"),
    predicate = { element !in it }
)

/**
 * Validates each element in an array using an existing validator.
 *
 * Example:
 * ```kotlin
 * val authorValidator = AtelierValidator<Author> {
 *     Author::name { notBlank() }
 *     Author::email { email() }
 * }
 *
 * User::authors {
 *     each(authorValidator) hint "All authors must be valid"
 * }
 *
 * validator.validate(User(authors = arrayOf(Author("John", "john@example.com")))) // Success
 * validator.validate(User(authors = arrayOf(Author("", "invalid")))) // Failure
 * ```
 */
public fun <T : Any> ValidationRule<Array<out T>?>.each(
    validator: AtelierValidator<T>
): Rule = constrainIfNotNull(
    message = "All elements must be valid",
    code = ValidationErrorCode.Companion.INVALID_VALUE,
    predicate = { array ->
        array.all { element ->
            validator.validate(element) is ValidationResult.Success
        }
    }
)

/**
 * Validates each element in an array using inline validation rules.
 *
 * Example:
 * ```kotlin
 * User::authors {
 *     isNotEmpty() hint "At least one author required"
 *
 *     each {
 *         Author::name { notBlank() hint "Author name required" }
 *         Author::email { email() hint "Invalid author email" }
 *     }
 * }
 *
 * validator.validate(User(authors = arrayOf(Author("John Doe", "john@example.com")))) // Success
 * validator.validate(User(authors = arrayOf(Author("J", "invalid-email")))) // Failure
 * ```
 */
public inline fun <reified T : Any> ValidationRule<Array<out T>?>.each(
    crossinline configure: ValidationScope<T>.() -> Unit
): Rule {
    val validator = AtelierValidator.Companion<T> {
        configure()
    }
    return each(validator)
}

/** Validates that an IntArray is not empty. */
@JvmName("isNotEmptyIntArray")
public fun ValidationRule<IntArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a LongArray is not empty. */
@JvmName("isNotEmptyLongArray")
public fun ValidationRule<LongArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a FloatArray is not empty. */
@JvmName("isNotEmptyFloatArray")
public fun ValidationRule<FloatArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a DoubleArray is not empty. */
@JvmName("isNotEmptyDoubleArray")
public fun ValidationRule<DoubleArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a BooleanArray is not empty. */
@JvmName("isNotEmptyBooleanArray")
public fun ValidationRule<BooleanArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a ByteArray is not empty. */
@JvmName("isNotEmptyByteArray")
public fun ValidationRule<ByteArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a ShortArray is not empty. */
@JvmName("isNotEmptyShortArray")
public fun ValidationRule<ShortArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/** Validates that a CharArray is not empty. */
@JvmName("isNotEmptyCharArray")
public fun ValidationRule<CharArray?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)
