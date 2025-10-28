/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.builders.ValidatorBuilder
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.validators.email
import dev.megatilus.atelier.validators.minLength
import dev.megatilus.atelier.validators.notBlank
import dev.megatilus.atelier.validators.range
import dev.megatilus.atelier.validators.strongPassword
import kotlin.reflect.KProperty1

/**
 * Core entry point for defining validations on a given data class.
 *
 * `AtelierValidator` provides a fluent DSL to define field-level and
 * object-level validation rules. It delegates the actual validation
 * logic to an underlying [ValidatorBuilder].
 *
 * Example usage:
 * ```
 * val validator = atelierValidator<User> {
 *     User::name {
 *         notBlank()
 *         minLength(2)
 *     }
 *     User::email {
 *         email()
 *     }
 * }
 *
 * val result = validator.validate(user)
 * ```
 *
 * @param T The type of the object being validated
 */
public class AtelierValidator<T : Any> : AtelierValidatorContract<T> {

    /** Internal builder responsible for registering and executing validations. */
    private val validatorBuilder = ValidatorBuilder<T>()

    /** Internal API used by the invoke operator. */
    internal fun <R> getFieldBuilder(property: KProperty1<T, R>): FieldValidatorBuilder<T, R> =
        validatorBuilder.field(property)

    /**
     * Executes all registered validations on the provided object.
     *
     * @param obj The object instance to validate
     * @return [ValidationResult.Success] if no validation failed, [ValidationResult.Failure] otherwise (with detailed errors)
     */
    override fun validate(obj: T): ValidationResult = validatorBuilder.validateObject(obj)

    /**
     * Executes validations in a fail-fast mode, stopping at the first error encountered.
     *
     * @param obj The object instance to validate
     * @return [ValidationResult.Success] if no validation failed, [ValidationResult.Failure] containing the first error otherwise
     */
    override fun validateFirst(obj: T): ValidationResult = validatorBuilder.validateObjectFirst(obj)
}

/**
 * DSL entry function for creating an [AtelierValidator].
 *
 * Simplifies validator creation using a Kotlin DSL style:
 * ```
 * val validator = atelierValidator<User> {
 *     User::password {
 *         notBlank()
 *         strongPassword()
 *     }
 * }
 * ```
 *
 * @param configure Lambda used to configure the validator
 * @return A configured [AtelierValidatorContract] instance ready to use
 */
public inline fun <reified T : Any> atelierValidator(
    configure: ValidatorDsl<T>.() -> Unit
): AtelierValidatorContract<T> {
    val validator = AtelierValidator<T>()
    val dsl = ValidatorDsl(validator)
    dsl.configure()

    return validator
}
