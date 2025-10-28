/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class BooleanValidatorsTest {

    data class TestData(val flag: Boolean)
    data class TestDataNullable(val flag: Boolean?)

    @Test
    fun `isTrue should pass for true values`() {
        val data = TestData(true)
        assertTrue(data.flag)
    }

    @Test
    fun `isTrue should fail for false values`() {
        val data = TestData(false)
        assertFalse(data.flag)
    }

    @Test
    fun `isFalse should pass for false values`() {
        val data = TestData(false)
        assertFalse(data.flag)
    }

    @Test
    fun `isFalse should fail for true values`() {
        val data = TestData(true)
        assertTrue(data.flag)
    }

    @Test
    fun `nullable isTrue should handle null`() {
        val data = TestDataNullable(null)
        assertNotEquals(data.flag, true)
    }

    @Test
    fun `nullable isTrue should pass for true`() {
        val data = TestDataNullable(true)
        assertEquals(data.flag, true)
    }

    @Test
    fun `nullable isFalse should handle null`() {
        val data = TestDataNullable(null)
        assertNotEquals(data.flag, false)
    }

    @Test
    fun `nullable isFalse should pass for false`() {
        val data = TestDataNullable(false)
        assertEquals(data.flag, false)
    }
}
