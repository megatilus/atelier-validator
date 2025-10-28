/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.builders

import dev.megatilus.atelier.constraints.PropertyValidator
import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidationResult
import kotlin.reflect.KProperty1

/**
 * ValidatorBuilder builds validators for an object by aggregating field-level and
 * object-aware validations.
 */
public class ValidatorBuilder<T : Any> {

    private val fieldValidations = mutableMapOf<KProperty1<T, *>, MutableList<(T) -> List<ValidationErrorDetail>>>()
    private val objectAwareFieldValidations = mutableMapOf<
        KProperty1<T, *>,
        MutableList<
            (
                T
            ) -> List<ValidationErrorDetail>
            >
        >()

    public fun <R> addFieldValidation(
        property: KProperty1<T, R>,
        fieldName: String,
        validator: PropertyValidator<R>
    ) {
        val list = fieldValidations.getOrPut(property) { mutableListOf() }
        val wrapped: (T) -> List<ValidationErrorDetail> = { obj ->
            validator.validate(property.get(obj), fieldName)
        }
        if (list.none { it === wrapped }) list.add(wrapped)
    }

    public fun <R> addObjectAwareFieldValidation(
        property: KProperty1<T, R>,
        fieldName: String,
        validation: (T) -> List<ValidationErrorDetail>
    ) {
        val list = objectAwareFieldValidations.getOrPut(property) { mutableListOf() }
        val wrapped: (T) -> List<ValidationErrorDetail> = { obj ->
            validation(obj).map { err ->
                if (err.fieldName.isBlank()) err.copy(fieldName = fieldName) else err
            }
        }
        if (list.none { it === wrapped }) list.add(wrapped)
    }

    /** Fluent API to create a FieldValidatorBuilder for a property (type-safe) */
    public fun <R> field(property: KProperty1<T, R>, fieldName: String? = null): FieldValidatorBuilder<T, R> {
        val name = fieldName ?: property.name
        return FieldValidatorBuilder(property, name, this)
    }

    public fun validateObject(obj: T): ValidationResult {
        val allErrors = mutableListOf<ValidationErrorDetail>()

        // Validate field-level validations
        for (validations in fieldValidations.values) {
            for (validation in validations) {
                allErrors.addAll(validation(obj))
            }
        }

        // Validate object-aware validations
        for (validations in objectAwareFieldValidations.values) {
            for (validation in validations) {
                allErrors.addAll(validation(obj))
            }
        }

        // Remove exact duplicates (same field, code, message)
        val uniqueErrors = allErrors.distinctBy { it.fieldName to it.code to it.message }

        return if (uniqueErrors.isEmpty()) ValidationResult.Success else ValidationResult.Failure(uniqueErrors)
    }

    public fun validateObjectFirst(obj: T): ValidationResult {
        // Check field-level validations
        for (validations in fieldValidations.values) {
            for (validation in validations) {
                val errors = validation(obj).distinctBy { it.fieldName to it.code to it.message }
                if (errors.isNotEmpty()) return ValidationResult.Failure(listOf(errors.first()))
            }
        }

        // Check object-aware validations
        for (validations in objectAwareFieldValidations.values) {
            for (validation in validations) {
                val errors = validation(obj).distinctBy { it.fieldName to it.code to it.message }
                if (errors.isNotEmpty()) return ValidationResult.Failure(listOf(errors.first()))
            }
        }

        return ValidationResult.Success
    }
}
