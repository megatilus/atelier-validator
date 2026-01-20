/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.validators.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class AtelierValidatorErrorTest {

    @Serializable
    data class User(
        val name: String,
        val email: String,
        val age: Int
    )

    private val userValidator = atelierValidator<User> {
        User::name {
            notBlank("Name is required")
            minLength(2, "Name must be at least 2 characters")
        }
        User::email {
            email("Invalid email format")
        }
        User::age {
            min(18, "Must be at least 18 years old")
            max(120, "Age must be realistic")
        }
    }

    @Test
    fun `ValidationErrorDetailDto should be created from ValidationErrorDetail`() {
        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        val error = result.errors.first()
        val dto = ValidationErrorDetailDto.from(error)

        assertEquals(error.fieldName, dto.field)
        assertEquals(error.message, dto.message)
        assertEquals(error.code.toString(), dto.code)
        assertEquals(error.actualValue, dto.value)
    }

    @Test
    fun `AtelierValidationErrorResponse should be created from ValidationResult Failure`() {
        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        val response = AtelierValidationErrorResponse.from(result)

        assertEquals("Validation failed", response.message)
        assertEquals(result.errorCount, response.errors.size)

        response.errors.forEach { error ->
            assertNotNull(error.field)
            assertNotNull(error.message)
            assertNotNull(error.code)
            assertNotNull(error.value)
        }
    }

    @Test
    fun `hasErrorFor should detect field errors`() {
        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        assertTrue(result.hasErrorFor("name"))
        assertTrue(result.hasErrorFor("email"))
        assertTrue(result.hasErrorFor("age"))
        assertFalse(result.hasErrorFor("nonexistent"))
    }

    @Test
    fun `respondValidationError should send error response with default status`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondValidationError(result)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errorResponse = body<AtelierValidationErrorResponse>()
            assertEquals("Validation failed", errorResponse.message)
            assertTrue(errorResponse.errors.isNotEmpty())
        }
    }

    @Test
    fun `respondValidationError should send error response with custom status`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondValidationError(result, HttpStatusCode.UnprocessableEntity)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
        }
    }

    @Test
    fun `respondValidationError should use custom errorResponseBuilder from config`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        @Serializable
        data class CustomError(val status: String, val errorCount: Int)

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
            errorResponseBuilder = { failure ->
                CustomError(status = "validation_error", errorCount = failure.errorCount)
            }
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondValidationError(result)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errorResponse = body<CustomError>()
            assertEquals("validation_error", errorResponse.status)
            assertTrue(errorResponse.errorCount > 0)
        }
    }

    @Test
    fun `respondCustomValidationError should send custom error with default status`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        @Serializable
        data class MyCustomError(val errorCode: String, val details: String)

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondCustomValidationError(
                            MyCustomError(
                                errorCode = "VALIDATION_FAILED",
                                details = result.errors.joinToString { it.message }
                            )
                        )
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errorResponse = body<MyCustomError>()
            assertEquals("VALIDATION_FAILED", errorResponse.errorCode)
            assertNotNull(errorResponse.details)
        }
    }

    @Test
    fun `respondCustomValidationError should send custom error with custom status`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        @Serializable
        data class MyCustomError(val errorCode: String)

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondCustomValidationError(
                            MyCustomError(errorCode = "CUSTOM_ERROR"),
                            HttpStatusCode.UnprocessableEntity
                        )
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
            val errorResponse = body<MyCustomError>()
            assertEquals("CUSTOM_ERROR", errorResponse.errorCode)
        }
    }

    @Test
    fun `error response should include all field details`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondValidationError(result)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "not-an-email", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errorResponse = body<AtelierValidationErrorResponse>()

            val fieldNames = errorResponse.errors.map { it.field }
            assertTrue(fieldNames.contains("name"))
            assertTrue(fieldNames.contains("email"))
            assertTrue(fieldNames.contains("age"))

            errorResponse.errors.forEach { error ->
                assertNotNull(error.field)
                assertNotNull(error.message)
                assertNotNull(error.code)
                assertNotNull(error.value)
            }
        }
    }

    @Test
    fun `respondValidationError should use config errorStatusCode when no status provided`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            errorStatusCode = HttpStatusCode.UnprocessableEntity
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        call.respondValidationError(result)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
        }
    }

    @Test
    fun `respondValidationError should not respond if response already committed`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                val validator = call.getValidator<User>()

                when (val result = validator?.validate(user)) {
                    is ValidationResult.Failure -> {
                        // Commit response first
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "Already handled"))
                        // This should not throw or change response
                        call.respondValidationError(result)
                    }

                    else -> {
                        call.respond(HttpStatusCode.Created, user)
                    }
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            // Should keep the first response
            assertEquals(HttpStatusCode.Conflict, status)
        }
    }
}
