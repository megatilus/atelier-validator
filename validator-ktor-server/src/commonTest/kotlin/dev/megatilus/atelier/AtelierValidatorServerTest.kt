/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.*

class AtelierValidatorServerTest {

    @Test
    fun `plugin stores config in application attributes`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)

            useAutomaticValidation = false
        }
        routing {
            get("/check") {
                val config = call.attributes.getOrNull(AtelierValidatorServerConfigKey)
                call.respond(if (config != null) HttpStatusCode.OK else HttpStatusCode.NotFound)
            }
        }
        assertEquals(HttpStatusCode.OK, client.get("/check").status)
    }

    @Test
    fun `plugin stores config in call attributes`() = testApplication {
        install(ContentNegotiation) { json() }

        install(AtelierValidatorServer) {
            register(userValidator)

            useAutomaticValidation = false
        }
        routing {
            get("/check") {
                val config = call.attributes.getOrNull(AtelierValidatorServerConfigKey)
                call.respond(if (config != null) HttpStatusCode.OK else HttpStatusCode.NotFound)
            }
        }
        assertEquals(HttpStatusCode.OK, client.get("/check").status)
    }

    @Test
    fun `plugin throws IllegalStateException when no validators at startup`() {
        val config = AtelierValidatorServerConfig().apply {
            validateAtStartup = true
        }
        assertFailsWith<IllegalStateException> {
            config.validateConfiguration()
        }
    }

    @Test
    fun `plugin skips startup check when validateAtStartup false`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            validateAtStartup = false
            useAutomaticValidation = false
        }
        routing { get("/") { call.respond(HttpStatusCode.OK) } }
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }
}

class AutomaticValidationTest {

    private fun ApplicationTestBuilder.setup() {
        install(ContentNegotiation) { json() }
        val config = AtelierValidatorServerConfig().apply {
            register(userValidator)
            useAutomaticValidation = true
        }
        install(StatusPages) { configureValidationExceptionHandlers(config) }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = true
        }
    }

    @Test
    fun `returns 400 on invalid body`() = testApplication {
        setup()
        routing {
            post("/users") {
                val user = call.receive<UserDto>()
                call.respond(HttpStatusCode.Created, user)
            }
        }
        val r = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertEquals(HttpStatusCode.BadRequest, r.status)
    }

    @Test
    fun `passes valid body to handler`() = testApplication {
        setup()
        routing {
            post("/users") {
                val user = call.receive<UserDto>()
                call.respond(HttpStatusCode.Created, user)
            }
        }
        val r = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","email":"john@example.com","age":25}""")
        }
        assertEquals(HttpStatusCode.Created, r.status)
    }

    @Test
    fun `auto-installs StatusPages when not present`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = true
        }
        routing {
            post("/users") {
                val user = call.receive<UserDto>()
                call.respond(HttpStatusCode.Created, user)
            }
        }
        val r = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertEquals(HttpStatusCode.BadRequest, r.status)
    }
}

class ConfigureValidationExceptionHandlersTest {

    @Test
    fun `handles AtelierValidationException with 400`() = testApplication {
        install(ContentNegotiation) { json() }
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        install(StatusPages) { configureValidationExceptionHandlers(config) }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/throw") {
                val failure = ValidationResult.Failure(
                    listOf(
                        dev.megatilus.atelier.results.ValidationError(
                            "name",
                            "Required",
                            dev.megatilus.atelier.results.ValidationErrorCode("REQUIRED"),
                            null
                        )
                    )
                )
                throw AtelierValidationException(failure)
            }
        }
        assertEquals(HttpStatusCode.BadRequest, client.get("/throw").status)
    }

    @Test
    fun `uses custom errorResponseBuilder for AtelierValidationException`() = testApplication {
        install(ContentNegotiation) { json() }
        val config = AtelierValidatorServerConfig().apply {
            register(userValidator)
            errorResponseBuilder = { _ -> mapOf("custom" to "error") }
        }
        install(StatusPages) { configureValidationExceptionHandlers(config) }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/throw") {
                val failure = ValidationResult.Failure(emptyList())
                throw AtelierValidationException(failure)
            }
        }
        val body = client.get("/throw").bodyAsText()
        assertTrue(body.contains("custom"))
    }
}

class AtelierValidationExceptionTest {

    @Test
    fun `message contains error count`() {
        val failure = ValidationResult.Failure(
            listOf(
                dev.megatilus.atelier.results.ValidationError(
                    "name",
                    "Required",
                    dev.megatilus.atelier.results.ValidationErrorCode("REQUIRED"),
                    null
                )
            )
        )
        val ex = AtelierValidationException(failure)
        assertTrue(ex.message!!.contains("1"))
    }

    @Test
    fun `message contains field name`() {
        val failure = ValidationResult.Failure(
            listOf(
                dev.megatilus.atelier.results.ValidationError(
                    "email",
                    "Invalid",
                    dev.megatilus.atelier.results.ValidationErrorCode("INVALID_FORMAT"),
                    null
                )
            )
        )
        val ex = AtelierValidationException(failure)
        assertTrue(ex.message!!.contains("email"))
    }

    @Test
    fun `holds reference to validation result`() {
        val failure = ValidationResult.Failure(emptyList())
        assertSame(failure, AtelierValidationException(failure).validationResult)
    }
}

class GetValidatorTest {

    @Serializable
    data class OtherDto(val value: String)

    @Test
    fun `returns validator when registered`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/check") {
                val v = call.getValidator<UserDto>()
                call.respond(if (v != null) HttpStatusCode.OK else HttpStatusCode.NotFound)
            }
        }
        assertEquals(HttpStatusCode.OK, client.get("/check").status)
    }

    @Test
    fun `returns null when type not registered`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/check") {
                val v = call.getValidator<OtherDto>()
                call.respond(if (v != null) HttpStatusCode.OK else HttpStatusCode.NotFound)
            }
        }
        assertEquals(HttpStatusCode.NotFound, client.get("/check").status)
    }

    @Test
    fun `returned validator produces correct Success result`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/validate") {
                val v = call.getValidator<UserDto>()!!
                val result = v.validate(UserDto("John", "john@example.com", 25))
                call.respond(if (result is ValidationResult.Success) HttpStatusCode.OK else HttpStatusCode.BadRequest)
            }
        }
        assertEquals(HttpStatusCode.OK, client.get("/validate").status)
    }

    @Test
    fun `returned validator produces correct Failure result`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/validate") {
                val v = call.getValidator<UserDto>()!!
                val result = v.validate(UserDto("", "bad", 25))
                call.respond(if (result is ValidationResult.Failure) HttpStatusCode.BadRequest else HttpStatusCode.OK)
            }
        }
        assertEquals(HttpStatusCode.BadRequest, client.get("/validate").status)
    }

    @Test
    fun `returned validator validateFirst works`() = testApplication {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
        routing {
            get("/validate-first") {
                val v = call.getValidator<UserDto>()!!
                val result = v.validateFirst(UserDto("", "bad", 25))
                call.respond(if (result is ValidationResult.Failure) HttpStatusCode.BadRequest else HttpStatusCode.OK)
            }
        }
        assertEquals(HttpStatusCode.BadRequest, client.get("/validate-first").status)
    }
}
