/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.validators.notBlank
import kotlin.reflect.KProperty1

/**
 * DSL scope for configuring validators.
 *
 * This class is used as the receiver for the atelierValidator configuration lambda.
 * It provides ONLY the property invoke syntax for defining validations.
 *
 * The validate() and validateFirst() methods are NOT available in this scope,
 * they are only available on the returned AtelierValidatorContract instance.
 *
 * @param T The type of object being validated
 */
public class ValidatorDsl<T : Any> @PublishedApi internal constructor(
    private val validator: AtelierValidator<T>
) {
    /**
     * DSL operator allowing property-based validation syntax.
     *
     * Example:
     * ```
     * User::name {
     *     notBlank()
     *     minLength(2)
     * }
     * ```
     *
     * @param configure Lambda to configure validation rules for this property
     */
    public operator fun <R> KProperty1<T, R>.invoke(configure: FieldValidatorBuilder<T, R>.() -> Unit) {
        validator.getFieldBuilder(this).apply(configure)
    }

    /**
     * Internal method to get field builder - used by internal field() extension for tests.
     */
    internal fun <R> getFieldBuilder(property: KProperty1<T, R>): FieldValidatorBuilder<T, R> {
        return validator.getFieldBuilder(property)
    }
}

/**
 * Internal extension function to support legacy field() syntax in tests.
 * Do not use in application code - use the property invoke syntax instead.
 */
internal fun <T : Any, R> ValidatorDsl<T>.field(property: KProperty1<T, R>): FieldValidatorBuilder<T, R> {
    return this.getFieldBuilder(property)
}
