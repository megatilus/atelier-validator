/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.rules.email
import dev.megatilus.atelier.rules.min
import dev.megatilus.atelier.rules.notBlank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AtelierValidatorTest {

    data class User(
        val name: String?,
        val email: String?,
        val age: Int?
    )

    @Test
    fun `validator should validate successfully when all rules pass`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() }
            User::email { email() }
        }

        val result = validator.validate(User(name = "John", email = "john@example.com", age = 25))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `validator should fail when any rule fails`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() }
        }

        val result = validator.validate(User(name = "", email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `validator should produce errors for all failing fields`() {
        val validator = AtelierValidator<User> {
            User::name { notBlank() hint "Name required" }
            User::email { email() hint "Invalid email" }
            User::age { min(18) hint "Must be 18+" }
        }

        val result = validator.validate(User(name = "", email = "invalid", age = 16))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(3, result.errorCount)
    }

    @Test
    fun `custom rule should work with constrainIfNotNull`() {
        val validator = AtelierValidator<User> {
            User::name {
                customRule { value -> value == null || value.startsWith("J") } hint "Name must start with J"
            }
        }

        assertTrue(validator.validate(User(name = "John", email = null, age = null)) is ValidationResult.Success)
        assertTrue(validator.validate(User(name = null, email = null, age = null)) is ValidationResult.Success)
        assertTrue(validator.validate(User(name = "Alice", email = null, age = null)) is ValidationResult.Failure)
    }
}
