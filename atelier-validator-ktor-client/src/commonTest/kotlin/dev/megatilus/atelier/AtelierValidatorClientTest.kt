/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.ktor.client.AtelierClientValidationException
import dev.megatilus.atelier.ktor.client.AtelierValidatorClient
import dev.megatilus.atelier.ktor.client.AtelierValidatorClientConfig
import dev.megatilus.atelier.ktor.client.AtelierValidatorClientConfigKey
import dev.megatilus.atelier.ktor.client.getAndValidate
import dev.megatilus.atelier.ktor.client.getValidator
import dev.megatilus.atelier.ktor.client.getValidatorFromConfig
import dev.megatilus.atelier.validator.results.ValidationResult
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

internal fun mockClient(
    responseBody: String,
    contentType: ContentType = ContentType.Application.Json,
    status: HttpStatusCode = HttpStatusCode.OK,
    configure: HttpClientConfig<MockEngineConfig>.() -> Unit = {}
): HttpClient = HttpClient(MockEngine) {
    engine {
        addHandler {
            respond(
                content = ByteReadChannel(responseBody),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, contentType.toString())
            )
        }
    }
    install(ContentNegotiation) { json() }
    configure()
}

class AtelierValidatorClientPluginTest {

    @Test
    fun `plugin stores config in client attributes`() {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondOk() } }
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }
        assertNotNull(client.attributes.getOrNull(AtelierValidatorClientConfigKey))
        client.close()
    }

    @Test
    fun `getValidator returns validator when registered`() {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondOk() } }
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }
        assertNotNull(client.getValidator<UserDto>())
        client.close()
    }

    @Test
    fun `getValidator returns null when type not registered`() {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondOk() } }
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }
        assertNull(client.getValidator<ProductDto>())
        client.close()
    }

    @Test
    fun `getValidator returns null when plugin not installed`() {
        val client = HttpClient(MockEngine) {
            engine { addHandler { respondOk() } }
            install(ContentNegotiation) { json() }
        }
        assertNull(client.getValidator<UserDto>())
        client.close()
    }
}

class AutomaticValidationClientTest {

    @Test
    fun `automatic validation throws on invalid response body`() = runTest {
        val invalidUser = UserDto("", "bad", 25)
        val client = mockClient(Json.encodeToString(invalidUser)) {
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = true
            }
        }
        assertFailsWith<AtelierClientValidationException> {
            client.getAndValidate<UserDto>("http://test/users/1")
        }
        client.close()
    }

    @Test
    fun `automatic validation passes valid response body`() = runTest {
        val validUser = UserDto("John", "john@example.com", 25)
        val client = mockClient(Json.encodeToString(validUser)) {
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = true
            }
        }

        val result = client.get("http://test/users/1").body<UserDto>()
        assertEquals(validUser, result)
        client.close()
    }

    @Test
    fun `automatic validation skips unregistered types`() = runTest {
        val product = ProductDto("Widget", 9.99)
        val client = mockClient(Json.encodeToString(product)) {
            install(AtelierValidatorClient) {
                register(userValidator) // only user registered
                useAutomaticValidation = true
            }
        }
        // Should not throw — no validator for ProductDto
        val result = client.get("http://test/products/1").body<ProductDto>()

        assertEquals(product, result)
        client.close()
    }

    @Test
    fun `automatic validation exception contains url`() = runTest {
        val invalidUser = UserDto("", "bad", 25)
        val client = mockClient(Json.encodeToString(invalidUser)) {
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = true
            }
        }
        val ex = assertFailsWith<AtelierClientValidationException> {
            client.getAndValidate<UserDto>("http://test/users/1")
        }
        assertNotNull(ex.url)
        client.close()
    }
}

class GetValidatorFromConfigTest {

    @Test
    fun `returns validator for registered type`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val v = getValidatorFromConfig(config, UserDto::class)
        assertNotNull(v)
    }

    @Test
    fun `returns null for unregistered type`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val v = getValidatorFromConfig(config, ProductDto::class)
        assertNull(v)
    }

    @Test
    fun `returned validator produces Success for valid object`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val v = getValidatorFromConfig(config, UserDto::class)!!
        assertTrue(v.validate(UserDto("John", "john@example.com", 25)) is ValidationResult.Success)
    }

    @Test
    fun `returned validator produces Failure for invalid object`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val v = getValidatorFromConfig(config, UserDto::class)!!
        assertTrue(v.validate(UserDto("", "bad", 25)) is ValidationResult.Failure)
    }

    @Test
    fun `returned validator validateFirst returns single error`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val v = getValidatorFromConfig(config, UserDto::class)!!
        val result = v.validateFirst(UserDto("", "bad", 25))
        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errors.size)
    }
}
