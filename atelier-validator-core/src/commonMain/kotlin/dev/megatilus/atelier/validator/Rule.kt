/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator

import dev.megatilus.atelier.validator.internal.FieldScope
import dev.megatilus.atelier.validator.results.ValidationErrorCode

/**
 * Represents a registered validation rule.
 *
 * Immutable - each modification returns a new instance.
 */
@ConsistentCopyVisibility
public data class Rule internal constructor(
    internal val ref: FieldScope.RuleRef
) {
    public companion object {
        public fun create(message: String, code: ValidationErrorCode): Rule =
            Rule(FieldScope.RuleRef(message, code))
    }

    internal val message get() = ref.message
    internal val code get() = ref.code

    public infix fun hint(customMessage: String): Rule {
        ref.message = customMessage
        return this
    }

    public infix fun hint(block: () -> String): Rule {
        ref.message = block()
        return this
    }

    public infix fun withCode(errorCode: ValidationErrorCode): Rule {
        ref.code = errorCode
        return this
    }

    public infix fun withCode(block: () -> ValidationErrorCode): Rule {
        ref.code = block()
        return this
    }
}
