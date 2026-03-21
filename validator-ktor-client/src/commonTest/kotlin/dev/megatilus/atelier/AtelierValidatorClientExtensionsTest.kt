/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

private val validUser = UserDto("John", "john@example.com", 25)
private val invalidUser = UserDto("", "bad", 25)
private val validProduct = ProductDto("Widget", 9.99)

private fun clientWith(
    responseBody: String,
    useAutomatic: Boolean = false,
    extra: HttpClientConfig<MockEngineConfig>.() -> Unit = {}
): HttpClient = HttpClient(MockEngine) {
    engine {
        addHandler {
            respond(
                content = ByteReadChannel(responseBody),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    }
    install(ContentNegotiation) { json() }
    install(AtelierValidatorClient) {
        register(userValidator)
        useAutomaticValidation = useAutomatic
    }
    extra()
}

class BodyAndValidateTest {

    @Test
    fun `returns object on valid response`() = runTest {
        val client = clientWith(Json.encodeToString(validUser))
        val result = client.get("http://test/users").bodyAndValidate<UserDto>()
        assertEquals(validUser, result)
        client.close()
    }

    @Test
    fun `throws AtelierClientValidationException on invalid response`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.get("http://test/users").bodyAndValidate<UserDto>()
        }
        client.close()
    }

    @Test
    fun `exception contains url`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        val ex = assertFailsWith<AtelierClientValidationException> {
            client.get("http://test/users/1").bodyAndValidate<UserDto>()
        }
        assertTrue(ex.url!!.contains("http://test/users/1"))
        client.close()
    }

    @Test
    fun `exception contains validation errors`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        val ex = assertFailsWith<AtelierClientValidationException> {
            client.get("http://test/users").bodyAndValidate<UserDto>()
        }
        assertTrue(ex.validationResult.errors.isNotEmpty())
        client.close()
    }

    @Test
    fun `throws IllegalStateException when no validator registered`() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(validProduct)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) { json() }
            install(AtelierValidatorClient) {
                register(userValidator) // only user — no product
                useAutomaticValidation = false
            }
        }
        assertFailsWith<IllegalStateException> {
            client.get("http://test/products").bodyAndValidate<ProductDto>()
        }
        client.close()
    }
}

class BodyAndValidateWithOnErrorTest {

    @Test
    fun `returns object on valid response`() = runTest {
        val client = clientWith(Json.encodeToString(validUser))
        val result = client.get("http://test/users").bodyAndValidate<UserDto> { }
        assertEquals(validUser, result)
        client.close()
    }

    @Test
    fun `returns null on invalid response`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        val result = client.get("http://test/users").bodyAndValidate<UserDto> { }
        assertNull(result)
        client.close()
    }

    @Test
    fun `onError is called on invalid response`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        var called = false
        client.get("http://test/users").bodyAndValidate<UserDto> { called = true }
        assertTrue(called)
        client.close()
    }

    @Test
    fun `onError not called on valid response`() = runTest {
        val client = clientWith(Json.encodeToString(validUser))
        var called = false
        client.get("http://test/users").bodyAndValidate<UserDto> { called = true }
        assertFalse(called)
        client.close()
    }

    @Test
    fun `onError receives correct failure`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        var received: ValidationResult.Failure? = null
        client.get("http://test/users").bodyAndValidate<UserDto> { received = it }
        assertNotNull(received)
        assertTrue(received.errors.isNotEmpty())
        client.close()
    }

    @Test
    fun `returns object when no validator registered`() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(validProduct)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) { json() }
            install(AtelierValidatorClient) {
                register(userValidator) // no product validator
                useAutomaticValidation = false
            }
        }
        // No validator → returns body as-is without validation
        val result = client.get("http://test/products").bodyAndValidate<ProductDto> { }
        assertEquals(validProduct, result)
        client.close()
    }
}

class BodyAndValidateOrNullTest {

    @Test
    fun `returns object on valid response`() = runTest {
        val client = clientWith(Json.encodeToString(validUser))
        val result = client.get("http://test/users").bodyAndValidateOrNull<UserDto>()
        assertEquals(validUser, result)
        client.close()
    }

    @Test
    fun `returns null on invalid response`() = runTest {
        val client = clientWith(Json.encodeToString(invalidUser))
        val result = client.get("http://test/users").bodyAndValidateOrNull<UserDto>()
        assertNull(result)
        client.close()
    }

    @Test
    fun `returns null on exception`() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = ByteReadChannel("not json at all"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) { json() }
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }
        val result = client.get("http://test/users").bodyAndValidateOrNull<UserDto>()
        assertNull(result)
        client.close()
    }

    @Test
    fun `returns body when no validator registered`() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = ByteReadChannel(Json.encodeToString(validProduct)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) { json() }
            install(AtelierValidatorClient) {
                register(userValidator)
                useAutomaticValidation = false
            }
        }
        val result = client.get("http://test/products").bodyAndValidateOrNull<ProductDto>()
        assertEquals(validProduct, result)
        client.close()
    }
}

class HttpMethodValidateTest {

    private fun multiMethodClient(responseBody: String): HttpClient = HttpClient(MockEngine) {
        engine {
            addHandler {
                respond(
                    content = ByteReadChannel(responseBody),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }
        install(ContentNegotiation) { json() }
        install(AtelierValidatorClient) {
            register(userValidator)
            useAutomaticValidation = false
        }
    }

    @Test
    fun `getAndValidate returns valid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(validUser))
        assertEquals(validUser, client.getAndValidate<UserDto>("http://test/users/1"))
        client.close()
    }

    @Test
    fun `getAndValidate throws on invalid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.getAndValidate<UserDto>("http://test/users/1")
        }
        client.close()
    }

    @Test
    fun `postAndValidate returns valid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(validUser))
        assertEquals(validUser, client.postAndValidate<UserDto>("http://test/users"))
        client.close()
    }

    @Test
    fun `postAndValidate throws on invalid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.postAndValidate<UserDto>("http://test/users")
        }
        client.close()
    }

    @Test
    fun `putAndValidate returns valid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(validUser))
        assertEquals(validUser, client.putAndValidate<UserDto>("http://test/users/1"))
        client.close()
    }

    @Test
    fun `putAndValidate throws on invalid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.putAndValidate<UserDto>("http://test/users/1")
        }
        client.close()
    }

    @Test
    fun `patchAndValidate returns valid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(validUser))
        assertEquals(validUser, client.patchAndValidate<UserDto>("http://test/users/1"))
        client.close()
    }

    @Test
    fun `patchAndValidate throws on invalid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.patchAndValidate<UserDto>("http://test/users/1")
        }
        client.close()
    }

    @Test
    fun `deleteAndValidate returns valid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(validUser))
        assertEquals(validUser, client.deleteAndValidate<UserDto>("http://test/users/1"))
        client.close()
    }

    @Test
    fun `deleteAndValidate throws on invalid object`() = runTest {
        val client = multiMethodClient(Json.encodeToString(invalidUser))
        assertFailsWith<AtelierClientValidationException> {
            client.deleteAndValidate<UserDto>("http://test/users/1")
        }
        client.close()
    }
}
