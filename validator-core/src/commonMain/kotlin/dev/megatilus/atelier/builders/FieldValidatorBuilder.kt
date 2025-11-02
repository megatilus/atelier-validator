/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.builders

import dev.megatilus.atelier.constraints.Constraint
import dev.megatilus.atelier.constraints.PropertyValidator
import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.reflect.KProperty1

/**
 * FieldValidatorBuilder constructs validation rules for a given field using a fluent API (DSL).
 *
 * This class allows defining simple field-level constraints (access to the field value only)
 * as well as validations that require access to the parent object (for example, comparisons
 * between fields).
 *
 * @param T The type of the validated object
 * @param R The type of the validated field
 * @param property Property reference (e.g. User::name)
 * @param fieldName The field name (used in error messages)
 * @param validatorBuilder The parent builder that aggregates all validations
 */
public class FieldValidatorBuilder<T : Any, R> internal constructor(
    private val property: KProperty1<T, R>,
    private val fieldName: String,
    private val validatorBuilder: ValidatorBuilder<T>
) {
    private val constraints = mutableListOf<Constraint<R>>()
    private val objectAwareValidations = mutableListOf<(T, R) -> ValidationErrorDetail?>()

    /** Low-level API to register a constraint */
    internal fun constraint(
        hint: String,
        code: ValidatorCode = ValidatorCode.CUSTOM_ERROR,
        predicate: (R) -> Boolean
    ): FieldValidatorBuilder<T, R> {
        val c = Constraint(hint = hint, code = code, predicate = predicate)
        constraints.add(c)
        updateValidator()

        return this
    }

    /** Public convenience for end-users (wraps the internal constraint). */
    public fun custom(
        code: ValidatorCode = ValidatorCode.CUSTOM_ERROR,
        message: String = "$fieldName validation failed",
        predicate: (R) -> Boolean
    ): FieldValidatorBuilder<T, R> =
        constraint(
            hint = message,
            code = code,
            predicate = predicate
        )

    public fun isEqualTo(
        selector: (T) -> R,
        message: String? = null
    ): FieldValidatorBuilder<T, R> {
        objectAwareValidations.add { obj, value ->
            val expected = selector(obj)

            if (value != expected) {
                ValidationErrorDetail(
                    fieldName = fieldName,
                    message = message ?: "Must match the expected value",
                    code = ValidatorCode.INVALID_VALUE,
                    actualValue = value?.toString() ?: "null"
                )
            } else {
                null
            }
        }

        updateValidator()

        return this
    }

    internal fun updateValidator() {
        val fieldValidator = PropertyValidator<R> { value, _ ->
            constraints.mapNotNull { it.validate(value, fieldName) }
        }

        validatorBuilder.addFieldValidation(property, fieldName, fieldValidator)

        if (objectAwareValidations.isNotEmpty()) {
            val composite: (T) -> List<ValidationErrorDetail> = { obj ->
                val value = property.get(obj)
                objectAwareValidations.mapNotNull { it(obj, value) }
            }

            validatorBuilder.addObjectAwareFieldValidation(property, fieldName, composite)
        }
    }
}
