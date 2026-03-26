/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.rules

import dev.megatilus.atelier.validator.Rule
import dev.megatilus.atelier.validator.ValidationRule
import dev.megatilus.atelier.validator.results.ValidationErrorCode

/**
 * Validates that a map is not empty.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     isNotEmpty() hint "At least one setting required"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Success
 * validator.validate(User(settings = emptyMap())) // Failure
 * validator.validate(User(settings = null)) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.isNotEmpty(): Rule = constrainIfNotNull(
    message = "Must not be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { it.isNotEmpty() }
)

/**
 * Validates that a map is empty.
 *
 * Example:
 * ```kotlin
 * User::errors {
 *     isEmpty() hint "Should have no errors"
 * }
 *
 * validator.validate(User(errors = emptyMap())) // Success
 * validator.validate(User(errors = null)) // Success
 * validator.validate(User(errors = mapOf("field" to "error"))) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.isEmpty(): Rule = constrain(
    message = "Must be empty",
    code = ValidationErrorCode("must_be_empty"),
    predicate = { it.isNullOrEmpty() }
)

/**
 * Validates map size within a range.
 *
 * Example:
 * ```kotlin
 * User::metadata {
 *     size(1..20) hint "Must have 1-20 metadata entries"
 * }
 *
 * validator.validate(User(metadata = mapOf("key" to "value"))) // Success
 * validator.validate(User(metadata = emptyMap())) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.size(range: IntRange): Rule = constrainIfNotNull(
    message = "Size must be between ${range.first} and ${range.last}",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size in range }
)

/**
 * Validates minimum map size.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     minSize(1) hint "At least one setting required"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Success
 * validator.validate(User(settings = emptyMap())) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.minSize(min: Int): Rule = constrainIfNotNull(
    message = "Must contain at least $min entries",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size >= min }
)

/**
 * Validates maximum map size.
 *
 * Example:
 * ```kotlin
 * User::metadata {
 *     maxSize(50) hint "Maximum 50 metadata entries allowed"
 * }
 *
 * validator.validate(User(metadata = mapOf("key" to "value"))) // Success
 * validator.validate(User(metadata = (0..60).associate { "key$it" to "value$it" })) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.maxSize(max: Int): Rule = constrainIfNotNull(
    message = "Must contain at most $max entries",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size <= max }
)

/**
 * Validates exact map size.
 *
 * Example:
 * ```kotlin
 * User::coords {
 *     exactSize(2) hint "Must have exactly 2 coordinates (x, y)"
 * }
 *
 * validator.validate(User(coords = mapOf("x" to 10, "y" to 20))) // Success
 * validator.validate(User(coords = mapOf("x" to 10))) // Failure
 * ```
 */
public fun ValidationRule<Map<*, *>?>.exactSize(size: Int): Rule = constrainIfNotNull(
    message = "Must contain exactly $size entries",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.size == size }
)

/**
 * Validates that a map contains a specific key.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     containsKey("theme") hint "Theme setting required"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Success
 * validator.validate(User(settings = mapOf("language" to "en"))) // Failure
 * ```
 */
public fun <K> ValidationRule<Map<K, *>?>.containsKey(key: K): Rule = constrainIfNotNull(
    message = "Must contain key $key",
    code = ValidationErrorCode("missing_key"),
    predicate = { it.containsKey(key) }
)

/**
 * Validates that a map does not contain a specific key.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     doesNotContainKey("deprecated_option") hint "Deprecated option not allowed"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Success
 * validator.validate(User(settings = mapOf("deprecated_option" to "value"))) // Failure
 * ```
 */
public fun <K> ValidationRule<Map<K, *>?>.doesNotContainKey(key: K): Rule = constrainIfNotNull(
    message = "Must not contain key $key",
    code = ValidationErrorCode("forbidden_key"),
    predicate = { !it.containsKey(key) }
)

/**
 * Validates that a map contains all specified keys.
 *
 * Example:
 * ```kotlin
 * User::config {
 *     containsAllKeys(listOf("host", "port", "username")) hint "Missing required config"
 * }
 *
 * validator.validate(User(config = mapOf("host" to "localhost", "port" to "3306", "username" to "root"))) // Success
 * validator.validate(User(config = mapOf("host" to "localhost"))) // Failure
 * ```
 */
public fun <K> ValidationRule<Map<K, *>?>.containsAllKeys(keys: Collection<K>): Rule = constrainIfNotNull(
    message = "Must contain all specified keys",
    code = ValidationErrorCode("missing_key"),
    predicate = { map -> keys.all { map.containsKey(it) } }
)

/**
 * Validates that a map contains at least one of the specified keys.
 *
 * Example:
 * ```kotlin
 * User::auth {
 *     containsAnyKey(listOf("password", "apiKey", "token")) hint "At least one auth method required"
 * }
 *
 * validator.validate(User(auth = mapOf("password" to "secret"))) // Success
 * validator.validate(User(auth = mapOf("token" to "abc123"))) // Success
 * validator.validate(User(auth = mapOf("username" to "john"))) // Failure
 * ```
 */
public fun <K> ValidationRule<Map<K, *>?>.containsAnyKey(keys: Collection<K>): Rule = constrainIfNotNull(
    message = "Must contain at least one of the specified keys",
    code = ValidationErrorCode("missing_key"),
    predicate = { map -> keys.any { map.containsKey(it) } }
)

/**
 * Validates that a map contains a specific value.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     containsValue("admin") hint "Must have admin role"
 * }
 *
 * validator.validate(User(roles = mapOf("role1" to "admin", "role2" to "user"))) // Success
 * validator.validate(User(roles = mapOf("role1" to "user"))) // Failure
 * ```
 */
public fun <V> ValidationRule<Map<*, V>?>.containsValue(value: V): Rule = constrainIfNotNull(
    message = "Must contain value $value",
    code = ValidationErrorCode("missing_value"),
    predicate = { it.containsValue(value) }
)

/**
 * Validates that a map does not contain a specific value.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     doesNotContainValue("disabled") hint "All settings must be enabled"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Success
 * validator.validate(User(settings = mapOf("feature" to "disabled"))) // Failure
 * ```
 */
public fun <V> ValidationRule<Map<*, V>?>.doesNotContainValue(value: V): Rule = constrainIfNotNull(
    message = "Must not contain value $value",
    code = ValidationErrorCode("forbidden_value"),
    predicate = { !it.containsValue(value) }
)

/**
 * Validates that a map contains all specified values.
 *
 * Example:
 * ```kotlin
 * User::permissions {
 *     containsAllValues(listOf("read", "write")) hint "Must have read and write permissions"
 * }
 *
 * validator.validate(User(permissions = mapOf("perm1" to "read", "perm2" to "write"))) // Success
 * validator.validate(User(permissions = mapOf("perm1" to "read"))) // Failure
 * ```
 */
public fun <V> ValidationRule<Map<*, V>?>.containsAllValues(values: Collection<V>): Rule = constrainIfNotNull(
    message = "Must contain all specified values",
    code = ValidationErrorCode("missing_value"),
    predicate = { map -> values.all { map.containsValue(it) } }
)

/**
 * Validates that a map contains at least one of the specified values.
 *
 * Example:
 * ```kotlin
 * User::roles {
 *     containsAnyValue(listOf("admin", "moderator")) hint "Must be admin or moderator"
 * }
 *
 * validator.validate(User(roles = mapOf("role1" to "admin"))) // Success
 * validator.validate(User(roles = mapOf("role1" to "moderator"))) // Success
 * validator.validate(User(roles = mapOf("role1" to "user"))) // Failure
 * ```
 */
public fun <V> ValidationRule<Map<*, V>?>.containsAnyValue(values: Collection<V>): Rule = constrainIfNotNull(
    message = "Must contain at least one of the specified values",
    code = ValidationErrorCode("missing_value"),
    predicate = { map -> values.any { map.containsValue(it) } }
)

/**
 * Validates each entry in a map using a predicate on both key and value.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     eachEntry { key, value ->
 *         key.startsWith("valid_") && value.isNotBlank()
 *     } hint "All settings must have valid_ prefix and non-blank values"
 * }
 *
 * validator.validate(User(settings = mapOf("valid_theme" to "dark", "valid_lang" to "en"))) // Success
 * validator.validate(User(settings = mapOf("theme" to "dark"))) // Failure
 * ```
 */
public fun <K, V> ValidationRule<Map<K, V>?>.eachEntry(
    predicate: (K, V) -> Boolean
): Rule = constrainIfNotNull(
    message = "All entries must satisfy condition",
    code = ValidationErrorCode.Companion.INVALID_VALUE,
    predicate = { map ->
        map.all { (key, value) -> predicate(key, value) }
    }
)

/**
 * Validates all keys in a map using a predicate.
 *
 * Example:
 * ```kotlin
 * User::metadata {
 *     eachKey { it.startsWith("meta_") } hint "All metadata keys must start with meta_"
 * }
 *
 * validator.validate(User(metadata = mapOf("meta_created" to "2025-01-01", "meta_updated" to "2025-03-13"))) // Success
 * validator.validate(User(metadata = mapOf("created" to "2025-01-01"))) // Failure
 * ```
 */
public fun <K> ValidationRule<Map<K, *>?>.eachKey(
    predicate: (K) -> Boolean
): Rule = constrainIfNotNull(
    message = "All keys must satisfy condition",
    code = ValidationErrorCode.Companion.INVALID_VALUE,
    predicate = { map ->
        map.keys.all { key -> predicate(key) }
    }
)

/**
 * Validates all values in a map using a predicate.
 *
 * Example:
 * ```kotlin
 * User::settings {
 *     eachValue { it.isNotBlank() } hint "All settings must have non-blank values"
 * }
 *
 * validator.validate(User(settings = mapOf("theme" to "dark", "language" to "en"))) // Success
 * validator.validate(User(settings = mapOf("theme" to ""))) // Failure
 * ```
 */
public fun <V> ValidationRule<Map<*, V>?>.eachValue(
    predicate: (V) -> Boolean
): Rule = constrainIfNotNull(
    message = "All values must satisfy condition",
    code = ValidationErrorCode.Companion.INVALID_VALUE,
    predicate = { map ->
        map.values.all { value -> predicate(value) }
    }
)
