/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.ktor.server.AtelierValidationErrorResponse
import dev.megatilus.atelier.ktor.server.AtelierValidatorServer
import dev.megatilus.atelier.ktor.server.ValidationErrorDto
import dev.megatilus.atelier.ktor.server.hasErrorFor
import dev.megatilus.atelier.ktor.server.respondValidationError
import dev.megatilus.atelier.validator.results.ErrorDetail
import dev.megatilus.atelier.validator.results.ValidationError
import dev.megatilus.atelier.validator.results.ValidationErrorCode
import dev.megatilus.atelier.validator.results.ValidationResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
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

class ValidationErrorDtoTest {

    @Test
    fun `from maps field name correctly`() {
        val dto = ValidationErrorDto.from(makeDetail(field = "email"))
        assertEquals("email", dto.field)
    }

    @Test
    fun `from maps message correctly`() {
        val dto = ValidationErrorDto.from(makeDetail(message = "Must be valid"))
        assertEquals("Must be valid", dto.message)
    }

    @Test
    fun `from maps code correctly`() {
        val dto = ValidationErrorDto.from(makeDetail(code = "INVALID_FORMAT"))
        assertEquals("INVALID_FORMAT", dto.code)
    }

    @Test
    fun `from maps actualValue correctly`() {
        val dto = ValidationErrorDto.from(makeDetail(value = "bad@email"))
        assertEquals("bad@email", dto.value)
    }

    @Test
    fun `from maps null actualValue correctly`() {
        val dto = ValidationErrorDto.from(makeDetail(value = null))
        assertNull(dto.value)
    }
}

class AtelierValidationErrorResponseTest {

    @Test
    fun `from message contains error count`() {
        val failure = makeFailure(makeError("name"), makeError("email"))
        val response = AtelierValidationErrorResponse.from(failure)
        assertTrue(response.message.contains("2"))
    }

    @Test
    fun `from errors list has correct size`() {
        val failure = makeFailure(makeError("name"), makeError("email"))
        val response = AtelierValidationErrorResponse.from(failure)
        assertEquals(2, response.errors.size)
    }

    @Test
    fun `from handles empty error list`() {
        val response = AtelierValidationErrorResponse.from(makeFailure())
        assertEquals(0, response.errors.size)
        assertTrue(response.message.contains("0"))
    }

    @Test
    fun `from maps each error to dto via toDetailedList`() {
        val failure = makeFailure(makeError(field = "age", code = "OUT_OF_RANGE"))
        val response = AtelierValidationErrorResponse.from(failure)
        assertEquals("age", response.errors.first().field)
        assertEquals("OUT_OF_RANGE", response.errors.first().code)
    }
}

class HasErrorForTest {

    @Test
    fun `returns true when field has error`() {
        assertTrue(makeFailure(makeError(field = "email")).hasErrorFor("email"))
    }

    @Test
    fun `returns false when field has no error`() {
        assertFalse(makeFailure(makeError(field = "email")).hasErrorFor("name"))
    }

    @Test
    fun `returns false on empty failure`() {
        assertFalse(makeFailure().hasErrorFor("name"))
    }

    @Test
    fun `returns true for correct field among multiple errors`() {
        val failure = makeFailure(makeError("name"), makeError("email"), makeError("age"))
        assertTrue(failure.hasErrorFor("age"))
        assertFalse(failure.hasErrorFor("password"))
    }
}

class RespondValidationErrorTest {

    private fun ApplicationTestBuilder.setup(
        errorBuilder: ((ValidationResult.Failure) -> Any)? = null
    ) {
        install(ContentNegotiation) { json() }

        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
            if (errorBuilder != null) errorResponseBuilder = errorBuilder
        }
    }

    @Test
    fun `sends 400 by default`() = testApplication {
        setup()
        routing {
            get("/error") { call.respondValidationError(makeFailure(makeError())) }
        }
        assertEquals(HttpStatusCode.BadRequest, client.get("/error").status)
    }

    @Test
    fun `response body contains field name`() = testApplication {
        setup()
        routing {
            get("/error") {
                call.respondValidationError(makeFailure(makeError("email", "Invalid email")))
            }
        }
        assertTrue(client.get("/error").bodyAsText().contains("email"))
    }

    @Test
    fun `respects custom status code`() = testApplication {
        setup()
        routing {
            get("/error") {
                call.respondValidationError(makeFailure(makeError()), HttpStatusCode.UnprocessableEntity)
            }
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, client.get("/error").status)
    }

    @Test
    fun `uses custom errorResponseBuilder when configured`() = testApplication {
        setup(errorBuilder = { _ -> mapOf("custom" to true) })
        routing {
            get("/error") { call.respondValidationError(makeFailure(makeError())) }
        }
        assertTrue(client.get("/error").bodyAsText().contains("custom"))
    }

    @Test
    fun `does not respond twice when response already committed`() = testApplication {
        setup()
        routing {
            get("/double") {
                val failure = makeFailure(makeError())
                call.respondValidationError(failure)
                call.respondValidationError(failure)
            }
        }
        assertEquals(HttpStatusCode.BadRequest, client.get("/double").status)
    }

    @Test
    fun `works without plugin installed - fallback to default format`() = testApplication {
        install(ContentNegotiation) { json() }
        routing {
            get("/error") { call.respondValidationError(makeFailure(makeError("name"))) }
        }
        val r = client.get("/error")
        assertEquals(HttpStatusCode.BadRequest, r.status)
        assertTrue(r.bodyAsText().contains("name"))
    }
}
