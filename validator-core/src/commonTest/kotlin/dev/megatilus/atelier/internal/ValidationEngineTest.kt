/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.internal

import dev.megatilus.atelier.AtelierValidator
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.rules.each
import dev.megatilus.atelier.rules.email
import dev.megatilus.atelier.rules.maxLength
import dev.megatilus.atelier.rules.min
import dev.megatilus.atelier.rules.minLength
import dev.megatilus.atelier.rules.nested
import dev.megatilus.atelier.rules.notBlank
import dev.megatilus.atelier.rules.notNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationEngineTest {

    data class User(
        val name: String?,
        val email: String?,
        val age: Int?
    )

    @Test
    fun `engine should validate single rule successfully`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() }
        }

        val result = validator.validate(User(name = "John", email = null, age = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `engine should collect validation errors`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() hint "Name required" }
            User::email { email() hint "Invalid email" }
        }

        val result = validator.validate(User(name = "", email = "invalid", age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(2, result.errorCount)
    }

    @Test
    fun `engine should stop on first failed rule per field`() {
        val validator = AtelierValidator<User> {
            User::name {
                notBlank() hint "Name is required"
                minLength(5) hint "Name too short"
                maxLength(50) hint "Name too long"
            }
        }

        val result = validator.validate(User(name = "Jo", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        // Should only have 1 error (minLength), not maxLength
        assertEquals(1, result.errorCount)
        assertEquals("Name too short", result.errors.first().message)
    }

    @Test
    fun `engine should validate all rules when all pass`() {
        val validator = AtelierValidator<User> {
            User::name {
                notBlank()
                minLength(2)
                maxLength(50)
            }
        }

        val result = validator.validate(User(name = "John", email = null, age = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `engine should validate all fields independently`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() hint "Name is required" }
            User::email { email() hint "Invalid email" }
            User::age { min(18) hint "Must be 18+" }
        }

        val result = validator.validate(User(name = "", email = "bad", age = 10))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(3, result.errorCount)

        val fieldNames = result.errors.map { it.fieldName }
        assertTrue("name" in fieldNames)
        assertTrue("email" in fieldNames)
        assertTrue("age" in fieldNames)
    }

    @Test
    fun `validateFirst should stop on first error`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() hint "Name is required" }
            User::email { email() hint "Invalid email" }
            User::age { min(18) hint "Must be 18+" }
        }

        val result = validator.validateFirst(User(name = "", email = "bad", age = 10))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        // Should only have first error (name)
        assertEquals("name", result.errors.first().fieldName)
    }

    @Test
    fun `validateFirst should succeed when all valid`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() }
            User::email { email() }
        }

        val result = validator.validateFirst(User(name = "John", email = "john@example.com", age = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `engine should collect error details correctly`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() hint "Name is required" }
        }

        val result = validator.validate(User(name = "", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        val error = result.errors.first()
        assertEquals("name", error.fieldName)
        assertEquals("Name is required", error.message)
        assertEquals("required", error.code.code)
        assertEquals("", error.actualValue)
    }

    @Test
    fun `engine should handle nested validation`() {
        data class Address(val city: String?)
        data class UserWithAddress(val name: String?, val address: Address?)

        val addressValidator = AtelierValidator<Address> {
            Address::city { notBlank() }
        }

        val validator = AtelierValidator<UserWithAddress> {
            UserWithAddress::name { notBlank() }
            UserWithAddress::address {
                nested(addressValidator)
            }
        }

        val result = validator.validate(
            UserWithAddress(
                name = "John",
                address = Address(city = "")
            )
        )

        assertTrue(result is ValidationResult.Failure)
    }

    @Test
    fun `engine should handle validator with no rules`() {
        val validator = AtelierValidator<User> {
            // No rules
        }

        val result = validator.validate(User(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `engine should handle null values with constrainIfNotNull`() {
        val validator = AtelierValidator<User> {
            User::name {
                minLength(5)
            }
        }

        // null should skip validation
        val result = validator.validate(User(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `engine should fail null values with constrain`() {
        val validator = AtelierValidator<User> {
            User::name {
                notNull()
            }
        }

        val result = validator.validate(User(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
    }
}
