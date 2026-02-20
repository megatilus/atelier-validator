/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationErrorDetail
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import io.ktor.http.*
import kotlin.test.*

class AtelierValidatorClientErrorTest {

    @Test
    fun testClientValidationExceptionCreation() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail(
                    fieldName = "email",
                    message = "Invalid email format",
                    code = ValidatorCode.INVALID_FORMAT,
                    actualValue = "invalid-email"
                )
            )
        )

        val exception = AtelierClientValidationException(
            validationResult = failure,
            url = "https://api.example.com/users/1"
        )

        assertEquals(failure, exception.validationResult)
        assertEquals("https://api.example.com/users/1", exception.url)
        assertTrue(exception.message!!.contains("1 error(s)"))
        assertTrue(exception.message!!.contains("https://api.example.com/users/1"))
    }

    @Test
    fun testClientValidationExceptionWithMultipleErrors() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("email", "Invalid email", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("age", "Too young", ValidatorCode.OUT_OF_RANGE, "-5"),
                ValidationErrorDetail("name", "Too short", ValidatorCode.TOO_SHORT, "J")
            )
        )

        val exception = AtelierClientValidationException(failure, "http://test.com")

        assertEquals(3, exception.validationResult.errorCount)
        assertTrue(exception.message!!.contains("3 error(s)"))
    }

    @Test
    fun testClientValidationExceptionHasErrorFor() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("email", "Invalid", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("age", "Invalid", ValidatorCode.OUT_OF_RANGE, "-5")
            )
        )

        val exception = AtelierClientValidationException(failure)

        assertTrue(exception.hasErrorFor("email"))
        assertTrue(exception.hasErrorFor("age"))
        assertFalse(exception.hasErrorFor("name"))
    }

    @Test
    fun testClientValidationExceptionErrorsFor() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("email", "Invalid format", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("email", "Already exists", ValidatorCode.CUSTOM_ERROR, "bad"),
                ValidationErrorDetail("age", "Too young", ValidatorCode.OUT_OF_RANGE, "-5")
            )
        )

        val exception = AtelierClientValidationException(failure)

        val emailErrors = exception.errorsFor("email")
        assertEquals(2, emailErrors.size)
        assertTrue(emailErrors.any { it.message == "Invalid format" })
        assertTrue(emailErrors.any { it.message == "Already exists" })

        val ageErrors = exception.errorsFor("age")
        assertEquals(1, ageErrors.size)
        assertEquals("Too young", ageErrors[0].message)

        val nameErrors = exception.errorsFor("name")
        assertTrue(nameErrors.isEmpty())
    }

    @Test
    fun testClientValidationExceptionWithoutUrl() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("field", "error", ValidatorCode.REQUIRED, "")
            )
        )

        val exception = AtelierClientValidationException(failure)

        assertNull(exception.url)
        assertFalse(exception.message!!.contains("for"))
    }

    @Test
    fun testClientValidationErrorDetailFrom() {
        val domainError = ValidationErrorDetail(
            fieldName = "email",
            message = "Invalid email format",
            code = ValidatorCode.INVALID_FORMAT,
            actualValue = "not-an-email"
        )

        val dto = ClientValidationErrorDetail.from(domainError, "https://api.example.com/users/1")

        assertEquals("email", dto.field)
        assertEquals("Invalid email format", dto.message)
        assertEquals(ValidatorCode.INVALID_FORMAT.toString(), dto.code)
        assertEquals("not-an-email", dto.value)
        assertEquals("https://api.example.com/users/1", dto.url)
    }

    @Test
    fun testClientValidationErrorDetailFromWithoutUrl() {
        val domainError = ValidationErrorDetail(
            fieldName = "age",
            message = "Must be positive",
            code = ValidatorCode.OUT_OF_RANGE,
            actualValue = "-10"
        )

        val dto = ClientValidationErrorDetail.from(domainError)

        assertEquals("age", dto.field)
        assertNull(dto.url)
    }

    @Test
    fun testClientValidationErrorResponseFromException() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("email", "Invalid", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("age", "Too young", ValidatorCode.OUT_OF_RANGE, "-5")
            )
        )

        val exception = AtelierClientValidationException(
            validationResult = failure,
            url = "https://api.example.com/users/1"
        )

        val response = ClientValidationErrorResponse.from(exception)

        assertEquals("Response validation failed", response.message)
        assertEquals(2, response.errors.size)
        assertEquals("https://api.example.com/users/1", response.url)

        assertTrue(response.errors.any { it.field == "email" })
        assertTrue(response.errors.any { it.field == "age" })
    }

    @Test
    fun testClientValidationErrorResponseFromFailure() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("name", "Required", ValidatorCode.REQUIRED, "")
            )
        )

        val response = ClientValidationErrorResponse.from(
            failure = failure,
            url = "https://api.example.com/products/1"
        )

        assertEquals("Response validation failed", response.message)
        assertEquals(1, response.errors.size)
        assertEquals("name", response.errors[0].field)
        assertEquals("https://api.example.com/products/1", response.url)
    }

    @Test
    fun testClientValidationErrorResponseWithMultipleErrors() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("field1", "Error 1", ValidatorCode.REQUIRED, ""),
                ValidationErrorDetail("field2", "Error 2", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("field3", "Error 3", ValidatorCode.OUT_OF_RANGE, "999")
            )
        )

        val response = ClientValidationErrorResponse.from(failure)

        assertEquals(3, response.errors.size)
        assertEquals("field1", response.errors[0].field)
        assertEquals("field2", response.errors[1].field)
        assertEquals("field3", response.errors[2].field)
    }

    @Test
    fun testClientValidationErrorResponseWithoutUrl() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("field", "error", ValidatorCode.CUSTOM_ERROR, "value")
            )
        )

        val response = ClientValidationErrorResponse.from(failure)

        assertNull(response.url)
    }

    @Test
    fun testEmptyValidationFailure() {
        val failure = ValidationResult.Failure(emptyList())
        val exception = AtelierClientValidationException(failure)

        assertEquals(0, exception.validationResult.errorCount)
        assertTrue(exception.message!!.contains("0 error(s)"))
    }

    @Test
    fun testVeryLongErrorMessage() {
        val longMessage = "x".repeat(1000)
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("field", longMessage, ValidatorCode.CUSTOM_ERROR, "value")
            )
        )

        val exception = AtelierClientValidationException(failure)

        assertNotNull(exception.message)
        assertEquals(exception.validationResult.errors[0].message.length, 1000)
    }

    @Test
    fun testSpecialCharactersInErrorDetails() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail(
                    fieldName = "field<>\"'&",
                    message = "Error with special chars: <>&\"'",
                    code = ValidatorCode.CUSTOM_ERROR,
                    actualValue = "<script>alert('xss')</script>"
                )
            )
        )

        val dto = ClientValidationErrorDetail.from(failure.errors[0])

        assertEquals("field<>\"'&", dto.field)
        assertTrue(dto.message.contains("<>&\"'"))
        assertEquals("<script>alert('xss')</script>", dto.value)
    }

    @Test
    fun testMultipleErrorsForSameField() {
        val failure = ValidationResult.Failure(
            errors = listOf(
                ValidationErrorDetail("email", "Required", ValidatorCode.REQUIRED, ""),
                ValidationErrorDetail("email", "Invalid format", ValidatorCode.INVALID_FORMAT, "bad"),
                ValidationErrorDetail("email", "Too long", ValidatorCode.TOO_LONG, "x".repeat(300))
            )
        )

        val exception = AtelierClientValidationException(failure)

        assertTrue(exception.hasErrorFor("email"))
        assertEquals(3, exception.errorsFor("email").size)
    }
}
