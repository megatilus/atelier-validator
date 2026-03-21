/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.rules.notBlank
import kotlin.test.*

class AtelierValidatorConfigTest {

    @Test
    fun `register stores validator by KClass key`() {
        val config = AtelierValidatorServerConfig()
        config.register(userValidator)
        assertTrue(config.validators.containsKey(UserDto::class))
    }

    @Test
    fun `register multiple validators stores all`() {
        val config = AtelierValidatorServerConfig()
        config.register(userValidator)
        config.register(productValidator)
        assertEquals(2, config.validators.size)
    }

    @Test
    fun `register overwrites previous validator for same type`() {
        val config = AtelierValidatorServerConfig()
        config.register(userValidator)
        val anotherUserValidator = AtelierValidator<UserDto> {
            UserDto::name { notBlank() }
        }
        config.register(anotherUserValidator)
        assertEquals(1, config.validators.size)
    }

    @Test
    fun `wrapper validate returns Success for valid object`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validate(UserDto("John", "john@example.com", 25)) is ValidationResult.Success)
    }

    @Test
    fun `wrapper validate returns Failure for invalid object`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validate(UserDto("", "bad", 25)) is ValidationResult.Failure)
    }

    @Test
    fun `wrapper validate throws IllegalArgumentException on type mismatch`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        val ex = assertFailsWith<IllegalArgumentException> {
            wrapper.validate("wrong type")
        }
        assertTrue(ex.message!!.contains("UserDto"))
    }

    @Test
    fun `wrapper validateFirst returns Success for valid object`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validateFirst(UserDto("John", "john@example.com", 25)) is ValidationResult.Success)
    }

    @Test
    fun `wrapper validateFirst returns Failure with single error`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        val result = wrapper.validateFirst(UserDto("", "bad", 25))
        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun `wrapper validateFirst throws IllegalArgumentException on type mismatch`() {
        val config = AtelierValidatorServerConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertFailsWith<IllegalArgumentException> {
            wrapper.validateFirst(42)
        }
    }

    @Test
    fun `validateConfiguration throws when no validators and validateAtStartup true`() {
        val config = AtelierValidatorServerConfig().apply { validateAtStartup = true }
        assertFailsWith<IllegalStateException> { config.validateConfiguration() }
    }

    @Test
    fun `validateConfiguration error message is descriptive`() {
        val config = AtelierValidatorServerConfig().apply { validateAtStartup = true }
        val ex = assertFailsWith<IllegalStateException> { config.validateConfiguration() }
        assertTrue(ex.message!!.contains("No validators registered"))
    }

    @Test
    fun `validateConfiguration passes when at least one validator registered`() {
        val config = AtelierValidatorServerConfig().apply {
            validateAtStartup = true
            register(userValidator)
        }
        config.validateConfiguration() // no throw
    }

    @Test
    fun `validateConfiguration skips check when validateAtStartup false`() {
        val config = AtelierValidatorServerConfig().apply {
            validateAtStartup = false
            // no validators — should not throw
        }
        config.validateConfiguration() // no throw
    }

    @Test
    fun `default errorResponseBuilder returns AtelierValidationErrorResponse`() {
        val config = AtelierValidatorServerConfig()
        val failure = ValidationResult.Failure(emptyList())
        val response = config.errorResponseBuilder(failure)
        assertTrue(response is AtelierValidationErrorResponse)
    }

    @Test
    fun `custom errorResponseBuilder is invoked`() {
        var called = false
        val config = AtelierValidatorServerConfig().apply {
            errorResponseBuilder = { _ ->
                called = true
                "custom response"
            }
        }
        config.errorResponseBuilder(ValidationResult.Failure(emptyList()))
        assertTrue(called)
    }

    @Test
    fun `custom errorResponseBuilder receives correct failure`() {
        val failure = ValidationResult.Failure(emptyList())
        var received: ValidationResult.Failure? = null
        val config = AtelierValidatorServerConfig().apply {
            errorResponseBuilder = { f ->
                received = f
                "ok"
            }
        }
        config.errorResponseBuilder(failure)
        assertSame(failure, received)
    }

    @Test
    fun `useAutomaticValidation defaults to true`() {
        val config = AtelierValidatorServerConfig()
        assertTrue(config.useAutomaticValidation)
    }

    @Test
    fun `validateAtStartup defaults to true`() {
        val config = AtelierValidatorServerConfig()
        assertTrue(config.validateAtStartup)
    }
}
