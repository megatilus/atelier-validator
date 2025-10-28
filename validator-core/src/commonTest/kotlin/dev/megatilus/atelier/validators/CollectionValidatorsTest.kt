/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionValidatorsTest {

    data class TestData(val items: Collection<String>)

    @Test
    fun `notEmpty should pass for non-empty collections`() {
        val data = TestData(listOf("a", "b"))
        assertTrue(data.items.isNotEmpty())
    }

    @Test
    fun `notEmpty should fail for empty collections`() {
        val data = TestData(emptyList())
        assertFalse(data.items.isNotEmpty())
    }

    @Test
    fun `size should validate collection size range`() {
        val data = TestData(listOf("a", "b", "c"))
        assertTrue(data.items.size in 1..5)
    }

    @Test
    fun `contains should work for collections`() {
        val data = TestData(setOf("a", "b", "c"))
        assertTrue(data.items.contains("b"))
        assertFalse(data.items.contains("z"))
    }

    @Test
    fun `containsAll should validate all elements present`() {
        val data = TestData(listOf("a", "b", "c", "d"))
        val required = listOf("a", "c")
        assertTrue(data.items.containsAll(required))
    }

    @Test
    fun `containsAny should validate at least one element`() {
        val data = TestData(setOf("a", "b", "c"))
        assertTrue(listOf("x", "b", "z").any { it in data.items })
    }
}
