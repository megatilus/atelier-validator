/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.Rule
import dev.megatilus.atelier.ValidationRule
import dev.megatilus.atelier.results.ValidationErrorCode

/**
 * Validates that a boolean value is true.
 *
 * Example:
 * ```kotlin
 * User::termsAccepted {
 *     isTrue() hint "You must accept the terms and conditions"
 * }
 *
 * validator.validate(User(termsAccepted = true)) // Success
 * validator.validate(User(termsAccepted = false)) // Failure
 * validator.validate(User(termsAccepted = null)) // Failure
 * ```
 */
public fun ValidationRule<Boolean?>.isTrue(): Rule = constrain(
    message = "Must be true",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = { it == true }
)

/**
 * Validates that a boolean value is false.
 *
 * Example:
 * ```kotlin
 * User::isDeleted {
 *     isFalse() hint "User must not be deleted"
 * }
 *
 * validator.validate(User(isDeleted = false)) // Success
 * validator.validate(User(isDeleted = true)) // Failure
 * validator.validate(User(isDeleted = null)) // Failure
 * ```
 */
public fun ValidationRule<Boolean?>.isFalse(): Rule = constrain(
    message = "Must be false",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = { it == false }
)
