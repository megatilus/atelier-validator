/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.ktor.server.AtelierValidationException
import dev.megatilus.atelier.ktor.server.AtelierValidatorServer
import dev.megatilus.atelier.ktor.server.AtelierValidatorServerConfig
import dev.megatilus.atelier.ktor.server.receiveAndValidate
import dev.megatilus.atelier.validator.results.ValidationResult
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

private fun ApplicationTestBuilder.setupManual(
    extra: AtelierValidatorServerConfig.() -> Unit = {}
) {
    install(ContentNegotiation) { json() }

    install(AtelierValidatorServer) {
        register(userValidator)
        useAutomaticValidation = false
        extra()
    }
}

class ReceiveAndValidateTest {

    @Test
    fun `returns object on valid body`() = testApplication {
        setupManual()
        routing {
            post("/users") {
                val user = call.receiveAndValidate<UserDto>() ?: return@post
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
    fun `returns null and 400 on invalid body`() = testApplication {
        setupManual()
        routing {
            post("/users") {
                val user = call.receiveAndValidate<UserDto>() ?: return@post
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
    fun `returns null and 500 when no validator registered for type`() = testApplication {
        setupManual() // only userValidator
        routing {
            post("/products") {
                call.receiveAndValidate<ProductDto>() ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        val r = client.post("/products") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Widget","price":9.99}""")
        }
        assertEquals(HttpStatusCode.InternalServerError, r.status)
    }

    @Test
    fun `route handler not executed when validation fails`() = testApplication {
        setupManual()
        var handlerExecuted = false
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto>() ?: return@post
                handlerExecuted = true
                call.respond(HttpStatusCode.Created)
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertFalse(handlerExecuted)
    }

    @Test
    fun `route handler executed when validation succeeds`() = testApplication {
        setupManual()
        var handlerExecuted = false
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto>() ?: return@post
                handlerExecuted = true
                call.respond(HttpStatusCode.Created)
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","email":"john@example.com","age":25}""")
        }
        assertTrue(handlerExecuted)
    }

    @Test
    fun `multiple validators work independently`() = testApplication {
        setupManual { register(productValidator) }
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto>() ?: return@post
                call.respond(HttpStatusCode.Created)
            }
            post("/products") {
                call.receiveAndValidate<ProductDto>() ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }

        assertEquals(
            HttpStatusCode.Created,
            client.post("/users") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"John","email":"john@example.com","age":25}""")
            }.status
        )

        assertEquals(
            HttpStatusCode.Created,
            client.post("/products") {
                contentType(ContentType.Application.Json)
                setBody("""{"title":"Widget","price":9.99}""")
            }.status
        )
    }

    @Test
    fun `invalid user and invalid product both return 400`() = testApplication {
        setupManual { register(productValidator) }
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto>() ?: return@post
                call.respond(HttpStatusCode.Created)
            }
            post("/products") {
                call.receiveAndValidate<ProductDto>() ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }

        assertEquals(
            HttpStatusCode.BadRequest,
            client.post("/users") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"","email":"bad","age":25}""")
            }.status
        )

        assertEquals(
            HttpStatusCode.BadRequest,
            client.post("/products") {
                contentType(ContentType.Application.Json)
                setBody("""{"title":"","price":-1.0}""")
            }.status
        )
    }
}

class ReceiveAndValidateWithOnErrorTest {

    private fun ApplicationTestBuilder.setup() {
        install(ContentNegotiation) { json() }
        install(AtelierValidatorServer) {
            register(userValidator)
            useAutomaticValidation = false
        }
    }

    @Test
    fun `onError called on invalid body`() = testApplication {
        setup()
        var onErrorCalled = false
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto> {
                    onErrorCalled = true
                    call.respond(HttpStatusCode.UnprocessableEntity)
                } ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertTrue(onErrorCalled)
    }

    @Test
    fun `onError not called on valid body`() = testApplication {
        setup()
        var onErrorCalled = false
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto> { onErrorCalled = true } ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"John","email":"john@example.com","age":25}""")
        }
        assertFalse(onErrorCalled)
    }

    @Test
    fun `onError receives correct failure`() = testApplication {
        setup()
        var receivedFailure: ValidationResult.Failure? = null
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto> { failure ->
                    receivedFailure = failure
                    call.respond(HttpStatusCode.BadRequest)
                } ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertNotNull(receivedFailure)
        assertTrue(receivedFailure.errors.isNotEmpty())
    }

    @Test
    fun `onError can respond with custom status`() = testApplication {
        setup()
        routing {
            post("/users") {
                call.receiveAndValidate<UserDto> {
                    call.respond(HttpStatusCode.UnprocessableEntity, "custom")
                } ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        val r = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, r.status)
    }

    @Test
    fun `onError can throw AtelierValidationException`() = testApplication {
        setup()
        var threwException = false
        routing {
            post("/users") {
                try {
                    call.receiveAndValidate<UserDto> { failure ->
                        throw AtelierValidationException(failure)
                    } ?: return@post
                    call.respond(HttpStatusCode.Created)
                } catch (e: AtelierValidationException) {
                    threwException = true
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"","email":"bad","age":25}""")
        }
        assertTrue(threwException)
    }

    @Test
    fun `returns 500 when no validator registered for type`() = testApplication {
        setup()

        routing {
            post("/products") {
                call.receiveAndValidate<ProductDto> {
                    call.respond(HttpStatusCode.BadRequest)
                } ?: return@post
                call.respond(HttpStatusCode.Created)
            }
        }
        val r = client.post("/products") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Widget","price":9.99}""")
        }
        assertEquals(HttpStatusCode.InternalServerError, r.status)
    }
}
