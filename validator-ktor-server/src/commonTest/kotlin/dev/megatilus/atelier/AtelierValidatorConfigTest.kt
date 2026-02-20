/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.validators.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlin.test.*

class AtelierValidatorConfigTest {

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
    fun `config should have default values`() {
        val config = AtelierValidatorConfig()

        assertEquals(HttpStatusCode.BadRequest, config.errorStatusCode)
        assertTrue(config.useAutomaticValidation)
        assertTrue(config.validateAtStartup)
        assertTrue(config.validators.isEmpty())
    }

    @Test
    fun `register should add validator to map`() {
        val config = AtelierValidatorConfig()

        config.register(userValidator)

        assertEquals(1, config.validators.size)
        assertTrue(config.validators.containsKey(User::class))
    }

    @Test
    fun `register should allow multiple validators`() {
        val config = AtelierValidatorConfig()

        config.register(userValidator)
        config.register(productValidator)

        assertEquals(2, config.validators.size)
        assertTrue(config.validators.containsKey(User::class))
        assertTrue(config.validators.containsKey(Product::class))
    }

    @Test
    fun `registered validator should validate correct type`() {
        val config = AtelierValidatorConfig()
        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        val validUser = User(name = "John Doe", email = "john@example.com", age = 25)
        val result = validator.validate(validUser)

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `registered validator should fail validation for invalid data`() {
        val config = AtelierValidatorConfig()
        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertTrue(result.errorCount > 0)
    }

    @Test
    fun `registered validator should throw exception for wrong type`() {
        val config = AtelierValidatorConfig()
        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        val product = Product(name = "Test", price = 10.0)

        assertFailsWith<IllegalArgumentException> {
            validator.validate(product)
        }
    }

    @Test
    fun `custom errorStatusCode should be set`() {
        val config = AtelierValidatorConfig()
        config.errorStatusCode = HttpStatusCode.UnprocessableEntity

        assertEquals(HttpStatusCode.UnprocessableEntity, config.errorStatusCode)
    }

    @Test
    fun `useAutomaticValidation should be configurable`() {
        val config = AtelierValidatorConfig()

        config.useAutomaticValidation = false
        assertFalse(config.useAutomaticValidation)

        config.useAutomaticValidation = true
        assertTrue(config.useAutomaticValidation)
    }

    @Test
    fun `validateAtStartup should be configurable`() {
        val config = AtelierValidatorConfig()

        assertTrue(config.validateAtStartup) // Default is true

        config.validateAtStartup = false
        assertFalse(config.validateAtStartup)

        config.validateAtStartup = true
        assertTrue(config.validateAtStartup)
    }

    @Test
    fun `custom errorResponseBuilder should be set`() {
        val config = AtelierValidatorConfig()

        @Serializable
        data class CustomError(val status: String, val errorCount: Int)

        config.errorResponseBuilder = { failure ->
            CustomError(status = "error", errorCount = failure.errorCount)
        }

        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        val customResponse = config.errorResponseBuilder(result) as CustomError

        assertEquals("error", customResponse.status)
        assertTrue(customResponse.errorCount > 0)
    }

    @Test
    fun `default errorResponseBuilder should return AtelierValidationErrorResponse`() {
        val config = AtelierValidatorConfig()

        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = userValidator.validate(invalidUser) as ValidationResult.Failure

        val response = config.errorResponseBuilder(result)

        assertTrue(response is AtelierValidationErrorResponse)
        assertEquals("Request validation failed: 4 error(s) detected", response.message)
        assertTrue(response.errors.isNotEmpty())
    }

    @Test
    fun `validateFirst should stop at first error`() {
        val config = AtelierValidatorConfig()
        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = validator.validateFirst(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `validate should collect all errors`() {
        val config = AtelierValidatorConfig()
        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        val invalidUser = User(name = "", email = "invalid", age = 15)
        val result = validator.validate(invalidUser)

        assertTrue(result is ValidationResult.Failure)
        assertTrue(result.errorCount >= 3) // name, email, age
    }

    @Test
    fun `validateConfiguration should throw when no validators registered and validateAtStartup is true`() {
        val config = AtelierValidatorConfig()
        config.validateAtStartup = true

        val exception = assertFailsWith<IllegalStateException> {
            config.validateConfiguration()
        }

        assertTrue(exception.message!!.contains("No validators registered"))
    }

    @Test
    fun `validateConfiguration should not throw when validateAtStartup is false`() {
        val config = AtelierValidatorConfig()
        config.validateAtStartup = false

        // Should not throw even with no validators
        config.validateConfiguration()
    }

    @Test
    fun `validateConfiguration should not throw when validators are registered`() {
        val config = AtelierValidatorConfig()
        config.validateAtStartup = true
        config.register(userValidator)

        // Should not throw
        config.validateConfiguration()
    }
}
