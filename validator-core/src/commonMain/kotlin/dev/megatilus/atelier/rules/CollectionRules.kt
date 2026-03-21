/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.AtelierValidator
import dev.megatilus.atelier.Rule
import dev.megatilus.atelier.ValidationRule
import dev.megatilus.atelier.ValidationScope
import dev.megatilus.atelier.results.ValidationErrorCode
import dev.megatilus.atelier.results.ValidationResult

/**
 * Validates that a collection is not empty.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     isNotEmpty() hint "At least one tag required"
 * }
 *
 * validator.validate(User(tags = listOf("kotlin"))) // Success
 * validator.validate(User(tags = emptyList())) // Failure
 * validator.validate(User(tags = null)) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.isNotEmpty(): Rule = constrain(
    message = "Must not be empty",
    code = ValidationErrorCode.REQUIRED,
    predicate = { !it.isNullOrEmpty() }
)

/**
 * Validates that a collection is empty.
 *
 * Example:
 * ```kotlin
 * User::errors {
 *     isEmpty() hint "Should have no errors"
 * }
 *
 * validator.validate(User(errors = emptyList())) // Success
 * validator.validate(User(errors = null)) // Success
 * validator.validate(User(errors = listOf("error"))) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.isEmpty(): Rule = constrain(
    message = "Must be empty",
    code = ValidationErrorCode("must_be_empty"),
    predicate = { it.isNullOrEmpty() }
)

/**
 * Validates collection size within a range.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     size(1..10) hint "Must have 1-10 tags"
 * }
 *
 * validator.validate(User(tags = listOf("kotlin", "java"))) // Success
 * validator.validate(User(tags = emptyList())) // Failure
 * validator.validate(User(tags = List(15) { "tag$it" })) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.size(range: IntRange): Rule = constrainIfNotNull(
    message = "Size must be between ${range.first} and ${range.last}",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.size in range }
)

/**
 * Validates minimum collection size.
 *
 * Example:
 * ```kotlin
 * User::authors {
 *     minSize(2) hint "At least 2 authors required"
 * }
 *
 * validator.validate(User(authors = listOf("John", "Jane"))) // Success
 * validator.validate(User(authors = listOf("John"))) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.minSize(min: Int): Rule = constrainIfNotNull(
    message = "Must contain at least $min items",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.size >= min }
)

/**
 * Validates maximum collection size.
 *
 * Example:
 * ```kotlin
 * User::tags {
 *     maxSize(10) hint "Maximum 10 tags allowed"
 * }
 *
 * validator.validate(User(tags = listOf("kotlin"))) // Success
 * validator.validate(User(tags = List(15) { "tag$it" })) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.maxSize(max: Int): Rule = constrainIfNotNull(
    message = "Must contain at most $max items",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.size <= max }
)

/**
 * Validates exact collection size.
 *
 * Example:
 * ```kotlin
 * User::coordinates {
 *     exactSize(2) hint "Must have exactly 2 coordinates (lat, lng)"
 * }
 *
 * validator.validate(User(coordinates = listOf(48.8566, 2.3522))) // Success
 * validator.validate(User(coordinates = listOf(48.8566))) // Failure
 * ```
 */
public fun ValidationRule<Collection<*>?>.exactSize(size: Int): Rule = constrainIfNotNull(
    message = "Must contain exactly $size items",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.size == size }
)

/**
 * Validates that a collection contains a specific element.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     contains("admin") hint "Must have admin role"
 * }
 *
 * validator.validate(User(roles = listOf("admin", "user"))) // Success
 * validator.validate(User(roles = listOf("user"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Collection<T>?>.contains(element: T): Rule = constrainIfNotNull(
    message = "Must contain $element",
    code = ValidationErrorCode("missing_element"),
    predicate = { it.contains(element) }
)

/**
 * Validates that a collection does not contain a specific element.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     doesNotContain("guest") hint "Guest role not allowed"
 * }
 *
 * validator.validate(User(roles = listOf("admin", "user"))) // Success
 * validator.validate(User(roles = listOf("guest", "user"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Collection<T>?>.doesNotContain(element: T): Rule = constrainIfNotNull(
    message = "Must not contain $element",
    code = ValidationErrorCode("forbidden_element"),
    predicate = { !it.contains(element) }
)

/**
 * Validates that a collection contains all specified elements.
 *
 * Example:
 * ```kotlin
 * User::permissions {
 *     containsAll(listOf("read", "write")) hint "Must have read and write permissions"
 * }
 *
 * validator.validate(User(permissions = listOf("read", "write", "delete"))) // Success
 * validator.validate(User(permissions = listOf("read"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Collection<T>?>.containsAll(elements: Collection<T>): Rule = constrainIfNotNull(
    message = "Must contain all specified elements",
    code = ValidationErrorCode("missing_element"),
    predicate = { it.containsAll(elements) }
)

/**
 * Validates that a collection contains at least one of the specified elements.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     containsAny(listOf("admin", "moderator")) hint "Must be admin or moderator"
 * }
 *
 * validator.validate(User(roles = listOf("admin", "user"))) // Success
 * validator.validate(User(roles = listOf("moderator"))) // Success
 * validator.validate(User(roles = listOf("user"))) // Failure
 * ```
 */
public fun <T> ValidationRule<Collection<T>?>.containsAny(elements: Collection<T>): Rule = constrainIfNotNull(
    message = "Must contain at least one of the specified elements",
    code = ValidationErrorCode("missing_element"),
    predicate = { collection -> elements.any { collection.contains(it) } }
)

/**
 * Validates each element in a collection using an existing validator.
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
 * validator.validate(User(authors = listOf(Author("John", "john@example.com")))) // Success
 * validator.validate(User(authors = listOf(Author("", "invalid")))) // Failure
 * ```
 */
public fun <T : Any> ValidationRule<Collection<T>?>.each(
    validator: AtelierValidator<T>
): Rule = constrainIfNotNull(
    message = "All elements must be valid",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = { collection ->
        collection.all { element ->
            validator.validate(element) is ValidationResult.Success
        }
    }
)

/**
 * Validates each element in a collection using inline validation rules.
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
 * validator.validate(User(authors = listOf(Author("John Doe", "john@example.com")))) // Success
 * validator.validate(User(authors = listOf(Author("J", "invalid-email")))) // Failure
 * ```
 */
public inline fun <reified T : Any> ValidationRule<Collection<T>?>.each(
    crossinline configure: ValidationScope<T>.() -> Unit
): Rule {
    val validator = AtelierValidator<T> {
        configure()
    }
    return each(validator)
}
