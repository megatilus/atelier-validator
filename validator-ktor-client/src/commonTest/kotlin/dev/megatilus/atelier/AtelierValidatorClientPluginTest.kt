/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validators.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*

class AtelierValidatorClientPluginTest {

    @Serializable
    data class User(
        val id: Int,
        val name: String,
        val email: String
    )

    @Serializable
    data class Product(
        val id: String,
        val name: String,
        val price: Double
    )

    private val userValidator = atelierValidator<User> {
        User::id { min(1) }
        User::name {
            notBlank()
            minLength(2)
        }
        User::email { email() }
    }

    private val productValidator = atelierValidator<Product> {
        Product::id { notBlank() }
        Product::name {
            notBlank()
            minLength(2)
        }
        Product::price { positive() }
    }

    // ==================== Installation Tests ====================

    @Test
    fun `should install plugin successfully`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        assertNotNull(client.pluginOrNull(AtelierValidatorClient))
        client.close()
    }

    @Test
    fun `should register validator correctly`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        val config = client.attributes[AtelierValidatorClientConfigKey]

        assertTrue(config.validators.containsKey(User::class))
        assertNotNull(config.validators[User::class])

        client.close()
    }

    @Test
    fun `should allow enabling automatic validation`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = true
            }
        }

        val config = client.attributes[AtelierValidatorClientConfigKey]

        assertTrue(config.useAutomaticValidation)

        client.close()
    }

    @Test
    fun `should store config in client attributes`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        val config = client.attributes[AtelierValidatorClientConfigKey]

        assertNotNull(config)
        assertEquals(1, config.validators.size)

        client.close()
    }

    @Test
    fun `should propagate config to request attributes`() = runTest {
        var requestConfig: AtelierValidatorClientConfig? = null

        val client = HttpClient(
            MockEngine { request ->
                requestConfig = request.attributes.getOrNull(AtelierValidatorClientConfigKey)
                respond("", HttpStatusCode.OK)
            }
        ) {
            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        client.get("https://example.com/test")

        assertNotNull(requestConfig)
        assertEquals(1, requestConfig?.validators?.size)

        client.close()
    }

    @Test
    fun `should work with ContentNegotiation`() = runTest {
        val client = HttpClient(
            MockEngine {
                respond(
                    """{"id":1,"name":"John Doe","email":"john@example.com"}""",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        val user = client.get("https://example.com/users/1").bodyAndValidate<User>()

        assertEquals(1, user.id)
        assertEquals("John Doe", user.name)

        client.close()
    }

    @Test
    fun `should allow multiple validators`() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            install(AtelierValidatorClient) {
                register(userValidator)
                register(productValidator)
            }
        }

        val config = client.attributes[AtelierValidatorClientConfigKey]

        assertEquals(2, config.validators.size)
        assertTrue(config.validators.containsKey(User::class))
        assertTrue(config.validators.containsKey(Product::class))

        client.close()
    }

    @Test
    fun `should validate response body in manual mode`() = runTest {
        val client = HttpClient(
            MockEngine {
                respond(
                    """{"id":1,"name":"John Doe","email":"john@example.com"}""",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }

        val user = client.get("https://example.com/users/1").bodyAndValidate<User>()

        assertEquals(1, user.id)

        client.close()
    }

    @Test
    fun `should handle validation errors in manual mode`() = runTest {
        val client = HttpClient(
            MockEngine {
                respond(
                    """{"id":0,"name":"","email":"invalid"}""",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        assertFailsWith<AtelierClientValidationException> {
            client.get("https://example.com/users/1").bodyAndValidate<User>()
        }

        client.close()
    }

    @Test
    fun `should provide meaningful error messages`() = runTest {
        val client = HttpClient(
            MockEngine {
                respond(
                    """{"id":0,"name":"A","email":"not-an-email"}""",
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(AtelierValidatorClient) {
                register(userValidator)
            }
        }

        val exception = assertFailsWith<AtelierClientValidationException> {
            client.get("https://example.com/users/1").bodyAndValidate<User>()
        }

        assertTrue(exception.validationResult.errorCount > 0)
        assertEquals(exception.message?.contains("validation failed", ignoreCase = true), true)

        client.close()
    }
}
