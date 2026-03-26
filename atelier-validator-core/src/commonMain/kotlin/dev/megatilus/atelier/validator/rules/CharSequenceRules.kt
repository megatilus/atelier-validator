/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.rules

import dev.megatilus.atelier.validator.Rule
import dev.megatilus.atelier.validator.ValidationRule
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import kotlin.text.contains

/**
 * Validates that a string is not blank (not null, not empty, not whitespace).
 *
 * Example:
 * ```kotlin
 * User::name {
 *     notBlank() hint "Name is required"
 * }
 *
 * validator.validate(User(name = "John")) // Success
 * validator.validate(User(name = "")) // Failure
 * validator.validate(User(name = "   ")) // Failure
 * validator.validate(User(name = null)) // Failure
 * ```
 */
public fun ValidationRule<String?>.notBlank(): Rule = constrain(
    message = "Cannot be blank",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { !it.isNullOrBlank() }
)

/**
 * Validates that a string is not empty (not null, not empty).
 *
 * Example:
 * ```kotlin
 * User::username {
 *     notEmpty() hint "Username is required"
 * }
 *
 * validator.validate(User(username = "john")) // Success
 * validator.validate(User(username = "   ")) // Success (whitespace allowed)
 * validator.validate(User(username = "")) // Failure
 * validator.validate(User(username = null)) // Failure
 * ```
 */
public fun ValidationRule<String?>.notEmpty(): Rule = constrain(
    message = "Cannot be empty",
    code = ValidationErrorCode.Companion.REQUIRED,
    predicate = { !it.isNullOrEmpty() }
)

/**
 * Validates string length within a range.
 *
 * Example:
 * ```kotlin
 * User::name {
 *     length(2..50) hint "Name must be 2-50 characters"
 * }
 *
 * validator.validate(User(name = "John")) // Success
 * validator.validate(User(name = "J")) // Failure
 * validator.validate(User(name = "a".repeat(60))) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.length(range: IntRange): Rule = constrainIfNotNull(
    message = "Length must be between ${range.first} and ${range.last} characters",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.length in range }
)

/**
 * Validates minimum string length.
 *
 * Example:
 * ```kotlin
 * User::password {
 *     minLength(8) hint "Password must be at least 8 characters"
 * }
 *
 * validator.validate(User(password = "password123")) // Success
 * validator.validate(User(password = "pass")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.minLength(min: Int): Rule = constrainIfNotNull(
    message = "Must be at least $min characters",
    code = ValidationErrorCode.Companion.TOO_SHORT,
    predicate = { it.length >= min }
)

/**
 * Validates maximum string length.
 *
 * Example:
 * ```kotlin
 * User::bio {
 *     maxLength(500) hint "Bio must be at most 500 characters"
 * }
 *
 * validator.validate(User(bio = "Short bio")) // Success
 * validator.validate(User(bio = "a".repeat(600))) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.maxLength(max: Int): Rule = constrainIfNotNull(
    message = "Must be at most $max characters",
    code = ValidationErrorCode.Companion.TOO_LONG,
    predicate = { it.length <= max }
)

/**
 * Validates exact string length.
 *
 * Example:
 * ```kotlin
 * User::zipCode {
 *     exactLength(5) hint "Zip code must be exactly 5 digits"
 * }
 *
 * validator.validate(User(zipCode = "75001")) // Success
 * validator.validate(User(zipCode = "7500")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.exactLength(length: Int): Rule = constrainIfNotNull(
    message = "Must be exactly $length characters",
    code = ValidationErrorCode.Companion.OUT_OF_RANGE,
    predicate = { it.length == length }
)

/**
 * Validates that a string matches a regex pattern.
 *
 * Example:
 * ```kotlin
 * User::phone {
 *     matches(Regex("^\\+?[1-9]\\d{1,14}$")) hint "Invalid phone number"
 * }
 *
 * validator.validate(User(phone = "+33612345678")) // Success
 * validator.validate(User(phone = "abc")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.matches(pattern: Regex): Rule = constrainIfNotNull(
    message = "Invalid format",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { pattern.matches(it) }
)

/**
 * Validates email format.
 *
 * Example:
 * ```kotlin
 * User::email {
 *     email() hint "Invalid email address"
 * }
 *
 * validator.validate(User(email = "john@example.com")) // Success
 * validator.validate(User(email = "invalid-email")) // Failure
 * ```
 */
public fun ValidationRule<String?>.email(): Rule = constrainIfNotNull(
    message = "Must be a valid email",
    code = ValidationErrorCode.Companion.INVALID_EMAIL,
    predicate = { EMAIL_REGEX.matches(it) }
)

/**
 * Validates URL format (http/https).
 *
 * Example:
 * ```kotlin
 * User::website {
 *     url() hint "Invalid website URL"
 * }
 *
 * validator.validate(User(website = "https://example.com")) // Success
 * validator.validate(User(website = "not-a-url")) // Failure
 * ```
 */
public fun ValidationRule<String?>.url(): Rule = constrainIfNotNull(
    message = "Must be a valid URL",
    code = ValidationErrorCode("invalid_url"),
    predicate = { URL_REGEX.matches(it) }
)

/**
 * Validates UUID format (v4).
 *
 * Example:
 * ```kotlin
 * User::id {
 *     uuid() hint "Invalid user ID"
 * }
 *
 * validator.validate(User(id = "550e8400-e29b-41d4-a716-446655440000")) // Success
 * validator.validate(User(id = "invalid-uuid")) // Failure
 * ```
 */
public fun ValidationRule<String?>.uuid(): Rule = constrainIfNotNull(
    message = "Must be a valid UUID",
    code = ValidationErrorCode("invalid_uuid"),
    predicate = { UUID_REGEX.matches(it) }
)

/**
 * Validates IPv4 address format.
 *
 * Example:
 * ```kotlin
 * Server::ip {
 *     ipv4() hint "Invalid IPv4 address"
 * }
 *
 * validator.validate(Server(ip = "192.168.1.1")) // Success
 * validator.validate(Server(ip = "256.1.1.1")) // Failure
 * ```
 */
public fun ValidationRule<String?>.ipv4(): Rule = constrainIfNotNull(
    message = "Must be a valid IPv4 address",
    code = ValidationErrorCode("invalid_ipv4"),
    predicate = { IPV4_REGEX.matches(it) }
)

/**
 * Validates IPv6 address format.
 *
 * Example:
 * ```kotlin
 * Server::ip {
 *     ipv6() hint "Invalid IPv6 address"
 * }
 *
 * validator.validate(Server(ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334")) // Success
 * validator.validate(Server(ip = "192.168.1.1")) // Failure
 * ```
 */
public fun ValidationRule<String?>.ipv6(): Rule = constrainIfNotNull(
    message = "Must be a valid IPv6 address",
    code = ValidationErrorCode("invalid_ipv6"),
    predicate = { IPV6_REGEX.matches(it) }
)

/**
 * Validates strong password with customizable requirements.
 *
 * Example:
 * ```kotlin
 * User::password {
 *     strongPassword(
 *         minLength = 8,
 *         requireUppercase = true,
 *         requireLowercase = true,
 *         requireDigit = true,
 *         requireSpecialChar = true
 *     ) hint "Password too weak"
 * }
 *
 * validator.validate(User(password = "Passw0rd!")) // Success
 * validator.validate(User(password = "weak")) // Failure
 * ```
 */
public fun ValidationRule<String?>.strongPassword(
    minLength: Int = 8,
    requireUppercase: Boolean = true,
    requireLowercase: Boolean = true,
    requireDigit: Boolean = true,
    requireSpecialChar: Boolean = true
): Rule = constrainIfNotNull(
    message = buildPasswordMessage(minLength, requireUppercase, requireLowercase, requireDigit, requireSpecialChar),
    code = ValidationErrorCode("weak_password"),
    predicate = { password ->
        password.length >= minLength &&
            (!requireUppercase || password.any { it.isUpperCase() }) &&
            (!requireLowercase || password.any { it.isLowerCase() }) &&
            (!requireDigit || password.any { it.isDigit() }) &&
            (!requireSpecialChar || password.any { isCommonSpecialChar(it) })
    }
)

/**
 * Validates that a string contains only alphanumeric characters.
 *
 * Example:
 * ```kotlin
 * User::username {
 *     alphanumeric() hint "Username can only contain letters and numbers"
 * }
 *
 * validator.validate(User(username = "john123")) // Success
 * validator.validate(User(username = "john_123")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.alphanumeric(): Rule = constrainIfNotNull(
    message = "Must contain only letters and numbers",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.all { char -> char.isLetterOrDigit() } }
)

/**
 * Validates that a string contains only letters.
 *
 * Example:
 * ```kotlin
 * User::firstName {
 *     alpha() hint "First name can only contain letters"
 * }
 *
 * validator.validate(User(firstName = "John")) // Success
 * validator.validate(User(firstName = "John123")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.alpha(): Rule = constrainIfNotNull(
    message = "Must contain only letters",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.all { char -> char.isLetter() } }
)

/**
 * Validates that a string contains only digits.
 *
 * Example:
 * ```kotlin
 * User::pin {
 *     numeric() hint "PIN must contain only digits"
 * }
 *
 * validator.validate(User(pin = "1234")) // Success
 * validator.validate(User(pin = "12a4")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.numeric(): Rule = constrainIfNotNull(
    message = "Must contain only digits",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.all { char -> char.isDigit() } }
)

/**
 * Validates that a string is all uppercase.
 *
 * Example:
 * ```kotlin
 * User::countryCode {
 *     uppercase() hint "Country code must be uppercase"
 * }
 *
 * validator.validate(User(countryCode = "FR")) // Success
 * validator.validate(User(countryCode = "fr")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.uppercase(): Rule = constrainIfNotNull(
    message = "Must be uppercase",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { str -> str.all { it.isUpperCase() || !it.isLetter() } }
)

/**
 * Validates that a string is all lowercase.
 *
 * Example:
 * ```kotlin
 * User::username {
 *     lowercase() hint "Username must be lowercase"
 * }
 *
 * validator.validate(User(username = "john")) // Success
 * validator.validate(User(username = "John")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.lowercase(): Rule = constrainIfNotNull(
    message = "Must be lowercase",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { str -> str.all { it.isLowerCase() || !it.isLetter() } }
)

/**
 * Validates that a string starts with a specific prefix.
 *
 * Example:
 * ```kotlin
 * User::sku {
 *     startsWith("PROD-") hint "SKU must start with PROD-"
 * }
 *
 * validator.validate(User(sku = "PROD-12345")) // Success
 * validator.validate(User(sku = "TEST-12345")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.startsWith(prefix: CharSequence): Rule = constrainIfNotNull(
    message = "Must start with $prefix",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.startsWith(prefix) }
)

/**
 * Validates that a string ends with a specific suffix.
 *
 * Example:
 * ```kotlin
 * User::domain {
 *     endsWith(".com") hint "Domain must end with .com"
 * }
 *
 * validator.validate(User(domain = "example.com")) // Success
 * validator.validate(User(domain = "example.org")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.endsWith(suffix: CharSequence): Rule = constrainIfNotNull(
    message = "Must end with $suffix",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.endsWith(suffix) }
)

/**
 * Validates that a string contains a specific substring.
 *
 * Example:
 * ```kotlin
 * User::email {
 *     containsString("@company.com") hint "Must be a company email"
 * }
 *
 * validator.validate(User(email = "john@company.com")) // Success
 * validator.validate(User(email = "john@gmail.com")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.containsString(substring: CharSequence): Rule = constrainIfNotNull(
    message = "Must contain $substring",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { it.contains(substring) }
)

/**
 * Validates that a string does not contain a specific substring.
 *
 * Example:
 * ```kotlin
 * User::password {
 *     doesNotContain("password") hint "Password cannot contain the word 'password'"
 * }
 *
 * validator.validate(User(password = "MySecret123")) // Success
 * validator.validate(User(password = "password123")) // Failure
 * ```
 */
public fun ValidationRule<CharSequence?>.doesNotContain(substring: CharSequence): Rule = constrainIfNotNull(
    message = "Must not contain $substring",
    code = ValidationErrorCode.Companion.INVALID_FORMAT,
    predicate = { !it.contains(substring) }
)

private fun buildPasswordMessage(
    minLength: Int,
    requireUppercase: Boolean,
    requireLowercase: Boolean,
    requireDigit: Boolean,
    requireSpecialChar: Boolean
): String {
    val requirements = mutableListOf<String>()
    requirements.add("at least $minLength characters")
    if (requireUppercase) requirements.add("one uppercase letter")
    if (requireLowercase) requirements.add("one lowercase letter")
    if (requireDigit) requirements.add("one digit")
    if (requireSpecialChar) requirements.add("one special character")
    return "Password must contain ${requirements.joinToString(", ")}"
}

private fun isCommonSpecialChar(char: Char): Boolean =
    char in "!@#$%^&*()_+-=[]{}|;:,.<>?/~`"

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

private val URL_REGEX = Regex(
    "^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$"
)

private val UUID_REGEX = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

private val IPV4_REGEX = Regex(
    "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
)

private val IPV6_REGEX = Regex(
    "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))$"
)
