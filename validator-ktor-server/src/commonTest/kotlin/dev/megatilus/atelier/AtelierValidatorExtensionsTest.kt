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
import io.ktor.server.request.receive
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
            assertEquals("Validation failed", errorResponse.message)
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
    fun `validateBatch should separate valid and invalid objects`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class BatchRequest(val users: List<User>)

        @Serializable
        data class BatchResponse(
            val validCount: Int,
            val invalidCount: Int,
            val errors: List<String>
        )

        routing {
            post("/users/batch") {
                val request = call.receive<BatchRequest>()
                val (valid, invalid) = call.validateBatch(request.users)

                if (invalid.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.MultiStatus,
                        BatchResponse(
                            validCount = valid.size,
                            invalidCount = invalid.size,
                            errors = invalid.flatMap { (_, failure) ->
                                failure.errors.map { it.message }
                            }
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.OK, BatchResponse(valid.size, 0, emptyList()))
                }
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val users = listOf(
            User(name = "John Doe", email = "john@example.com", age = 25), // Valid
            User(name = "", email = "invalid", age = 15), // Invalid
            User(name = "Jane Smith", email = "jane@example.com", age = 30) // Valid
        )

        client.post("/users/batch") {
            contentType(ContentType.Application.Json)
            setBody(BatchRequest(users))
        }.apply {
            assertEquals(HttpStatusCode.MultiStatus, status)
            val response = body<BatchResponse>()
            assertEquals(2, response.validCount)
            assertEquals(1, response.invalidCount)
            assertTrue(response.errors.isNotEmpty())
        }
    }

    @Test
    fun `validateBatch should return all valid when all objects are valid`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class BatchRequest(val users: List<User>)

        @Serializable
        data class BatchResponse(val validCount: Int, val invalidCount: Int)

        routing {
            post("/users/batch") {
                val request = call.receive<BatchRequest>()
                val (valid, invalid) = call.validateBatch(request.users)

                call.respond(
                    HttpStatusCode.OK,
                    BatchResponse(validCount = valid.size, invalidCount = invalid.size)
                )
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val users = listOf(
            User(name = "John Doe", email = "john@example.com", age = 25),
            User(name = "Jane Smith", email = "jane@example.com", age = 30)
        )

        client.post("/users/batch") {
            contentType(ContentType.Application.Json)
            setBody(BatchRequest(users))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<BatchResponse>()
            assertEquals(2, response.validCount)
            assertEquals(0, response.invalidCount)
        }
    }

    @Test
    fun `validateBatch should return all invalid when all objects are invalid`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(userValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class BatchRequest(val users: List<User>)

        @Serializable
        data class BatchResponse(val validCount: Int, val invalidCount: Int)

        routing {
            post("/users/batch") {
                val request = call.receive<BatchRequest>()
                val (valid, invalid) = call.validateBatch(request.users)

                call.respond(
                    HttpStatusCode.OK,
                    BatchResponse(validCount = valid.size, invalidCount = invalid.size)
                )
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val users = listOf(
            User(name = "", email = "invalid1", age = 15),
            User(name = "", email = "invalid2", age = 10)
        )

        client.post("/users/batch") {
            contentType(ContentType.Application.Json)
            setBody(BatchRequest(users))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<BatchResponse>()
            assertEquals(0, response.validCount)
            assertEquals(2, response.invalidCount)
        }
    }

    @Test
    fun `validateBatch should return empty lists when no validator registered`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            // No validator registered
        }

        @Serializable
        data class BatchRequest(val users: List<User>)

        @Serializable
        data class BatchResponse(val validCount: Int, val invalidCount: Int)

        routing {
            post("/users/batch") {
                val request = call.receive<BatchRequest>()
                val (valid, invalid) = call.validateBatch(request.users)

                call.respond(
                    HttpStatusCode.OK,
                    BatchResponse(validCount = valid.size, invalidCount = invalid.size)
                )
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val users = listOf(
            User(name = "John", email = "john@example.com", age = 25)
        )

        client.post("/users/batch") {
            contentType(ContentType.Application.Json)
            setBody(BatchRequest(users))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<BatchResponse>()
            assertEquals(0, response.validCount)
            assertEquals(0, response.invalidCount)
        }
    }

    @Test
    fun `validateBatch should work with different types`() = testApplication {
        install(ServerContentNegotiation) {
            json()
        }

        install(AtelierValidatorPlugin) {
            register(productValidator)
            useAutomaticValidation = false
        }

        @Serializable
        data class BatchRequest(val products: List<Product>)

        @Serializable
        data class BatchResponse(val validCount: Int, val invalidCount: Int)

        routing {
            post("/products/batch") {
                val request = call.receive<BatchRequest>()
                val (valid, invalid) = call.validateBatch(request.products)

                call.respond(
                    HttpStatusCode.OK,
                    BatchResponse(validCount = valid.size, invalidCount = invalid.size)
                )
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }

        val products = listOf(
            Product(name = "Product 1", price = 10.0), // Valid
            Product(name = "", price = -5.0), // Invalid
            Product(name = "Product 3", price = 20.0) // Valid
        )

        client.post("/products/batch") {
            contentType(ContentType.Application.Json)
            setBody(BatchRequest(products))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<BatchResponse>()
            assertEquals(2, response.validCount)
            assertEquals(1, response.invalidCount)
        }
    }
}
