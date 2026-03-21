/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

import dev.megatilus.atelier.AtelierValidator
import dev.megatilus.atelier.rules.email
import dev.megatilus.atelier.rules.max
import dev.megatilus.atelier.rules.maxLength
import dev.megatilus.atelier.rules.min
import dev.megatilus.atelier.rules.minLength
import dev.megatilus.atelier.rules.notBlank
import dev.megatilus.atelier.rules.notNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationErrorCodeTest {

    data class TestEntity(
        val name: String?,
        val email: String?,
        val age: Int?
    )

    @Test
    fun `notNull should produce required error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name { notNull() }
        }

        val result = validator.validate(TestEntity(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("required", result.errors.first().code.code)
    }

    @Test
    fun `notBlank should produce required error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank()
            }
        }

        val result = validator.validate(TestEntity(name = "", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("required", result.errors.first().code.code)
    }

    @Test
    fun `email should produce invalid_format error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email { email() }
        }

        val result = validator.validate(TestEntity(name = null, email = "invalid", age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("invalid_email", result.errors.first().code.code)
    }

    @Test
    fun `min should produce out_of_range error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::age { min(18) }
        }

        val result = validator.validate(TestEntity(name = null, email = null, age = 16))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("out_of_range", result.errors.first().code.code)
    }

    @Test
    fun `max should produce out_of_range error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::age { max(120) }
        }

        val result = validator.validate(TestEntity(name = null, email = null, age = 150))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("out_of_range", result.errors.first().code.code)
    }

    @Test
    fun `minLength should produce too_short error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name { minLength(5) }
        }

        val result = validator.validate(TestEntity(name = "Jo", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("too_short", result.errors.first().code.code)
    }

    @Test
    fun `maxLength should produce too_long error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name { maxLength(10) }
        }

        val result = validator.validate(TestEntity(name = "VeryLongName", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("too_long", result.errors.first().code.code)
    }

    @Test
    fun `all validation errors should have consistent error codes`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank() hint "Name required"
                minLength(3) hint "Name too short"
            }
            TestEntity::email {
                email() hint "Invalid email"
            }
            TestEntity::age {
                min(18) hint "Too young"
            }
        }

        val result = validator.validate(TestEntity(name = "Jo", email = "bad", age = 16))

        assertTrue(result is ValidationResult.Failure)
        
        // Each error should have a valid error code
        result.errors.forEach { error ->
            assertTrue(error.code.code.isNotEmpty())
            assertTrue(error.code.code.matches(Regex("^[a-z_]+$")))
        }
    }

    @Test
    fun `custom rules should produce custom_error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                customRule { it == null || it.startsWith("user_") } hint "Must start with user_"
            }
        }

        val result = validator.validate(TestEntity(name = "john", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals("custom_error", result.errors.first().code.code)
    }

    @Test
    fun `ValidationError should contain error code`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email { email() hint "Invalid email format" }
        }

        val result = validator.validate(TestEntity(name = null, email = "notanemail", age = null))

        assertTrue(result is ValidationResult.Failure)
        val error = result.errors.first()
        
        assertEquals("email", error.fieldName)
        assertEquals("Invalid email format", error.message)
        assertEquals("invalid_email", error.code.code)
        assertEquals("notanemail", error.actualValue)
    }
}
