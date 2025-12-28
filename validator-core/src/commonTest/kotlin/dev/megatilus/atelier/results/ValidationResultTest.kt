/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationResultTest {

    @Test
    fun `ValidationResult Success should have correct properties`() {
        val result = ValidationResult.Success

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `ValidationResult Failure should have correct properties`() {
        val error =
            ValidationErrorDetail(
                fieldName = "testField",
                message = "Test error message",
                code = ValidatorCode.REQUIRED,
                actualValue = "actualValue"
            )
        val result = ValidationResult.Failure(error)

        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertEquals(1, result.errorCount)
        assertEquals(listOf(error), result.errors)
    }

    @Test
    fun `ValidationResult Failure with multiple errors should handle correctly`() {
        val error1 = ValidationErrorDetail("field1", "Error 1", ValidatorCode.REQUIRED, "value1")
        val error2 = ValidationErrorDetail("field2", "Error 2", ValidatorCode.TOO_SHORT, "value2")
        val error3 =
            ValidationErrorDetail("field1", "Error 3", ValidatorCode.INVALID_EMAIL, "value3")

        val result = ValidationResult.Failure(listOf(error1, error2, error3))

        assertEquals(3, result.errorCount)
        assertEquals(3, result.errors.size)

        // Test errorsByField grouping
        val errorsByField = result.errorsByField
        assertEquals(2, errorsByField.size)
        assertEquals(2, errorsByField["field1"]?.size)
        assertEquals(1, errorsByField["field2"]?.size)

        // Test errorsFor method
        val field1Errors = result.errorsFor("field1")
        assertEquals(2, field1Errors.size)
        assertTrue(field1Errors.contains(error1))
        assertTrue(field1Errors.contains(error3))

        val field2Errors = result.errorsFor("field2")
        assertEquals(1, field2Errors.size)
        assertTrue(field2Errors.contains(error2))

        val nonExistentFieldErrors = result.errorsFor("nonExistent")
        assertEquals(0, nonExistentFieldErrors.size)
    }

    @Test
    fun `ValidationResult Failure firstErrorFor should return correct error`() {
        val error1 = ValidationErrorDetail("field1", "Error 1", ValidatorCode.REQUIRED, "value1")
        val error2 = ValidationErrorDetail("field2", "Error 2", ValidatorCode.TOO_SHORT, "value2")
        val error3 =
            ValidationErrorDetail("field1", "Error 3", ValidatorCode.INVALID_EMAIL, "value3")

        val result = ValidationResult.Failure(listOf(error1, error2, error3))

        // Should return first error for field1
        val firstField1Error = result.firstErrorFor("field1")
        assertEquals(error1, firstField1Error)

        // Should return the only error for field2
        val firstField2Error = result.firstErrorFor("field2")
        assertEquals(error2, firstField2Error)

        // Should return null for non-existent field
        val nonExistentError = result.firstErrorFor("nonExistent")
        assertNull(nonExistentError)
    }

    @Test
    fun `ValidationErrorDetail should have correct toString representation`() {
        val error =
            ValidationErrorDetail(
                fieldName = "username",
                message = "Username cannot be blank",
                code = ValidatorCode.REQUIRED,
                actualValue = ""
            )

        assertEquals("username: Username cannot be blank", error.toString())
    }

    @Test
    fun `ValidationErrorDetail should handle special characters in toString`() {
        val error =
            ValidationErrorDetail(
                fieldName = "user.email",
                message = "Email format is invalid: special@chars.com",
                code = ValidatorCode.INVALID_EMAIL,
                actualValue = "special@chars.com"
            )

        assertEquals("user.email: Email format is invalid: special@chars.com", error.toString())
    }

    @Test
    fun `ValidationResult Failure constructor with single error should work`() {
        val error = ValidationErrorDetail("field", "message", ValidatorCode.CUSTOM_ERROR, "value")
        val result = ValidationResult.Failure(error)

        assertEquals(1, result.errorCount)
        assertEquals(listOf(error), result.errors)
    }

    @Test
    fun `ValidationResult Failure with empty error list should handle correctly`() {
        val result = ValidationResult.Failure(emptyList())

        assertEquals(0, result.errorCount)
        assertTrue(result.errors.isEmpty())
        assertTrue(result.errorsByField.isEmpty())

        val errorsForAnyField = result.errorsFor("anyField")
        assertTrue(errorsForAnyField.isEmpty())

        val firstErrorForAnyField = result.firstErrorFor("anyField")
        assertNull(firstErrorForAnyField)
    }

    @Test
    fun `ValidationErrorDetail should preserve all field values correctly`() {
        val fieldName = "testField"
        val message = "Test message with special characters: éàù@#$%"
        val code = ValidatorCode.OUT_OF_RANGE
        val actualValue = "actualValue123"

        val error = ValidationErrorDetail(fieldName, message, code, actualValue)

        assertEquals(fieldName, error.fieldName)
        assertEquals(message, error.message)
        assertEquals(code, error.code)
        assertEquals(actualValue, error.actualValue)
    }

    @Test
    fun `ValidationResult Failure errorsByField should handle duplicate field names correctly`() {
        val error1 = ValidationErrorDetail("field1", "Error 1", ValidatorCode.REQUIRED, "")
        val error2 = ValidationErrorDetail("field1", "Error 2", ValidatorCode.TOO_SHORT, "ab")
        val error3 = ValidationErrorDetail("field1", "Error 3", ValidatorCode.INVALID_FORMAT, "123")

        val result = ValidationResult.Failure(listOf(error1, error2, error3))
        val errorsByField = result.errorsByField

        assertEquals(1, errorsByField.size)
        assertEquals(3, errorsByField["field1"]?.size)

        val field1Errors = errorsByField["field1"]!!
        assertTrue(field1Errors.contains(error1))
        assertTrue(field1Errors.contains(error2))
        assertTrue(field1Errors.contains(error3))
    }
}
