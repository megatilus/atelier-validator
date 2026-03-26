/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.rules.notNull
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationResultTest {

    data class User(
        val name: String?,
        val email: String?,
        val age: Int?
    )

    @Test
    fun `Success should have isSuccess true`() {
        val result = ValidationResult.Success

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `Failure should contain correct error count`() {
        val validator = AtelierValidator<User> {
            User::name { notNull() hint "Name required" }
            User::email { notNull() hint "Email required" }
        }

        val result = validator.validate(User(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(2, result.errorCount)
    }

    @Test
    fun `errorsFor should return errors for specific field`() {
        val validator = AtelierValidator<User> {
            User::name { notNull() hint "Name required" }
            User::email { notNull() hint "Email required" }
        }

        val result = validator.validate(User(name = null, email = null, age = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorsFor("name").size)
        assertEquals(1, result.errorsFor("email").size)
        assertEquals(0, result.errorsFor("age").size)
    }
}
