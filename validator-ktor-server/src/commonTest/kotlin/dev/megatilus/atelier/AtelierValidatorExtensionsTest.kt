/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validators.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class AtelierValidatorExtensionsTest {

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
    fun `receiveAndValidate should return null on validation failure`() = testApplication {
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

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "", email = "invalid", age = 15))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errorResponse = body<AtelierValidationErrorResponse>()
            assertEquals("Request validation failed: 4 error(s) detected", errorResponse.message)
            assertTrue(errorResponse.errors.isNotEmpty())
        }
    }

    @Test
    fun `receiveAndValidate should return object on validation success`() = testApplication {
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
    fun `receiveAndValidate should return null and respond 500 if no validator registered`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            // No validator registered for User
            register(productValidator) // Register a different validator to pass startup validation
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
            setBody(User(name = "John", email = "john@example.com", age = 25))
        }.apply {
            assertEquals(HttpStatusCode.InternalServerError, status)
        }
    }

    @Test
    fun `receiveAndValidate with custom error handler should use custom handler`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class CustomError(val customMessage: String, val errorCount: Int)

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User> { failure ->
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        CustomError("Custom validation error", failure.errorCount)
                    )
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
            val errorResponse = body<CustomError>()
            assertEquals("Custom validation error", errorResponse.customMessage)
            assertTrue(errorResponse.errorCount > 0)
        }
    }

    @Test
    fun `receiveAndValidate with custom handler should return object on success`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class CustomError(val message: String)

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User> { _ ->
                    call.respond(HttpStatusCode.BadRequest, CustomError("Error"))
                } ?: return@post
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
    fun `receiveAndValidate with custom handler that throws exception`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                try {
                    val user = call.receiveAndValidate<User> { failure ->
                        throw AtelierValidationException(failure)
                    } ?: return@post
                    call.respond(HttpStatusCode.Created, user)
                } catch (e: AtelierValidationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
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
        }
    }

    @Test
    fun `receiveAndValidate should work with different types`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            register(productValidator)
            useAutomaticValidation = false
        }

        routing {
            post("/users") {
                val user = call.receiveAndValidate<User>() ?: return@post
                call.respond(HttpStatusCode.Created, user)
            }

            post("/products") {
                val product = call.receiveAndValidate<Product>() ?: return@post
                call.respond(HttpStatusCode.Created, product)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        // Test User endpoint
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(User(name = "John Doe", email = "john@example.com", age = 25))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }

        // Test Product endpoint
        client.post("/products") {
            contentType(ContentType.Application.Json)
            setBody(Product(name = "Test Product", price = 29.99))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }

        // Test invalid Product
        client.post("/products") {
            contentType(ContentType.Application.Json)
            setBody(Product(name = "", price = -10.0))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun `receiveAndValidate should use custom errorResponseBuilder from config`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        @Serializable
        data class CustomGlobalError(val errorType: String, val count: Int)

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
            errorResponseBuilder = { failure ->
                CustomGlobalError(errorType = "GLOBAL_VALIDATION_ERROR", count = failure.errorCount)
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
            val errorResponse = body<CustomGlobalError>()
            assertEquals("GLOBAL_VALIDATION_ERROR", errorResponse.errorType)
            assertTrue(errorResponse.count > 0)
        }
    }
}
