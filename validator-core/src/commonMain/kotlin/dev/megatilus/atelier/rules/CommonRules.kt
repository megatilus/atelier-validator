/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.AtelierValidator
import dev.megatilus.atelier.Rule
import dev.megatilus.atelier.ValidationRule
import dev.megatilus.atelier.results.ValidationErrorCode
import dev.megatilus.atelier.results.ValidationResult

/**
 * Validates that a value is not null.
 *
 * Example:
 * ```kotlin
 * User::name {
 *     notNull()
 * }
 *
 * validator.validate(User(name = "John")) // Success
 * validator.validate(User(name = null)) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.notNull(): Rule = constrain(
    message = "Must not be null",
    code = ValidationErrorCode.REQUIRED,
    predicate = { it != null }
)

/**
 * Validates that a value is null.
 *
 * Example:
 * ```kotlin
 * User::deletedAt {
 *     isNull() hint "User must not be deleted"
 * }
 *
 * validator.validate(User(deletedAt = null)) // Success
 * validator.validate(User(deletedAt = "2025-03-14")) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.isNull(): Rule = constrain(
    message = "Must be null",
    code = ValidationErrorCode("must_be_null"),
    predicate = { it == null }
)

/**
 * Validates that a value equals the expected value.
 *
 * Example:
 * ```kotlin
 * User::status {
 *     equalTo("active") hint "User must be active"
 * }
 *
 * validator.validate(User(status = "active")) // Success
 * validator.validate(User(status = "inactive")) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.equalTo(expected: T): Rule = constrainIfNotNull(
    message = "Must be equal to $expected",
    code = ValidationErrorCode("must_equal"),
    predicate = { it == expected }
)

/**
 * Validates that a value does not equal the given value.
 *
 * Example:
 * ```kotlin
 * User::status {
 *     notEqualTo("banned") hint "User cannot be banned"
 * }
 *
 * validator.validate(User(status = "active")) // Success
 * validator.validate(User(status = "banned")) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.notEqualTo(value: T): Rule = constrainIfNotNull(
    message = "Must not be equal to $value",
    code = ValidationErrorCode("must_not_equal"),
    predicate = { it != value }
)

/**
 * Validates that a value is in the given collection.
 *
 * Example:
 * ```kotlin
 * User::role {
 *     isIn(listOf("admin", "user", "moderator")) hint "Invalid role"
 * }
 *
 * validator.validate(User(role = "admin")) // Success
 * validator.validate(User(role = "guest")) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.isIn(values: Collection<T>): Rule = constrainIfNotNull(
    message = "Must be one of: ${values.joinToString(", ")}",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = { it in values }
)

/**
 * Validates that a value is not in the given collection.
 *
 * Example:
 * ```kotlin
 * User::username {
 *     isNotIn(listOf("admin", "root", "system")) hint "Username is reserved"
 * }
 *
 * validator.validate(User(username = "john")) // Success
 * validator.validate(User(username = "admin")) // Failure
 * ```
 */
public fun <T> ValidationRule<T?>.isNotIn(values: Collection<T>): Rule = constrainIfNotNull(
    message = "Must not be one of: ${values.joinToString(", ")}",
    code = ValidationErrorCode("forbidden_value"),
    predicate = { it !in values }
)

/**
 * Validates a nested object using an existing validator.
 *
 * Example:
 * ```kotlin
 * val addressValidator = AtelierValidator<Address> {
 *     Address::city { notBlank() }
 * }
 *
 * UserWithAddress::address {
 *     nested(addressValidator)
 * }
 *
 * validator.validate(UserWithAddress(address = Address(city = ""))) // Failure
 * validator.validate(UserWithAddress(address = Address(city = "Paris"))) // Success
 * ```
 */
public fun <T : Any> ValidationRule<T?>.nested(
    validator: AtelierValidator<T>
): Rule = constrainIfNotNull(
    message = "Nested object validation failed",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = { obj ->
        validator.validate(obj) is ValidationResult.Success
    }
)
