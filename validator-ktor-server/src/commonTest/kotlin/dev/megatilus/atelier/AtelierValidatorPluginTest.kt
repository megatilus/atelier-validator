/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.validators.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class AtelierValidatorPluginTest {

    @Serializable
    data class User(
        val name: String,
        val email: String,
        val age: Int
    )

    @Serializable
    data class Product(
        val name: String,
        val price: Double
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
        }
    }

    private val productValidator = atelierValidator<Product> {
        Product::name {
            notBlank("Product name is required")
        }
        Product::price {
            min(0.0, "Price must be positive")
        }
    }

    @Test
    fun `plugin should install successfully with manual validation`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            get("/test") {
                call.respond(HttpStatusCode.OK, "Plugin installed")
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Plugin installed", bodyAsText())
        }
    }

    @Test
    fun `plugin should install successfully with automatic validation`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = true
        }

        routing {
            get("/test") {
                call.respond(HttpStatusCode.OK, "Plugin installed")
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Plugin installed", bodyAsText())
        }
    }

    @Test
    fun `plugin should install with multiple validators`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            register(productValidator)
        }

        routing {
            get("/test") {
                call.respond(HttpStatusCode.OK, "OK")
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `validateConfiguration should throw when no validators and validateAtStartup is true`() {
        val config = AtelierValidatorConfig()
        config.validateAtStartup = true
        // No validators registered

        val exception = assertFailsWith<IllegalStateException> {
            config.validateConfiguration()
        }

        assertTrue(exception.message!!.contains("No validators registered"))
    }

    @Test
    fun `plugin should not fail at startup when validateAtStartup is false`() = testApplication {
        install(AtelierValidatorPlugin) {
            validateAtStartup = false
            // No validators registered
        }

        routing {
            get("/test") {
                call.respond(HttpStatusCode.OK, "OK")
            }
        }

        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun `automatic validation should reject invalid request`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = true
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                call.respond(HttpStatusCode.Created, user)
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
        }
    }

    @Test
    fun `automatic validation should accept valid request`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = true
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                call.respond(HttpStatusCode.Created, user)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val validUser = User(name = "John Doe", email = "john@example.com", age = 25)

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(validUser)
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<User>()
            assertEquals(validUser, response)
        }
    }

    @Test
    fun `automatic validation disabled should not reject invalid request`() = testApplication {
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
                call.respond(HttpStatusCode.Created, user)
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
            // Should pass through without automatic validation
            assertEquals(HttpStatusCode.Created, status)
        }
    }

    @Test
    fun `automatic validation with pre-installed StatusPages should use existing StatusPages`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        val validatorConfig = AtelierValidatorConfig().apply {
            register(userValidator)
            useAutomaticValidation = true
            errorStatusCode = HttpStatusCode.UnprocessableEntity
        }

        // Install StatusPages BEFORE AtelierValidator
        install(StatusPages) {
            configureValidationExceptionHandlers(validatorConfig)
        }

        install(AtelierValidatorPlugin) {
            validators.putAll(validatorConfig.validators)
            errorStatusCode = validatorConfig.errorStatusCode
            useAutomaticValidation = true
        }

        routing {
            post("/users") {
                val user = call.receive<User>()
                call.respond(HttpStatusCode.Created, user)
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
            // Should use custom status code from config
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
        }
    }

    @Test
    fun `getValidator should return validator for registered type`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            get("/validator-check") {
                val validator = call.getValidator<User>()
                if (validator != null) {
                    call.respond(HttpStatusCode.OK, "Validator found")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Validator not found")
                }
            }
        }

        client.get("/validator-check").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Validator found", bodyAsText())
        }
    }

    @Test
    fun `getValidator should return null for unregistered type`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            get("/validator-check") {
                val validator = call.getValidator<Product>()
                if (validator != null) {
                    call.respond(HttpStatusCode.OK, "Validator found")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Validator not found")
                }
            }
        }

        client.get("/validator-check").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun `AtelierValidationException should include error count in message`() {
        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        val exception = AtelierValidationException(result)

        assertTrue(exception.message!!.contains(result.errorCount.toString()))
        assertTrue(exception.message!!.contains("Validation failed"))
    }

    @Test
    fun `plugin should work with custom error status code in manual mode`() = testApplication {
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
                val user = call.receiveAndValidate<User>() ?: return@post
                call.respond(HttpStatusCode.Created, user)
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
    fun `plugin should work with custom error response builder in manual mode`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        @Serializable
        data class CustomError(val status: String, val count: Int)

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
            errorResponseBuilder = { failure ->
                CustomError(status = "error", count = failure.errorCount)
            }
        }

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User>() ?: return@post
                call.respond(HttpStatusCode.Created, user)
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
            assertEquals("error", errorResponse.status)
            assertTrue(errorResponse.count > 0)
        }
    }

    @Test
    fun `config should be accessible from application attributes`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            errorStatusCode = HttpStatusCode.UnprocessableEntity
        }

        @Serializable
        data class ConfigCheckResponse(val hasConfig: Boolean, val statusCode: Int)

        routing {
            get("/config-check") {
                val config = application.attributes.getOrNull(AtelierValidatorConfigKey)
                if (config != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ConfigCheckResponse(
                            hasConfig = true,
                            statusCode = config.errorStatusCode.value
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ConfigCheckResponse(hasConfig = false, statusCode = 0)
                    )
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        client.get("/config-check").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<ConfigCheckResponse>()
            assertTrue(response.hasConfig)
            assertEquals(422, response.statusCode)
        }
    }

    @Test
    fun `config should be accessible from call attributes`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            errorStatusCode = HttpStatusCode.UnprocessableEntity
        }

        routing {
            get("/config-check") {
                val config = call.attributes.getOrNull(AtelierValidatorConfigKey)
                if (config != null) {
                    call.respond(HttpStatusCode.OK, "Config accessible")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Config not found")
                }
            }
        }

        client.get("/config-check").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Config accessible", bodyAsText())
        }
    }

    @Test
    fun `configureValidationExceptionHandlers should handle AtelierValidationException`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        val validatorConfig = AtelierValidatorConfig().apply {
            register(userValidator)
            errorStatusCode = HttpStatusCode.UnprocessableEntity
            useAutomaticValidation = false
        }

        install(StatusPages) {
            configureValidationExceptionHandlers(validatorConfig)
        }

        install(AtelierValidatorPlugin) {
            validators.putAll(validatorConfig.validators)
            errorStatusCode = validatorConfig.errorStatusCode
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User> { failure ->
                    throw AtelierValidationException(failure)
                } ?: return@post
                call.respond(HttpStatusCode.Created, user)
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
    fun `validator should work with validateFirst for early termination`() = testApplication {
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

                when (val result = validator?.validateFirst(user)) {
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
            // validateFirst should return only 1 error
            assertEquals(1, errorResponse.errors.size)
        }
    }

    @Test
    fun `manual validation mode should not auto-install StatusPages`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User>() ?: return@post
                call.respond(HttpStatusCode.Created, user)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        // Should work fine without StatusPages in manual mode
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}
