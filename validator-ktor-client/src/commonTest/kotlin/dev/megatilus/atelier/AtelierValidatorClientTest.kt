/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import dev.megatilus.atelier.validators.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*

class AtelierValidatorClientTest {

    @Serializable
    data class User(
        val id: String,
        val name: String,
        val email: String,
        val age: Int
    )

    @Serializable
    data class Product(
        val id: String,
        val name: String,
        val price: Double
    )

    private val userValidator = atelierValidator<User> {
        User::id { notBlank() }
        User::name {
            notBlank()
            minLength(2)
            maxLength(50)
        }
        User::email {
            notBlank()
            email()
        }
        User::age {
            min(0)
            max(150)
        }
    }

    private val productValidator = atelierValidator<Product> {
        Product::id { notBlank() }
        Product::name {
            notBlank()
            minLength(1)
        }
        Product::price { positive() }
    }

    private fun createMockClient(
        statusCode: HttpStatusCode = HttpStatusCode.OK,
        responseBody: String,
        automaticValidation: Boolean = false
    ): HttpClient {
        return HttpClient(
            MockEngine { _ ->
                respond(
                    content = responseBody,
                    status = statusCode,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }

            install(AtelierValidatorClient) {
                register(userValidator)
                register(productValidator)
                useAutomaticValidation = automaticValidation
            }
        }
    }

    @Test
    fun testPluginInstallation() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John","email":"john@example.com","age":25}"""
        )

        val config = client.attributes[AtelierValidatorClientConfigKey]
        assertNotNull(config)
        assertTrue(config.validators.isNotEmpty())
    }

    @Test
    fun testValidatorRegistration() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John","email":"john@example.com","age":25}"""
        )

        val config = client.attributes[AtelierValidatorClientConfigKey]
        assertNotNull(config.validators[User::class])
        assertNotNull(config.validators[Product::class])
    }

    @Test
    fun testManualValidationSuccess() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John","email":"john@example.com","age":25}"""
        )

        val user = client.get("/users/1").bodyAndValidate<User>()

        assertEquals("1", user.id)
        assertEquals("John", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals(25, user.age)
    }

    @Test
    fun testManualValidationFailure() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"","name":"","email":"invalid","age":-10}"""
        )

        val exception = assertFailsWith<AtelierClientValidationException> {
            client.get("/users/1").bodyAndValidate<User>()
        }

        assertTrue(exception.validationResult.errorCount > 0)
        assertTrue(exception.validationResult.errorsFor("id").isNotEmpty())
        assertTrue(exception.validationResult.errorsFor("name").isNotEmpty())
        assertTrue(exception.validationResult.errorsFor("email").isNotEmpty())
        assertTrue(exception.validationResult.errorsFor("age").isNotEmpty())
    }

    @Test
    fun testManualValidationWithCustomErrorHandler() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"","name":"","email":"invalid","age":-10}"""
        )

        var errorHandlerCalled = false
        var capturedFailure: ValidationResult.Failure? = null

        val user = client.get("/users/1").bodyAndValidate<User> { failure ->
            errorHandlerCalled = true
            capturedFailure = failure
        }

        assertNull(user)
        assertTrue(errorHandlerCalled)
        assertNotNull(capturedFailure)
        assertTrue(capturedFailure.errorCount > 0)
    }

    @Test
    fun testBodyAndValidateOrNull() = runTest {
        val clientValid = createMockClient(
            responseBody = """{"id":"1","name":"John","email":"john@example.com","age":25}"""
        )

        val clientInvalid = createMockClient(
            responseBody = """{"id":"","name":"","email":"invalid","age":-10}"""
        )

        val validUser = clientValid.get("/users/1").bodyAndValidateOrNull<User>()
        assertNotNull(validUser)

        val invalidUser = clientInvalid.get("/users/1").bodyAndValidateOrNull<User>()
        assertNull(invalidUser)
    }

    @Test
    fun testGetAndValidate() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John","email":"john@example.com","age":25}"""
        )

        val user = client.getAndValidate<User>("/users/1")

        assertEquals("1", user.id)
        assertEquals("John", user.name)
    }

    @Test
    fun testPostAndValidate() = runTest {
        val client = createMockClient(
            statusCode = HttpStatusCode.Created,
            responseBody = """{"id":"2","name":"Jane","email":"jane@example.com","age":30}"""
        )

        val user = client.postAndValidate<User>("/users") {
            setBody("""{"name":"Jane","email":"jane@example.com","age":30}""")
        }

        assertEquals("2", user.id)
        assertEquals("Jane", user.name)
    }

    @Test
    fun testPutAndValidate() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John Updated","email":"john@example.com","age":26}"""
        )

        val user = client.putAndValidate<User>("/users/1") {
            setBody("""{"name":"John Updated","age":26}""")
        }

        assertEquals("John Updated", user.name)
        assertEquals(26, user.age)
    }

    @Test
    fun testPatchAndValidate() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John Patched","email":"john@example.com","age":25}"""
        )

        val user = client.patchAndValidate<User>("/users/1") {
            setBody("""{"name":"John Patched"}""")
        }

        assertEquals("John Patched", user.name)
    }

    // TODO("A FAIRE COMME LES AUTRES")
    @Test
    fun testDeleteAndValidate() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"John Patched","email":"john@example.com","age":25}"""
        )

        val user = client.deleteAndValidate<User>("/users/1") {
            setBody("""{"name":"John Delete"}""")
        }

        assertEquals("John Patched", user.name)
    }

    @Test
    fun testMultipleValidators() = runTest {
        val client = createMockClient(
            responseBody = """{"id":"1","name":"Product","price":19.99}"""
        )

        val product = client.getAndValidate<Product>("/products/1")

        assertEquals("1", product.id)
        assertEquals("Product", product.name)
        assertEquals(19.99, product.price)
    }
}
