/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.builders

import dev.megatilus.atelier.atelierValidator
import dev.megatilus.atelier.field
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.test.*

class ConstraintFunctionTest {

    data class TestData(val value: String, val count: Int)

    @Test
    fun `constraint should allow custom validation logic`() {
        val validator = atelierValidator<TestData> {
            field(TestData::value).constraint(
                hint = "Must start with uppercase",
                code = ValidatorCode.INVALID_FORMAT,
                predicate = { it.first().isUpperCase() }
            )
        }

        val valid = TestData("Hello", 5)
        val invalid = TestData("hello", 5)

        assertTrue(validator.validate(valid) is ValidationResult.Success)
        assertTrue(validator.validate(invalid) is ValidationResult.Failure)
    }

    @Test
    fun `constraint should work with complex business rules`() {
        val validator = atelierValidator<TestData> {
            field(TestData::count).constraint(
                hint = "Count must be even and positive",
                code = ValidatorCode.INVALID_VALUE,
                predicate = { it > 0 && it % 2 == 0 }
            )
        }

        assertTrue(validator.validate(TestData("test", 2)) is ValidationResult.Success)
        assertTrue(validator.validate(TestData("test", 4)) is ValidationResult.Success)
        assertTrue(validator.validate(TestData("test", 1)) is ValidationResult.Failure)
        assertTrue(validator.validate(TestData("test", -2)) is ValidationResult.Failure)
    }
}
