/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.builders

import dev.megatilus.atelier.results.ValidatorCode

/**
 * **Extension API for validator modules only.**
 *
 * This function allows external validator modules (e.g., kotlinx-datetime, custom validators)
 * to add constraints to field validators. It should NOT be used in end-user application code.
 *
 * ## For End Users
 * If you need custom validation logic in your application, use `custom()` instead:
 * ```
 * User::age {
 *     custom(message = "Must be an adult") { it >= 18 }
 * }
 * ```
 *
 * ## For Extension Authors
 * When creating validator extensions, you can use this method:
 * ```
 * fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isFutureDate(message: String? = null) =
 *     constraintForExtension(
 *         hint = message ?: "Date must be in the future",
 *         code = ValidatorCode.INVALID_VALUE
 *     ) { it > Clock.System.todayIn(TimeZone.currentSystemDefault()) }
 * ```
 *
 * @param hint The error message to display when validation fails
 * @param code The validation error code
 * @param predicate The validation logic returning true if the value is valid
 */
public fun <T : Any, R> FieldValidatorBuilder<T, R>.constraintForExtension(
    hint: String,
    code: ValidatorCode = ValidatorCode.CUSTOM_ERROR,
    predicate: (R) -> Boolean
): FieldValidatorBuilder<T, R> {
    return this.constraint(hint = hint, code = code, predicate = predicate)
}
