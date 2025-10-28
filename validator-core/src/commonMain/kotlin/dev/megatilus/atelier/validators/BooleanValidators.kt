/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.jvm.JvmName

public fun <T : Any> FieldValidatorBuilder<T, Boolean>.isTrue(
    message: String? = null
): FieldValidatorBuilder<T, Boolean> {
    return constraint(
        hint = message ?: "Must be true",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, Boolean>.isFalse(
    message: String? = null
): FieldValidatorBuilder<T, Boolean> {
    return constraint(
        hint = message ?: "Must be false",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { !it }
    )
}

@JvmName("isTrueNullable")
public fun <T : Any> FieldValidatorBuilder<T, Boolean?>.isTrue(
    message: String? = null
): FieldValidatorBuilder<T, Boolean?> {
    return constraint(
        hint = message ?: "Must be true",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == true }
    )
}

@JvmName("isFalseNullable")
public fun <T : Any> FieldValidatorBuilder<T, Boolean?>.isFalse(
    message: String? = null
): FieldValidatorBuilder<T, Boolean?> {
    return constraint(
        hint = message ?: "Must be false",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == false }
    )
}
