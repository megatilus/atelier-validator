/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validators.*
import kotlinx.serialization.Serializable
import kotlin.test.*

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

        config.useAutomaticValidation = false

        assertFalse(config.useAutomaticValidation)
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
    fun testUseAutomaticValidationFlag() {
        val config = AtelierValidatorClientConfig()

        // Default is true (automatic validation)
        assertTrue(config.useAutomaticValidation)

        // Can be disabled
        config.useAutomaticValidation = false
        assertFalse(config.useAutomaticValidation)
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
    fun testEmptyValidators() {
        val config = AtelierValidatorClientConfig()

        assertTrue(config.validators.isEmpty())
    }

    @Test
    fun testValidatorOverwrite() {
        val config = AtelierValidatorClientConfig()

        // First validator
        val firstValidator = atelierValidator<User> {
            User::name { minLength(5) }
        }

        // Second validator for same type
        val secondValidator = atelierValidator<User> {
            User::name { minLength(10) }
        }

        config.register(firstValidator)
        config.register(secondValidator)

        // Should have only one validator (latest one)
        assertEquals(1, config.validators.size)
    }

    @Test
    fun testValidatorWithNullableFields() {
        @Serializable
        data class OptionalUser(
            val id: String,
            val name: String?,
            val email: String?
        )

        val optionalValidator = atelierValidator<OptionalUser> {
            OptionalUser::id { notBlank() }
        }

        val config = AtelierValidatorClientConfig()
        config.register(optionalValidator)

        assertNotNull(config.validators[OptionalUser::class])
    }

    @Test
    fun testConfigIsolation() {
        val config1 = AtelierValidatorClientConfig()
        val config2 = AtelierValidatorClientConfig()

        config1.register(userValidator)
        config2.register(productValidator)

        // Configs should be isolated
        assertEquals(1, config1.validators.size)
        assertEquals(1, config2.validators.size)
        assertNotNull(config1.validators[User::class])
        assertNull(config1.validators[Product::class])
        assertNotNull(config2.validators[Product::class])
        assertNull(config2.validators[User::class])
    }

    @Test
    fun testAutomaticValidationToggle() {
        val config = AtelierValidatorClientConfig()

        // Can toggle multiple times
        assertTrue(config.useAutomaticValidation)

        config.useAutomaticValidation = false
        assertFalse(config.useAutomaticValidation)
    }
}
