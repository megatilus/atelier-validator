/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.rules.notBlank
import kotlin.test.*

class AtelierValidatorClientConfigTest {

    @Test
    fun `register stores validator by KClass key`() {
        val config = AtelierValidatorClientConfig()
        config.register(userValidator)
        assertTrue(config.validators.containsKey(UserDto::class))
    }

    @Test
    fun `register multiple validators stores all`() {
        val config = AtelierValidatorClientConfig()
        config.register(userValidator)
        config.register(productValidator)
        assertEquals(2, config.validators.size)
    }

    @Test
    fun `register overwrites previous validator for same type`() {
        val config = AtelierValidatorClientConfig()
        config.register(userValidator)
        val another = AtelierValidator<UserDto> { UserDto::name { notBlank() } }
        config.register(another)
        assertEquals(1, config.validators.size)
    }

    @Test
    fun `wrapper validate returns Success for valid object`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validate(UserDto("John", "john@example.com", 25)) is ValidationResult.Success)
    }

    @Test
    fun `wrapper validate returns Failure for invalid object`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validate(UserDto("", "bad", 25)) is ValidationResult.Failure)
    }

    @Test
    fun `wrapper validate throws IllegalArgumentException on type mismatch`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        val ex = assertFailsWith<IllegalArgumentException> { wrapper.validate("wrong") }
        assertTrue(ex.message!!.contains("UserDto"))
    }

    @Test
    fun `wrapper validateFirst returns Success for valid object`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertTrue(wrapper.validateFirst(UserDto("John", "john@example.com", 25)) is ValidationResult.Success)
    }

    @Test
    fun `wrapper validateFirst returns Failure with single error`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        val result = wrapper.validateFirst(UserDto("", "bad", 25))
        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errors.size)
    }

    @Test
    fun `wrapper validateFirst throws IllegalArgumentException on type mismatch`() {
        val config = AtelierValidatorClientConfig().apply { register(userValidator) }
        val wrapper = config.validators[UserDto::class]!!
        assertFailsWith<IllegalArgumentException> { wrapper.validateFirst(42) }
    }

    @Test
    fun `useAutomaticValidation defaults to true`() {
        assertTrue(AtelierValidatorClientConfig().useAutomaticValidation)
    }
}
