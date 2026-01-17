/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validators.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlin.test.*

/**
 * Tests for AtelierValidatorClientConfig (simplified version).
 */
class AtelierValidatorClientConfigTest {

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
        User::name { notBlank() }
        User::email { email() }
        User::age { min(0) }
    }

    private val productValidator = atelierValidator<Product> {
        Product::id { notBlank() }
        Product::name { notBlank() }
        Product::price { positive() }
    }

    @Test
    fun testDefaultConfiguration() {
        val config = AtelierValidatorClientConfig()

        // Default values
        assertFalse(config.useAutomaticValidation) // Manual by default
        assertTrue(config.acceptedStatusCodes.isNotEmpty())

        // Default accepted status codes should be 2xx
        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Created in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Accepted in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.BadRequest in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.NotFound in config.acceptedStatusCodes)
    }

    @Test
    fun testRegisterValidator() {
        val config = AtelierValidatorClientConfig()

        config.register(userValidator)

        assertNotNull(config.validators[User::class])
        assertEquals(1, config.validators.size)
    }

    @Test
    fun testRegisterMultipleValidators() {
        val config = AtelierValidatorClientConfig()

        config.register(userValidator)
        config.register(productValidator)

        assertEquals(2, config.validators.size)
        assertNotNull(config.validators[User::class])
        assertNotNull(config.validators[Product::class])
    }

    @Test
    fun testAcceptStatusCodeRange() {
        val config = AtelierValidatorClientConfig()

        config.acceptStatusCodeRange(200..201)

        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Created in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.Accepted in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.NoContent in config.acceptedStatusCodes)
    }

    @Test
    fun testAcceptStatusCodeRangeWithCustomRange() {
        val config = AtelierValidatorClientConfig()

        config.acceptStatusCodeRange(200..299)

        // All 2xx should be accepted
        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Created in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Accepted in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.NoContent in config.acceptedStatusCodes)
    }

    @Test
    fun testAcceptSpecificStatusCodes() {
        val config = AtelierValidatorClientConfig()

        config.acceptStatusCodes(
            HttpStatusCode.OK,
            HttpStatusCode.Created,
            HttpStatusCode.NoContent
        )

        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Created in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.NoContent in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.Accepted in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.BadRequest in config.acceptedStatusCodes)
    }

    @Test
    fun testAcceptOnlySuccessCodes() {
        val config = AtelierValidatorClientConfig()

        config.acceptStatusCodes(HttpStatusCode.OK)

        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertFalse(HttpStatusCode.Created in config.acceptedStatusCodes)
    }

    @Test
    fun testUseAutomaticValidationFlag() {
        val config = AtelierValidatorClientConfig()

        // Default is false (manual validation)
        assertFalse(config.useAutomaticValidation)

        // Can be enabled
        config.useAutomaticValidation = true
        assertTrue(config.useAutomaticValidation)
    }

    @Test
    fun testValidatorTypeChecking() {
        val config = AtelierValidatorClientConfig()

        config.register(userValidator)

        val validator = config.validators[User::class]
        assertNotNull(validator)

        // Should work with correct type
        val validUser = User("1", "John", "john@example.com", 25)
        val result = validator.validate(validUser)
        assertTrue(result is dev.megatilus.atelier.results.ValidationResult.Success)

        // Should throw with incorrect type
        assertFailsWith<IllegalArgumentException> {
            validator.validate("wrong type" as Any)
        }
    }

    @Test
    fun testMultipleStatusCodeRanges() {
        val config1 = AtelierValidatorClientConfig()
        config1.acceptStatusCodeRange(200..299)

        val config2 = AtelierValidatorClientConfig()
        config2.acceptStatusCodeRange(400..499)

        // config1 accepts 2xx
        assertTrue(HttpStatusCode.OK in config1.acceptedStatusCodes)
        assertFalse(HttpStatusCode.BadRequest in config1.acceptedStatusCodes)

        // config2 accepts 4xx
        assertFalse(HttpStatusCode.OK in config2.acceptedStatusCodes)
        assertTrue(HttpStatusCode.BadRequest in config2.acceptedStatusCodes)
    }

    @Test
    fun testConfigurationChaining() {
        val config = AtelierValidatorClientConfig().apply {
            register(userValidator)
            register(productValidator)
            acceptStatusCodeRange(200..201)
            useAutomaticValidation = false
        }

        assertEquals(2, config.validators.size)
        assertEquals(2, config.acceptedStatusCodes.size)
        assertFalse(config.useAutomaticValidation)
    }

    @Test
    fun testEmptyValidators() {
        val config = AtelierValidatorClientConfig()

        assertTrue(config.validators.isEmpty())
    }

    @Test
    fun testOverwriteAcceptedStatusCodes() {
        val config = AtelierValidatorClientConfig()

        // First set
        config.acceptStatusCodes(HttpStatusCode.OK)
        assertEquals(1, config.acceptedStatusCodes.size)

        // Overwrite
        config.acceptStatusCodes(HttpStatusCode.Created, HttpStatusCode.Accepted)
        assertEquals(2, config.acceptedStatusCodes.size)
        assertFalse(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Created in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.Accepted in config.acceptedStatusCodes)
    }

    @Test
    fun testAcceptAllStatusCodes() {
        val config = AtelierValidatorClientConfig()

        // Accept a very wide range
        config.acceptStatusCodeRange(100..599)

        assertTrue(HttpStatusCode.OK in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.BadRequest in config.acceptedStatusCodes)
        assertTrue(HttpStatusCode.InternalServerError in config.acceptedStatusCodes)
    }
}
