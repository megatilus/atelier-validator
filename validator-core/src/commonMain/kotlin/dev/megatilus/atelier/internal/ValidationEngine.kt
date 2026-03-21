/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.internal

import dev.megatilus.atelier.results.ValidationError
import dev.megatilus.atelier.results.ValidationResult
import kotlin.reflect.KProperty1

/**
 * Validation engine for building and executing validators.
 */
internal class ValidationEngine<T : Any> {

    internal val fieldValidations = mutableMapOf<KProperty1<T, *>, MutableList<(T) -> List<ValidationError>>>()

    internal fun <R> addFieldValidation(
        property: KProperty1<T, R>,
        fieldName: String,
        validator: FieldRule<R>
    ) {
        val list = fieldValidations.getOrPut(property) { mutableListOf() }
        val wrapped: (T) -> List<ValidationError> = { obj ->
            validator.validate(property.get(obj), fieldName)
        }
        if (list.none { it === wrapped }) list.add(wrapped)
    }

    internal fun <R> field(property: KProperty1<T, R>, fieldName: String? = null): FieldScope<T, R> {
        val name = fieldName ?: property.name
        return FieldScope(property, name, this)
    }

    internal fun validateObject(obj: T): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()

        // Validate field-level validations
        for (validations in fieldValidations.values) {
            for (validation in validations) {
                allErrors.addAll(validation(obj))
            }
        }

        val uniqueErrors = allErrors.distinctBy {
            it.fieldName to it.code to it.message
        }

        return if (uniqueErrors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(uniqueErrors)
        }
    }

    internal fun validateObjectFirst(obj: T): ValidationResult {
        // Check field-level validations
        for (validations in fieldValidations.values) {
            for (validation in validations) {
                val errors = validation(obj).distinctBy {
                    it.fieldName to it.code to it.message
                }
                if (errors.isNotEmpty()) {
                    return ValidationResult.Failure(listOf(errors.first()))
                }
            }
        }

        return ValidationResult.Success
    }
}
