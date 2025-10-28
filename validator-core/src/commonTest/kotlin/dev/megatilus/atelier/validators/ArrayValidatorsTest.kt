/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class ArrayValidatorsTest {

    data class TestData(val items: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as TestData
            return items.contentEquals(other.items)
        }
        override fun hashCode(): Int = items.contentHashCode()
    }

    @Test
    fun `notEmpty should pass for non-empty arrays`() {
        val data = TestData(arrayOf("a", "b"))
        assertTrue(data.items.isNotEmpty())
    }

    @Test
    fun `notEmpty should fail for empty arrays`() {
        val data = TestData(arrayOf())
        assertFalse(data.items.isNotEmpty())
    }

    @Test
    fun `isEmpty should pass for empty arrays`() {
        val data = TestData(arrayOf())
        assertTrue(data.items.isEmpty())
    }

    @Test
    fun `isEmpty should fail for non-empty arrays`() {
        val data = TestData(arrayOf("a"))
        assertFalse(data.items.isEmpty())
    }

    @Test
    fun `size should pass when in range`() {
        val data = TestData(arrayOf("a", "b", "c"))
        val size = data.items.size
        assertTrue(size in 1..5)
        assertTrue(size in 3..3)
    }

    @Test
    fun `size should fail when out of range`() {
        val data = TestData(arrayOf("a"))
        assertFalse(data.items.size >= 5)

        val data2 = TestData(arrayOf("a", "b", "c", "d", "e", "f"))
        assertFalse(data2.items.size <= 3)
    }

    @Test
    fun `minSize should pass when size is sufficient`() {
        val data = TestData(arrayOf("a", "b", "c"))
        assertTrue(data.items.size >= 2)
    }

    @Test
    fun `maxSize should pass when size is within limit`() {
        val data = TestData(arrayOf("a", "b"))
        assertTrue(data.items.size <= 5)
    }

    @Test
    fun `exactSize should pass when size matches`() {
        val data = TestData(arrayOf("a", "b", "c"))
        assertEquals(3, data.items.size)
    }

    @Test
    fun `exactSize should fail when size differs`() {
        val data = TestData(arrayOf("a", "b"))
        assertNotEquals(5, data.items.size)
    }

    @Test
    fun `contains should pass when element exists`() {
        val data = TestData(arrayOf("a", "b", "c"))
        assertTrue(data.items.contains("b"))
    }

    @Test
    fun `contains should fail when element missing`() {
        val data = TestData(arrayOf("a", "b"))
        assertFalse(data.items.contains("z"))
    }

    @Test
    fun `doesNotContain should pass when element missing`() {
        val data = TestData(arrayOf("a", "b"))
        assertFalse(data.items.contains("z"))
    }

    @Test
    fun `doesNotContain should fail when element exists`() {
        val data = TestData(arrayOf("a", "b", "c"))
        assertTrue(data.items.contains("b"))
    }

    @Test
    fun `containsAll should pass when all elements exist`() {
        val data = TestData(arrayOf("a", "b", "c", "d"))
        val required = arrayOf("a", "c")
        assertTrue(required.all { it in data.items })
    }

    @Test
    fun `containsAll should fail when any element missing`() {
        val data = TestData(arrayOf("a", "b"))
        val required = arrayOf("a", "z")
        assertFalse(required.all { it in data.items })
    }

    @Test
    fun `containsAny should pass when at least one element exists`() {
        val data = TestData(arrayOf("a", "b", "c"))
        val options = arrayOf("x", "b", "z")
        assertTrue(options.any { it in data.items })
    }

    @Test
    fun `containsAny should fail when no element exists`() {
        val data = TestData(arrayOf("a", "b"))
        val options = arrayOf("x", "y", "z")
        assertFalse(options.any { it in data.items })
    }
}
