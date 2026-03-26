/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleTest {

    data class TestUser(val name: String?, val age: Int?)

    @Test
    fun `constrain should validate non-null values`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Name must not be empty",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { !it.isNullOrEmpty() }
                )
            }
        }

        val validUser = TestUser(name = "John", age = 25)
        val result = validator.validate(validUser)

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `constrain should fail when predicate returns false`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Name must not be empty",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { !it.isNullOrEmpty() }
                )
            }
        }

        val invalidUser = TestUser(name = "", age = 25)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("name", result.errors.first().fieldName)
        assertEquals("Name must not be empty", result.errors.first().message)
    }

    @Test
    fun `constrain should handle null values in predicate`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Name is required",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { it != null }
                )
            }
        }

        val nullUser = TestUser(name = null, age = 25)
        val result = validator.validate(nullUser)

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `constrain with hint should override message`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Default message",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { it != null }
                ) hint "Custom hint message"
            }
        }

        val invalidUser = TestUser(name = null, age = 25)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertEquals("Custom hint message", result.errors.first().message)
    }

    @Test
    fun `constrainIfNotNull should skip validation for null values`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Name must be at least 3 chars",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 3 }
                )
            }
        }

        val nullUser = TestUser(name = null, age = 25)
        val result = validator.validate(nullUser)

        // null should pass - validation skipped
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `constrainIfNotNull should validate non-null values`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Name must be at least 3 chars",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 3 }
                )
            }
        }

        val validUser = TestUser(name = "John", age = 25)
        val result = validator.validate(validUser)

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `constrainIfNotNull should fail for invalid non-null values`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Name must be at least 3 chars",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 3 }
                )
            }
        }

        val invalidUser = TestUser(name = "Jo", age = 25)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("name", result.errors.first().fieldName)
        assertEquals("Name must be at least 3 chars", result.errors.first().message)
    }

    @Test
    fun `constrainIfNotNull predicate should receive non-null value`() {
        var receivedValue: String? = "not called"

        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Test",
                    code = ValidationErrorCode.INVALID_VALUE,
                    predicate = {
                        receivedValue = it
                        it.isNotEmpty()
                    }
                )
            }
        }

        validator.validate(TestUser(name = "John", age = 25))

        // Predicate was called with non-null value
        assertEquals("John", receivedValue)
    }

    @Test
    fun `constrainIfNotNull predicate should not be called for null`() {
        var wasCalled = false

        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Test",
                    code = ValidationErrorCode.INVALID_VALUE,
                    predicate = {
                        wasCalled = true
                        true
                    }
                )
            }
        }

        validator.validate(TestUser(name = null, age = 25))

        // Predicate was NOT called for null
        assertFalse(wasCalled)
    }

    @Test
    fun `multiple constrain rules should all execute`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Name is required",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { it != null }
                )
                constrainIfNotNull(
                    message = "Name must be at least 2 chars",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 2 }
                )
                constrainIfNotNull(
                    message = "Name must be at most 50 chars",
                    code = ValidationErrorCode.TOO_LONG,
                    predicate = { it.length <= 50 }
                )
            }
        }

        // Valid user - all rules pass
        val validUser = TestUser(name = "John", age = 25)
        assertTrue(validator.validate(validUser) is ValidationResult.Success)

        // Invalid user - fails first rule
        val nullUser = TestUser(name = null, age = 25)
        val nullResult = validator.validate(nullUser)
        assertTrue(nullResult is ValidationResult.Failure)
        assertEquals(1, nullResult.errorCount) // Only first rule fails

        // Invalid user - fails second rule
        val shortNameUser = TestUser(name = "J", age = 25)
        val shortResult = validator.validate(shortNameUser)
        assertTrue(shortResult is ValidationResult.Failure)
        assertEquals(1, shortResult.errorCount)
        assertEquals("Name must be at least 2 chars", shortResult.errors.first().message)
    }

    @Test
    fun `duplicate rules should both execute and produce multiple errors`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrainIfNotNull(
                    message = "Name too short - first check",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 5 }
                )
                constrainIfNotNull(
                    message = "Name too short - second check",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 5 }
                )
            }
        }

        val invalidUser = TestUser(name = "Jo", age = 25)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        // Both duplicate rules execute
        assertEquals(2, result.errorCount)
    }

    @Test
    fun `constrainIfNotNull should work with numeric nullables`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::age {
                constrainIfNotNull(
                    message = "Age must be at least 18",
                    code = ValidationErrorCode.OUT_OF_RANGE,
                    predicate = { it >= 18 }
                )
            }
        }

        // null age - should pass
        assertTrue(validator.validate(TestUser(name = "John", age = null)) is ValidationResult.Success)

        // Valid age - should pass
        assertTrue(validator.validate(TestUser(name = "John", age = 25)) is ValidationResult.Success)

        // Invalid age - should fail
        val result = validator.validate(TestUser(name = "John", age = 16))
        assertTrue(result is ValidationResult.Failure)
        assertEquals("age", result.errors.first().fieldName)
    }

    @Test
    fun `hint should override message for both constrain and constrainIfNotNull`() {
        val validator = AtelierValidator<TestUser> {
            TestUser::name {
                constrain(
                    message = "Default constrain message",
                    code = ValidationErrorCode.REQUIRED,
                    predicate = { it != null }
                ) hint "Custom not null message"

                constrainIfNotNull(
                    message = "Default constrainIfNotNull message",
                    code = ValidationErrorCode.TOO_SHORT,
                    predicate = { it.length >= 3 }
                ) hint "Custom length message"
            }
        }

        // Test null - uses custom hint from constrain
        val nullResult = validator.validate(TestUser(name = null, age = 25))
        assertTrue(nullResult is ValidationResult.Failure)
        assertEquals("Custom not null message", nullResult.errors.first().message)

        // Test short name - uses custom hint from constrainIfNotNull
        val shortResult = validator.validate(TestUser(name = "Jo", age = 25))
        assertTrue(shortResult is ValidationResult.Failure)
        assertEquals("Custom length message", shortResult.errors.first().message)
    }
}
