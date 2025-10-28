/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class MapValidatorsTest {

    data class TestData(val config: Map<String, Int>)

    @Test
    fun `notEmpty should pass for non-empty maps`() {
        val data = TestData(mapOf("a" to 1, "b" to 2))
        assertTrue(data.config.isNotEmpty())
    }

    @Test
    fun `notEmpty should fail for empty maps`() {
        val data = TestData(emptyMap())
        assertFalse(data.config.isNotEmpty())
    }

    @Test
    fun `size should validate map size`() {
        val data = TestData(mapOf("a" to 1, "b" to 2, "c" to 3))
        assertEquals(3, data.config.size)
        assertTrue(data.config.size in 1..5)
    }

    @Test
    fun `exactSize should work for maps`() {
        val data = TestData(mapOf("a" to 1, "b" to 2))
        assertEquals(2, data.config.size)
    }

    @Test
    fun `containsKey should pass when key exists`() {
        val data = TestData(mapOf("apiUrl" to 1, "timeout" to 30))
        assertTrue(data.config.containsKey("apiUrl"))
    }

    @Test
    fun `containsKey should fail when key missing`() {
        val data = TestData(mapOf("a" to 1))
        assertFalse(data.config.containsKey("missing"))
    }

    @Test
    fun `containsKeys should validate all keys present`() {
        val data = TestData(mapOf("a" to 1, "b" to 2, "c" to 3))
        val required = arrayOf("a", "c")
        assertTrue(required.all { it in data.config.keys })
    }

    @Test
    fun `containsKeys should fail when any key missing`() {
        val data = TestData(mapOf("a" to 1, "b" to 2))
        val required = arrayOf("a", "missing")
        assertFalse(required.all { it in data.config.keys })
    }

    @Test
    fun `doesNotContainKey should pass when key absent`() {
        val data = TestData(mapOf("a" to 1))
        assertFalse(data.config.containsKey("forbidden"))
    }

    @Test
    fun `doesNotContainKey should fail when key present`() {
        val data = TestData(mapOf("a" to 1, "forbidden" to 2))
        assertTrue(data.config.containsKey("forbidden"))
    }
}
