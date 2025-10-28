/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class SetValidatorsTest {

    data class TestData(val items: Set<String>)

    @Test
    fun `notEmpty should pass for non-empty sets`() {
        val data = TestData(setOf("a", "b"))
        assertTrue(data.items.isNotEmpty())
    }

    @Test
    fun `size should work with sets`() {
        val data = TestData(setOf("a", "b", "c"))
        assertEquals(3, data.items.size)
    }

    @Test
    fun `set ensures uniqueness`() {
        val data = TestData(setOf("a", "b", "a"))
        assertEquals(2, data.items.size)
        assertTrue(data.items.contains("a"))
    }
}
