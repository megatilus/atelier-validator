/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.internal

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.reflect.KProperty1

@PublishedApi
internal class AtelierValidatorImpl<T : Any> : AtelierValidator<T> {

    private val validationEngine = ValidationEngine<T>()

    @PublishedApi
    internal fun <R> getFieldBuilder(property: KProperty1<T, R>): FieldScope<T, R> =
        validationEngine.field(property)

    override fun validate(obj: T): ValidationResult =
        validationEngine.validateObject(obj)

    override fun validateFirst(obj: T): ValidationResult =
        validationEngine.validateObjectFirst(obj)
}
