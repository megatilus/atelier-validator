/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.ktor.client.AtelierClientValidationException
import dev.megatilus.atelier.ktor.client.ClientValidationErrorDto
import dev.megatilus.atelier.ktor.client.ClientValidationErrorResponse
import dev.megatilus.atelier.validator.results.ErrorDetail
import dev.megatilus.atelier.validator.results.ValidationError
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.test.*

private fun makeDetail(
    field: String = "name",
    message: String = "Required",
    code: String = "REQUIRED",
    value: String? = null
) = ErrorDetail(field, code, message, value)

private fun makeError(
    field: String = "name",
    message: String = "Required",
    code: String = "REQUIRED",
    value: String? = null
) = ValidationError(field, message, ValidationErrorCode(code), value)

private fun makeFailure(vararg errors: ValidationError) =
    ValidationResult.Failure(errors.toList())

class AtelierClientValidationExceptionTest {

    @Test
    fun `message contains error count`() {
        val ex = AtelierClientValidationException(makeFailure(makeError()))
        assertTrue(ex.message!!.contains("1"))
    }

    @Test
    fun `message contains url when provided`() {
        val ex = AtelierClientValidationException(
            makeFailure(makeError()),
            url = "https://api.example.com/users/1"
        )
        assertTrue(ex.message!!.contains("https://api.example.com/users/1"))
    }

    @Test
    fun `message does not contain url when null`() {
        val ex = AtelierClientValidationException(makeFailure(makeError()), url = null)
        assertFalse(ex.message!!.contains("for "))
    }

    @Test
    fun `holds reference to validation result`() {
        val failure = makeFailure(makeError())
        assertSame(failure, AtelierClientValidationException(failure).validationResult)
    }

    @Test
    fun `hasErrorFor returns true when field has error`() {
        val ex = AtelierClientValidationException(makeFailure(makeError(field = "email")))
        assertTrue(ex.hasErrorFor("email"))
    }

    @Test
    fun `hasErrorFor returns false when field has no error`() {
        val ex = AtelierClientValidationException(makeFailure(makeError(field = "email")))
        assertFalse(ex.hasErrorFor("name"))
    }

    @Test
    fun `errorsFor returns errors for field`() {
        val ex = AtelierClientValidationException(
            makeFailure(makeError("email"), makeError("name"))
        )
        assertEquals(1, ex.errorsFor("email").size)
        assertEquals("email", ex.errorsFor("email").first().fieldName)
    }

    @Test
    fun `errorsFor returns empty list for unknown field`() {
        val ex = AtelierClientValidationException(makeFailure(makeError("email")))
        assertTrue(ex.errorsFor("password").isEmpty())
    }
}

class ClientValidationErrorDtoTest {

    @Test
    fun `from maps field correctly`() {
        assertEquals("email", ClientValidationErrorDto.from(makeDetail(field = "email")).field)
    }

    @Test
    fun `from maps message correctly`() {
        assertEquals("Must be valid", ClientValidationErrorDto.from(makeDetail(message = "Must be valid")).message)
    }

    @Test
    fun `from maps code correctly`() {
        assertEquals("INVALID_FORMAT", ClientValidationErrorDto.from(makeDetail(code = "INVALID_FORMAT")).code)
    }

    @Test
    fun `from maps value correctly`() {
        assertEquals("bad@", ClientValidationErrorDto.from(makeDetail(value = "bad@")).value)
    }

    @Test
    fun `from maps null value correctly`() {
        assertNull(ClientValidationErrorDto.from(makeDetail(value = null)).value)
    }

    @Test
    fun `from maps url correctly`() {
        val dto = ClientValidationErrorDto.from(makeDetail(), url = "https://api.example.com")
        assertEquals("https://api.example.com", dto.url)
    }

    @Test
    fun `from url defaults to null`() {
        assertNull(ClientValidationErrorDto.from(makeDetail()).url)
    }
}

class ClientValidationErrorResponseTest {

    @Test
    fun `from exception maps errors correctly`() {
        val ex = AtelierClientValidationException(
            makeFailure(makeError("name"), makeError("email")),
            url = "https://api.example.com/users"
        )
        val response = ClientValidationErrorResponse.from(ex)
        assertEquals(2, response.errors.size)
        assertEquals("https://api.example.com/users", response.url)
    }

    @Test
    fun `from exception message is set`() {
        val ex = AtelierClientValidationException(makeFailure(makeError()))
        val response = ClientValidationErrorResponse.from(ex)
        assertEquals("Response validation failed", response.message)
    }

    @Test
    fun `from exception errors contain url`() {
        val ex = AtelierClientValidationException(
            makeFailure(makeError("name")),
            url = "https://api.example.com"
        )
        val response = ClientValidationErrorResponse.from(ex)
        assertEquals("https://api.example.com", response.errors.first().url)
    }

    @Test
    fun `from failure maps errors correctly`() {
        val failure = makeFailure(makeError("name"), makeError("email"))
        val response = ClientValidationErrorResponse.from(failure, url = "https://api.example.com")
        assertEquals(2, response.errors.size)
        assertEquals("https://api.example.com", response.url)
    }

    @Test
    fun `from failure handles empty error list`() {
        val response = ClientValidationErrorResponse.from(makeFailure())
        assertEquals(0, response.errors.size)
    }

    @Test
    fun `from failure url defaults to null`() {
        val response = ClientValidationErrorResponse.from(makeFailure(makeError()))
        assertNull(response.url)
    }

    @Test
    fun `from failure errors contain url`() {
        val failure = makeFailure(makeError("email"))
        val response = ClientValidationErrorResponse.from(failure, url = "https://api.example.com")
        assertEquals("https://api.example.com", response.errors.first().url)
    }

    @Test
    fun `from failure errors url is null when not provided`() {
        val failure = makeFailure(makeError("email"))
        val response = ClientValidationErrorResponse.from(failure)
        assertNull(response.errors.first().url)
    }
}
