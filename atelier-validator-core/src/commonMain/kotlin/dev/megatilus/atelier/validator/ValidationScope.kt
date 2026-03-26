/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator

import dev.megatilus.atelier.validator.internal.AtelierValidatorImpl
import kotlin.reflect.KProperty1

/**
 * DSL scope for configuring validators.
 */
public class ValidationScope<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val validator: AtelierValidatorImpl<T>
) {
    public inline operator fun <R> KProperty1<T, R>.invoke(
        configure: ValidationRule<R>.() -> Unit
    ) {
        val fieldScope = validator.getFieldBuilder(this)
        fieldScope.configure()
        fieldScope.buildRules()
    }
}
