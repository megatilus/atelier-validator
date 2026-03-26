/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.internal

import dev.megatilus.atelier.validator.Rule
import dev.megatilus.atelier.validator.ValidationRule
import dev.megatilus.atelier.validator.results.ValidationError
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import kotlin.reflect.KProperty1

/**
 * Internal implementation of [ValidationRule] for a specific field.
 */
@PublishedApi
internal class FieldScope<T : Any, R> @PublishedApi internal constructor(
    private val property: KProperty1<T, R>,
    override val fieldName: String,
    private val validationEngine: ValidationEngine<T>
) : ValidationRule<R> {

    internal class RuleRef(var message: String, var code: ValidationErrorCode)

    internal data class StoredRule<R>(
        val ref: RuleRef,
        val predicate: (R) -> Boolean,
        val checkNull: Boolean
    ) {
        fun validate(value: R, fieldName: String): ValidationError? {
            if (checkNull && value == null) return null

            return if (!predicate(value)) {
                ValidationError(
                    fieldName = fieldName,
                    message = ref.message.replace("{value}", value?.toString() ?: "null"),
                    code = ref.code,
                    actualValue = value?.toString()
                )
            } else {
                null
            }
        }
    }

    private val rules = mutableListOf<StoredRule<R>>()

    override fun constrain(
        message: String,
        code: ValidationErrorCode,
        predicate: (R) -> Boolean
    ): Rule {
        val ref = RuleRef(message, code)

        rules.add(StoredRule(ref, predicate, checkNull = false))
        return Rule(ref)
    }

    override fun constrainIfNotNull(
        message: String,
        code: ValidationErrorCode,
        predicate: (R & Any) -> Boolean
    ): Rule {
        val nullAwarePredict: (R) -> Boolean = { value ->
            if (value == null) {
                true
            } else {
                predicate(value)
            }
        }

        val ref = RuleRef(message, code)

        rules.add(StoredRule(ref, nullAwarePredict, checkNull = true))
        return Rule(ref)
    }

    @PublishedApi
    internal fun buildRules() {
        if (rules.isNotEmpty()) {
            val fieldValidator = FieldRule<R> { value, _ ->
                rules.mapNotNull { it.validate(value, fieldName) }
            }
            validationEngine.addFieldValidation(property, fieldName, fieldValidator)
        }
    }
}
