/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.helpers.isCommonSpecialChar
import dev.megatilus.atelier.helpers.isValidLuhn
import dev.megatilus.atelier.helpers.passwordMessage
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

private val EMAIL_REGEX =
    Regex(
        "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9]" +
            "(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
            "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    )

private val URL_REGEX =
    Regex("^https?://[-\\w.]+(?:[:\\d]+)?(?:/[\\w/_.]*(?:\\?[\\w&=%.]*)?(?:#[\\w.]*)?)?$")

private val PHONE_REGEX = Regex("^\\+?[1-9]\\d{1,14}$") // Format E.164

public fun <T : Any> FieldValidatorBuilder<T, String>.notBlank(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Cannot be blank",
        code = ValidatorCode.NOT_BLANK,
        predicate = { it.isNotBlank() }
    )
}

@JvmName("notBlankNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.notBlank(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Cannot be blank",
        code = ValidatorCode.NOT_BLANK,
        predicate = { it != null && it.isNotBlank() }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Cannot be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it.isNotEmpty() }
    )
}

@JvmName("notEmptyNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.notEmpty(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Cannot be empty",
        code = ValidatorCode.NOT_EMPTY,
        predicate = { it != null && it.isNotEmpty() }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.minLength(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be at least $min characters",
        code = ValidatorCode.TOO_SHORT,
        predicate = { it.length >= min }
    )
}

@JvmName("minLengthNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.minLength(
    min: Int,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be at least $min characters",
        code = ValidatorCode.TOO_SHORT,
        predicate = { it == null || it.length >= min }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.maxLength(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be at most $max characters",
        code = ValidatorCode.TOO_LONG,
        predicate = { it.length <= max }
    )
}

@JvmName("maxLengthNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.maxLength(
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be at most $max characters",
        code = ValidatorCode.TOO_LONG,
        predicate = { it == null || it.length <= max }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.length(
    min: Int,
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be between $min and $max characters",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.length in min..max }
    )
}

@JvmName("lengthNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.length(
    min: Int,
    max: Int,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be between $min and $max characters",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it.length in min..max }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.exactLength(
    expectedLength: Int,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be exactly $expectedLength characters",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.length == expectedLength }
    )
}

@JvmName("exactLengthNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.exactLength(
    expectedLength: Int,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be exactly $expectedLength characters",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it.length == expectedLength }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.matches(
    expected: String,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must match '$expected'",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == expected }
    )
}

@JvmName("matchesNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.matches(
    expected: String,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must match '$expected'",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == expected }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.matchesPattern(
    pattern: Regex,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Invalid format",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { pattern.matches(it) }
    )
}

@JvmName("matchesPatternNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.matchesPattern(
    pattern: Regex,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Invalid format",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || pattern.matches(it) }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.alphanumeric(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must contain only letters and numbers",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.all { c -> c.isLetterOrDigit() } }
    )
}

@JvmName("alphanumericNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.alphanumeric(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must contain only letters and numbers",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.all { c -> c.isLetterOrDigit() } }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.alpha(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must contain only letters",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.all { c -> c.isLetter() } }
    )
}

@JvmName("alphaNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.alpha(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must contain only letters",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.all { c -> c.isLetter() } }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.numeric(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must contain only numbers",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.all { c -> c.isDigit() } }
    )
}

@JvmName("numericNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.numeric(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must contain only numbers",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.all { c -> c.isDigit() } }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.uppercase(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be uppercase",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == it.uppercase() }
    )
}

@JvmName("uppercaseNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.uppercase(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be uppercase",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it == it.uppercase() }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.lowercase(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be lowercase",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == it.lowercase() }
    )
}

@JvmName("lowercaseNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.lowercase(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be lowercase",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it == it.lowercase() }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.email(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be a valid email address",
        code = ValidatorCode.INVALID_EMAIL,
        predicate = { it.isBlank() || EMAIL_REGEX.matches(it) }
    )
}

@JvmName("emailNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.email(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be a valid email address",
        code = ValidatorCode.INVALID_EMAIL,
        predicate = { it == null || it.isBlank() || EMAIL_REGEX.matches(it) }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.url(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be a valid URL",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.isBlank() || URL_REGEX.matches(it) }
    )
}

@JvmName("urlNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.url(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be a valid URL",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.isBlank() || URL_REGEX.matches(it) }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.phoneNumber(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be a valid phone number",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = {
            val cleaned = it.replace(Regex("[\\s()-]"), "")
            PHONE_REGEX.matches(cleaned)
        }
    )
}

@JvmName("phoneNumberNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.phoneNumber(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be a valid phone number",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = {
            it == null || it.isBlank() || run {
                val cleaned = it.replace(Regex("[\\s()-]"), "")
                PHONE_REGEX.matches(cleaned)
            }
        }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.creditCard(
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint = message ?: "Must be a valid credit card number",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = {
            val cleaned = it.replace(Regex("[\\s-]"), "")
            cleaned.length in 13..19 && isValidLuhn(cleaned)
        }
    )
}

@JvmName("creditCardNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.creditCard(
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint = message ?: "Must be a valid credit card number",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = {
            it == null || it.isBlank() || run {
                val cleaned = it.replace(Regex("[\\s-]"), "")
                cleaned.length in 13..19 && isValidLuhn(cleaned)
            }
        }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, String>.strongPassword(
    minLength: Int = 8,
    requireUppercase: Boolean = true,
    requireLowercase: Boolean = true,
    requireDigit: Boolean = true,
    requireSpecialChar: Boolean = true,
    message: String? = null
): FieldValidatorBuilder<T, String> {
    return constraint(
        hint =
        message ?: passwordMessage(minLength, requireUppercase, requireLowercase, requireDigit, requireSpecialChar),
        code = ValidatorCode.WEAK_PASSWORD,
        predicate = { password ->
            password.length >= minLength &&
                (!requireUppercase || password.any { it.isUpperCase() }) &&
                (!requireLowercase || password.any { it.isLowerCase() }) &&
                (!requireDigit || password.any { it.isDigit() }) &&
                (!requireSpecialChar || password.any { isCommonSpecialChar(it) })
        }
    )
}

@JvmName("strongPasswordNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.strongPassword(
    minLength: Int = 8,
    requireUppercase: Boolean = true,
    requireLowercase: Boolean = true,
    requireDigit: Boolean = true,
    requireSpecialChar: Boolean = true,
    message: String? = null
): FieldValidatorBuilder<T, String?> {
    return constraint(
        hint =
        message ?: passwordMessage(minLength, requireUppercase, requireLowercase, requireDigit, requireSpecialChar),
        code = ValidatorCode.WEAK_PASSWORD,
        predicate = { password ->
            password == null || (
                password.length >= minLength &&
                    (!requireUppercase || password.any { it.isUpperCase() }) &&
                    (!requireLowercase || password.any { it.isLowerCase() }) &&
                    (!requireDigit || password.any { it.isDigit() }) &&
                    (!requireSpecialChar || password.any { isCommonSpecialChar(it) })
                )
        }
    )
}
