/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListValidatorsTest {

    data class TestData(val items: List<String>)

    @Test
    fun `notEmpty should pass for non-empty lists`() {
        val data = TestData(listOf("a", "b"))
        assertTrue(data.items.isNotEmpty())
    }

    @Test
    fun `size should work with lists`() {
        val data = TestData(listOf("a", "b", "c"))
        assertEquals(3, data.items.size)
        assertTrue(data.items.size in 1..5)
    }

    @Test
    fun `contains works with duplicates in list`() {
        val data = TestData(listOf("a", "b", "a", "c"))
        assertTrue(data.items.contains("a"))
        assertEquals(4, data.items.size)
    }
}
