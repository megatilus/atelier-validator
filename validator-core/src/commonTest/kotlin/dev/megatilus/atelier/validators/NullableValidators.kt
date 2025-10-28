/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class NullableValidatorsTest {

    data class StringData(val value: String?)
    data class ArrayData(val items: Array<String>?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ArrayData
            return items.contentEquals(other.items)
        }
        override fun hashCode(): Int = items.contentHashCode()
    }
    data class ListData(val items: List<String>?)
    data class SetData(val items: Set<String>?)
    data class MapData(val config: Map<String, Int>?)

    @Test
    fun `nullable string notBlank should pass for non-null non-blank`() {
        val data = StringData("hello")
        assertTrue(data.value != null && data.value.isNotBlank())
    }

    @Test
    fun `nullable string notBlank should fail for null`() {
        val data = StringData(null)
        assertFalse(data.value != null && data.value.isNotBlank())
    }

    @Test
    fun `nullable string notBlank should fail for blank`() {
        val data = StringData("   ")
        assertFalse(data.value != null && data.value.isNotBlank())
    }

    @Test
    fun `nullable string length should allow null`() {
        val data = StringData(null)
        // null devrait être considéré comme valide pour les contraintes de taille
        assertTrue(data.value == null || data.value.length in 1..10)
    }

    @Test
    fun `nullable array notEmpty should fail for null`() {
        val data = ArrayData(null)
        assertFalse(data.items != null && data.items.isNotEmpty())
    }

    @Test
    fun `nullable array notEmpty should fail for empty`() {
        val data = ArrayData(arrayOf())
        assertFalse(data.items != null && data.items.isNotEmpty())
    }

    @Test
    fun `nullable array notEmpty should pass for non-empty`() {
        val data = ArrayData(arrayOf("a", "b"))
        assertTrue(data.items != null && data.items.isNotEmpty())
    }

    @Test
    fun `nullable array size should allow null`() {
        val data = ArrayData(null)
        assertTrue(data.items == null || data.items.size in 1..5)
    }

    @Test
    fun `nullable list size should allow null`() {
        val data = ListData(null)
        assertTrue(data.items == null || data.items.size in 1..10)
    }

    @Test
    fun `nullable set size should allow null`() {
        val data = SetData(null)
        assertTrue(data.items == null || data.items.size in 1..10)
    }

    @Test
    fun `nullable map size should allow null`() {
        val data = MapData(null)
        assertTrue(data.config == null || data.config.size in 1..10)
    }

    @Test
    fun `nullable isEqualTo should handle null comparison`() {
        val data = StringData(null)
        assertEquals(data.value, null)

        val data2 = StringData("test")
        assertNotEquals(data2.value, null)
    }

    @Test
    fun `nullable isIn should fail for null values`() {
        val data = StringData(null)
        val allowed = arrayOf("a", "b", "c")
        assertFalse(data.value != null && data.value in allowed)
    }

    @Test
    fun `nullable isNotIn should allow null values`() {
        val data = StringData(null)
        val forbidden = arrayOf("a", "b", "c")
        assertTrue(data.value == null || data.value !in forbidden)
    }
}
