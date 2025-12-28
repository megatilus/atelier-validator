/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import dev.megatilus.atelier.validators.*
import kotlin.test.*

class AtelierValidatorTest {

    private data class TestUser(
        val name: String,
        val email: String,
        val age: Int,
        val password: String?
    )

    private data class SimpleTestObject(val value: String)

    @Test
    fun `atelierValidator DSL should create working validator`() {
        val validator =
            atelierValidator<SimpleTestObject> { field(SimpleTestObject::value).notBlank() }

        val validObject = SimpleTestObject("valid")
        val result = validator.validate(validObject)

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `atelierValidator should fail validation with blank value`() {
        val validator =
            atelierValidator<SimpleTestObject> { field(SimpleTestObject::value).notBlank() }

        val invalidObject = SimpleTestObject("")
        val result = validator.validate(invalidObject)

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)

        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount)
        assertEquals("value", failure.errors.first().fieldName)
        assertEquals(ValidatorCode.REQUIRED, failure.errors.first().code)
    }

    @Test
    fun `validator should handle multiple field validations`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name).notBlank("Name is required")
                field(TestUser::email).email("Invalid email format")
                field(TestUser::age).min(0, "Age must be positive")
            }

        val validUser = TestUser("John Doe", "john@example.com", 25, "password")
        val result = validator.validate(validUser)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validator should collect multiple validation errors`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name).notBlank("Name is required")
                field(TestUser::email).email("Invalid email format")
                field(TestUser::age).min(18, "Must be at least 18")
            }

        val invalidUser = TestUser("", "invalid-email", 10, null)
        val result = validator.validate(invalidUser)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(3, failure.errorCount)

        val errorFields = failure.errors.map { it.fieldName }.toSet()
        assertEquals(setOf("name", "email", "age"), errorFields)
    }

    @Test
    fun `validateFirst should return only first error`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name).notBlank("Name is required")
                field(TestUser::email).email("Invalid email format")
                field(TestUser::age).min(18, "Must be at least 18")
            }

        val invalidUser = TestUser("", "invalid-email", 10, null)
        val result = validator.validateFirst(invalidUser)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount)
    }

    @Test
    fun `validateFirst should return success when no errors`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name).notBlank()
                field(TestUser::email).email()
                field(TestUser::age).min(0)
            }

        val validUser = TestUser("John", "john@example.com", 25, "password")
        val result = validator.validateFirst(validUser)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validator should handle nullable fields`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::password).minLength(8, "Password must be at least 8 characters")
            }

        val userWithNullPassword = TestUser("John", "john@example.com", 25, null)
        val result = validator.validate(userWithNullPassword)

        assertTrue(result.isSuccess) // null should pass minLength for nullable String
    }

    @Test
    fun `validator should validate nullable fields when not null`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::password).minLength(8, "Password must be at least 8 characters")
            }

        val userWithShortPassword = TestUser("John", "john@example.com", 25, "123")
        val result = validator.validate(userWithShortPassword)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount)
        assertEquals("password", failure.errors.first().fieldName)
        assertEquals(ValidatorCode.TOO_SHORT, failure.errors.first().code)
    }

    @Test
    fun `validator should handle chained validations on same field`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name)
                    .notBlank("Name cannot be blank")
                    .minLength(2, "Name must be at least 2 characters")
                    .maxLength(50, "Name cannot exceed 50 characters")
            }

        val userWithShortName = TestUser("J", "john@example.com", 25, "password")
        val result = validator.validate(userWithShortName)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount)
        assertEquals("name", failure.errors.first().fieldName)
        assertEquals(ValidatorCode.TOO_SHORT, failure.errors.first().code)
    }

    @Test
    fun `validator should handle complex validation scenarios`() {
        val validator =
            atelierValidator<TestUser> {
                field(TestUser::name)
                    .notBlank("Name is required")
                    .length(2, 50, "Name must be between 2 and 50 characters")

                field(TestUser::email)
                    .notBlank("Email is required")
                    .email("Must be a valid email address")

                field(TestUser::age).range(18, 120, "Age must be between 18 and 120")

                field(TestUser::password).minLength(8, "Password must be at least 8 characters")
            }

        // Test completely valid user
        val validUser = TestUser("John Doe", "john.doe@example.com", 30, "password123")
        assertTrue(validator.validate(validUser).isSuccess)

        // Test completely invalid user
        val invalidUser = TestUser("", "invalid", 15, "123")
        val result = validator.validate(invalidUser)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(5, failure.errorCount)
    }

    @Test
    fun `validator should work with custom error messages`() {
        val validator =
            atelierValidator<SimpleTestObject> {
                field(SimpleTestObject::value).notBlank("This is a custom error message")
            }

        val invalidObject = SimpleTestObject("")
        val result = validator.validate(invalidObject)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals("This is a custom error message", failure.errors.first().message)
    }

    @Test
    fun `validator should preserve actual values in error details`() {
        val validator =
            atelierValidator<TestUser> { field(TestUser::age).min(18, "Must be at least 18") }

        val youngUser = TestUser("John", "john@example.com", 16, null)
        val result = validator.validate(youngUser)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals("16", failure.errors.first().actualValue)
    }

    @Test
    fun `validator should handle empty validator configuration`() {
        val validator =
            atelierValidator<SimpleTestObject> {
                // No validation rules
            }

        val anyObject = SimpleTestObject("any value")
        val result = validator.validate(anyObject)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `different validators should be independent`() {
        val validator1 =
            atelierValidator<SimpleTestObject> { field(SimpleTestObject::value).notBlank() }

        val validator2 =
            atelierValidator<SimpleTestObject> { field(SimpleTestObject::value).minLength(10) }

        val testObject = SimpleTestObject("short")

        val result1 = validator1.validate(testObject)
        val result2 = validator2.validate(testObject)

        assertTrue(result1.isSuccess) // Passes notBlank
        assertTrue(result2.isFailure) // Fails minLength(10)
    }

    @Test
    fun `validator should implement Validator interface correctly`() {
        val validator: AtelierValidatorContract<SimpleTestObject> = atelierValidator {
            field(SimpleTestObject::value).notBlank()
        }

        val validObject = SimpleTestObject("valid")
        val invalidObject = SimpleTestObject("")

        assertTrue(validator.validate(validObject).isSuccess)
        assertTrue(validator.validate(invalidObject).isFailure)
        assertTrue(validator.validateFirst(validObject).isSuccess)
        assertTrue(validator.validateFirst(invalidObject).isFailure)
    }

    @Test
    fun `new invoke syntax should work with single field`() {
        val validator = atelierValidator<SimpleTestObject> {
            SimpleTestObject::value {
                notBlank()
            }
        }

        val validObject = SimpleTestObject("valid")
        val invalidObject = SimpleTestObject("")

        assertTrue(validator.validate(validObject).isSuccess)
        assertTrue(validator.validate(invalidObject).isFailure)
    }

    @Test
    fun `new invoke syntax should work with multiple fields`() {
        val validator = atelierValidator<TestUser> {
            TestUser::name {
                notBlank()
                minLength(2)
            }

            TestUser::email {
                notBlank()
                email()
            }

            TestUser::age {
                min(18)
                max(120)
            }
        }

        val validUser = TestUser("John", "john@example.com", 25, null)
        val invalidUser = TestUser("", "invalid-email", 15, null)

        val validResult = validator.validate(validUser)
        assertTrue(validResult.isSuccess)

        val invalidResult = validator.validate(invalidUser)
        assertTrue(invalidResult.isFailure)
        val failure = invalidResult as ValidationResult.Failure
        assertTrue(failure.errorCount >= 3) // name blank, email invalid, age < 18
    }

    @Test
    fun `new invoke syntax and old field syntax should work together`() {
        val validator = atelierValidator<TestUser> {
            // New syntax
            TestUser::name {
                notBlank()
            }

            // Old syntax
            field(TestUser::email).email()

            // New syntax again
            TestUser::age {
                min(0)
            }
        }

        val validUser = TestUser("John", "john@example.com", 25, null)
        val result = validator.validate(validUser)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `new invoke syntax should work with chained validations`() {
        val validator = atelierValidator<TestUser> {
            TestUser::name {
                notBlank("Name is required")
                minLength(2, "Name must be at least 2 characters")
                maxLength(50, "Name cannot exceed 50 characters")
            }
        }

        val validUser = TestUser("John Doe", "john@example.com", 25, null)
        val tooShortUser = TestUser("J", "john@example.com", 25, null)
        val blankNameUser = TestUser("", "john@example.com", 25, null)

        assertTrue(validator.validate(validUser).isSuccess)
        assertTrue(validator.validate(tooShortUser).isFailure)
        assertTrue(validator.validate(blankNameUser).isFailure)
    }

    @Test
    fun `new invoke syntax should support nullable fields`() {
        val validator = atelierValidator<TestUser> {
            TestUser::password {
                minLength(8)
            }
        }

        val userWithPassword = TestUser("John", "john@example.com", 25, "password123")
        val userWithShortPassword = TestUser("John", "john@example.com", 25, "short")
        val userWithNullPassword = TestUser("John", "john@example.com", 25, null)

        assertTrue(validator.validate(userWithPassword).isSuccess)
        assertTrue(validator.validate(userWithShortPassword).isFailure)
        assertTrue(validator.validate(userWithNullPassword).isSuccess)
    }
}
