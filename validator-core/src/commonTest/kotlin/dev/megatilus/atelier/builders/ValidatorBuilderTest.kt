/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.builders

import dev.megatilus.atelier.atelierValidator
import dev.megatilus.atelier.field
import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorBuilderTest {

    private data class TestObject(
        val stringField: String,
        val intField: Int,
        val nullableStringField: String?,
        val booleanField: Boolean
    )

    @Test
    fun `ValidatorBuilder should handle single field validation`() {
        val builder = ValidatorBuilder<TestObject>()
        val fieldBuilder = builder.field(TestObject::stringField)

        // Add a simple validation
        fieldBuilder.constraint(
            hint = "Must not be blank",
            code = ValidatorCode.NOT_BLANK,
            predicate = { it.isNotBlank() }
        )

        val validObject = TestObject("valid", 1, null, true)
        val result = builder.validateObject(validObject)
        assertTrue(result.isSuccess)

        val invalidObject = TestObject("", 1, null, true)
        val invalidResult = builder.validateObject(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `ValidatorBuilder should handle multiple field validations`() {
        val builder = ValidatorBuilder<TestObject>()

        // Add validation for string field
        builder.field(TestObject::stringField)
            .constraint(
                hint = "Must not be blank",
                code = ValidatorCode.NOT_BLANK,
                predicate = { it.isNotBlank() }
            )

        // Add validation for int field
        builder.field(TestObject::intField)
            .constraint(
                hint = "Must be positive",
                code = ValidatorCode.OUT_OF_RANGE,
                predicate = { it > 0 }
            )

        val validObject = TestObject("valid", 5, null, true)
        val result = builder.validateObject(validObject)
        assertTrue(result.isSuccess)

        val invalidObject = TestObject("", -1, null, true)
        val invalidResult = builder.validateObject(invalidObject)
        assertTrue(invalidResult.isFailure)

        val failure = invalidResult as ValidationResult.Failure
        assertEquals(2, failure.errorCount)
    }

    @Test
    fun `ValidatorBuilder should handle field replacement correctly`() {
        val builder = ValidatorBuilder<TestObject>()

        // Add initial validation
        val fieldBuilder = builder.field(TestObject::stringField)
        fieldBuilder.constraint(
            hint = "Initial validation",
            code = ValidatorCode.NOT_BLANK,
            predicate = { it.isNotBlank() }
        )

        // Replace with different validation
        builder.addFieldValidation(
            property = TestObject::stringField,
            fieldName = "stringField",
            validator = { value, fieldName ->
                if (value.length < 5) {
                    listOf(
                        ValidationErrorDetail(
                            fieldName = fieldName,
                            message = "Must be at least 5 characters",
                            code = ValidatorCode.TOO_SHORT,
                            actualValue = value
                        )
                    )
                } else {
                    emptyList()
                }
            }
        )

        val shortStringObject = TestObject("abc", 1, null, true)
        val result = builder.validateObject(shortStringObject)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals("Must be at least 5 characters", failure.errors.first().message)
        assertEquals(ValidatorCode.TOO_SHORT, failure.errors.first().code)
    }

    @Test
    fun `ValidatorBuilder should handle nullable fields correctly`() {
        val builder = ValidatorBuilder<TestObject>()

        builder.field(TestObject::nullableStringField)
            .constraint(
                hint = "Must be at least 3 characters when not null",
                code = ValidatorCode.TOO_SHORT,
                predicate = { it == null || it.length >= 3 }
            )

        // Null should pass
        val nullObject = TestObject("valid", 1, null, true)
        assertTrue(builder.validateObject(nullObject).isSuccess)

        // Valid non-null should pass
        val validObject = TestObject("valid", 1, "hello", true)
        assertTrue(builder.validateObject(validObject).isSuccess)

        // Invalid non-null should fail
        val invalidObject = TestObject("valid", 1, "hi", true)
        val result = builder.validateObject(invalidObject)
        assertTrue(result.isFailure)
    }

    @Test
    fun `ValidatorBuilder should handle custom field names`() {
        val builder = ValidatorBuilder<TestObject>()

        val fieldBuilder = builder.field(TestObject::stringField, "customFieldName")
        fieldBuilder.constraint(
            hint = "Custom validation failed",
            code = ValidatorCode.INVALID_VALUE,
            predicate = { false } // Always fail for testing
        )

        val result = builder.validateObject(TestObject("test", 1, null, true))
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals("customFieldName", failure.errors.first().fieldName)
    }

    @Test
    fun `ValidatorBuilder should use property name when custom name not provided`() {
        val builder = ValidatorBuilder<TestObject>()

        builder.field(TestObject::intField)
            .constraint(
                hint = "Test validation",
                code = ValidatorCode.INVALID_VALUE,
                predicate = { false }
            )

        val result = builder.validateObject(TestObject("test", 1, null, true))
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals("intField", failure.errors.first().fieldName)
    }

    @Test
    fun `ValidatorBuilder should handle empty validation with no rules`() {
        val builder = ValidatorBuilder<TestObject>()

        // No validation rules added
        val result = builder.validateObject(TestObject("any", -999, null, false))
        assertTrue(result.isSuccess, "Empty validator should pass any object")
    }

    @Test
    fun `ValidatorBuilder should handle isEqualTo validation`() {
        data class PasswordForm(val password: String, val confirmPassword: String)

        val validator =
            atelierValidator<PasswordForm> {
                field(PasswordForm::confirmPassword)
                    .isEqualTo({ it.password }, "Passwords must match")
            }

        // Should pass when passwords match
        val validForm = PasswordForm("secret123", "secret123")
        assertTrue(validator.validate(validForm).isSuccess)

        // Should fail when passwords don't match
        val invalidForm = PasswordForm("secret123", "different")
        val result = validator.validate(invalidForm)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals("confirmPassword", failure.errors.first().fieldName)
        assertEquals("Passwords must match", failure.errors.first().message)
    }

    @Test
    fun `ValidatorBuilder validateObjectFirst should return first error only`() {
        val builder = ValidatorBuilder<TestObject>()

        // Add multiple validations that will all fail
        builder.field(TestObject::stringField)
            .constraint(
                hint = "String validation failed",
                code = ValidatorCode.NOT_BLANK,
                predicate = { false }
            )

        builder.field(TestObject::intField)
            .constraint(
                hint = "Int validation failed",
                code = ValidatorCode.OUT_OF_RANGE,
                predicate = { false }
            )

        val result = builder.validateObjectFirst(TestObject("test", 1, null, true))
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount, "Should return only first error")
    }

    @Test
    fun `ValidatorBuilder should handle isEqualTo with different types`() {
        data class FormData(
            val age: Int,
            val confirmAge: Int,
            val email: String,
            val confirmEmail: String
        )

        val validator =
            atelierValidator<FormData> {
                field(FormData::confirmAge).isEqualTo({ it.age })
                field(FormData::confirmEmail).isEqualTo({ it.email })
            }

        // Should pass when all fields match
        val validForm = FormData(25, 25, "test@example.com", "test@example.com")
        assertTrue(validator.validate(validForm).isSuccess)

        // Should fail when age doesn't match
        val invalidAgeForm = FormData(25, 26, "test@example.com", "test@example.com")
        val ageResult = validator.validate(invalidAgeForm)
        assertTrue(ageResult.isFailure)

        val ageFailure = ageResult as ValidationResult.Failure
        assertEquals("confirmAge", ageFailure.errors.first().fieldName)

        // Should fail when email doesn't match
        val invalidEmailForm = FormData(25, 25, "test@example.com", "different@example.com")
        val emailResult = validator.validate(invalidEmailForm)
        assertTrue(emailResult.isFailure)

        val emailFailure = emailResult as ValidationResult.Failure
        assertEquals("confirmEmail", emailFailure.errors.first().fieldName)
    }

    @Test
    fun `ValidatorBuilder should combine field and isEqualTo validations`() {
        data class RegistrationForm(val email: String, val confirmEmail: String)

        val validator =
            atelierValidator<RegistrationForm> {
                field(RegistrationForm::email)
                    .constraint(
                        hint = "Email must not be blank",
                        code = ValidatorCode.NOT_BLANK,
                        predicate = { it.isNotBlank() }
                    )
                field(RegistrationForm::confirmEmail)
                    .isEqualTo({ it.email }, "Emails must match")
            }

        // Object that fails field validation (blank email)
        val invalidObject = RegistrationForm("", "")
        val result = validator.validate(invalidObject)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals(ValidatorCode.NOT_BLANK, failure.errors.first().code)

        // Object that passes field validation but fails isEqualTo validation
        val mismatchObject = RegistrationForm("test@example.com", "different@example.com")
        val mismatchResult = validator.validate(mismatchObject)
        assertTrue(mismatchResult.isFailure)

        val mismatchFailure = mismatchResult as ValidationResult.Failure
        assertEquals("confirmEmail", mismatchFailure.errors.first().fieldName)
        assertEquals("Emails must match", mismatchFailure.errors.first().message)
    }

    @Test
    fun `ValidatorBuilder should work with atelierValidator function`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::stringField)
                    .constraint(
                        hint = "String must not be blank",
                        code = ValidatorCode.NOT_BLANK,
                        predicate = { it.isNotBlank() }
                    )
            }

        val validObject = TestObject("valid", 1, null, true)
        assertTrue(validator.validate(validObject).isSuccess)

        val invalidObject = TestObject("", 1, null, true)
        assertTrue(validator.validate(invalidObject).isFailure)
    }

    @Test
    fun `ValidatorBuilder should handle isEqualTo with multiple types`() {
        data class MultiTypeForm(
            val count: Int,
            val confirmCount: Int,
            val price: Double,
            val confirmPrice: Double,
            val active: Boolean,
            val confirmActive: Boolean,
            val name: String,
            val confirmName: String
        )

        val validator =
            atelierValidator<MultiTypeForm> {
                field(MultiTypeForm::confirmCount).isEqualTo({ it.count }, "Count must match")
                field(MultiTypeForm::confirmPrice).isEqualTo({ it.price }, "Price must match")
                field(MultiTypeForm::confirmActive)
                    .isEqualTo({ it.active }, "Active flag must match")
                field(MultiTypeForm::confirmName).isEqualTo({ it.name }, "Name must match")
            }

        // All fields match - should pass
        val validForm =
            MultiTypeForm(
                count = 42,
                confirmCount = 42,
                price = 99.99,
                confirmPrice = 99.99,
                active = true,
                confirmActive = true,
                name = "Product",
                confirmName = "Product"
            )
        assertTrue(validator.validate(validForm).isSuccess)

        // Int mismatch - should fail
        val intMismatch =
            MultiTypeForm(
                count = 42,
                confirmCount = 43,
                price = 99.99,
                confirmPrice = 99.99,
                active = true,
                confirmActive = true,
                name = "Product",
                confirmName = "Product"
            )
        val intResult = validator.validate(intMismatch)
        assertTrue(intResult.isFailure)
        val intFailure = intResult as ValidationResult.Failure
        assertEquals("confirmCount", intFailure.errors.first().fieldName)
        assertEquals("Count must match", intFailure.errors.first().message)

        // Double mismatch - should fail
        val doubleMismatch =
            MultiTypeForm(
                count = 42,
                confirmCount = 42,
                price = 99.99,
                confirmPrice = 100.0,
                active = true,
                confirmActive = true,
                name = "Product",
                confirmName = "Product"
            )
        val doubleResult = validator.validate(doubleMismatch)
        assertTrue(doubleResult.isFailure)
        val doubleFailure = doubleResult as ValidationResult.Failure
        assertEquals("confirmPrice", doubleFailure.errors.first().fieldName)

        // Boolean mismatch - should fail
        val boolMismatch =
            MultiTypeForm(
                count = 42,
                confirmCount = 42,
                price = 99.99,
                confirmPrice = 99.99,
                active = true,
                confirmActive = false,
                name = "Product",
                confirmName = "Product"
            )
        val boolResult = validator.validate(boolMismatch)
        assertTrue(boolResult.isFailure)
        val boolFailure = boolResult as ValidationResult.Failure
        assertEquals("confirmActive", boolFailure.errors.first().fieldName)

        // String mismatch - should fail
        val stringMismatch =
            MultiTypeForm(
                count = 42,
                confirmCount = 42,
                price = 99.99,
                confirmPrice = 99.99,
                active = true,
                confirmActive = true,
                name = "Product",
                confirmName = "Different"
            )
        val stringResult = validator.validate(stringMismatch)
        assertTrue(stringResult.isFailure)
        val stringFailure = stringResult as ValidationResult.Failure
        assertEquals("confirmName", stringFailure.errors.first().fieldName)
    }

    @Test
    fun `ValidatorBuilder should handle isEqualTo with nullable fields`() {
        data class NullableForm(val value: String?, val confirmValue: String?)

        val validator =
            atelierValidator<NullableForm> {
                field(NullableForm::confirmValue).isEqualTo({ it.value }, "Values must match")
            }

        // Both null should pass
        val bothNull = NullableForm(null, null)
        assertTrue(validator.validate(bothNull).isSuccess)

        // Both same value should pass
        val bothSame = NullableForm("test", "test")
        assertTrue(validator.validate(bothSame).isSuccess)

        // Different values should fail
        val different = NullableForm("test", "other")
        val result = validator.validate(different)
        assertTrue(result.isFailure)

        // One null, one not should fail
        val oneNull = NullableForm("test", null)
        val nullResult = validator.validate(oneNull)
        assertTrue(nullResult.isFailure)
    }
}
