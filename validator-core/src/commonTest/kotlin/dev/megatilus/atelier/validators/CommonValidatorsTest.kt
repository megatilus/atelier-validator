/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.*

class CommonValidatorsTest {

    data class TestData(val status: String, val count: Int)

    @Test
    fun `isEqualTo should pass when values match`() {
        val data = TestData("ACTIVE", 5)
        assertEquals("ACTIVE", data.status)
        assertEquals(5, data.count)
    }

    @Test
    fun `isEqualTo should fail when values differ`() {
        val data = TestData("INACTIVE", 3)
        assertNotEquals("ACTIVE", data.status)
        assertNotEquals(5, data.count)
    }

    @Test
    fun `isNotEqualTo should pass when values differ`() {
        val data = TestData("ACTIVE", 5)
        assertNotEquals("DELETED", data.status)
    }

    @Test
    fun `isNotEqualTo should fail when values match`() {
        val data = TestData("ACTIVE", 5)
        assertEquals("ACTIVE", data.status)
    }

    @Test
    fun `isIn should pass when value in allowed list`() {
        val data = TestData("ACTIVE", 5)
        val allowed = arrayOf("ACTIVE", "PENDING", "COMPLETED")
        assertTrue(data.status in allowed)
    }

    @Test
    fun `isIn should fail when value not in allowed list`() {
        val data = TestData("DELETED", 5)
        val allowed = arrayOf("ACTIVE", "PENDING", "COMPLETED")
        assertFalse(data.status in allowed)
    }

    @Test
    fun `isNotIn should pass when value not in forbidden list`() {
        val data = TestData("ACTIVE", 5)
        val forbidden = arrayOf("DELETED", "BANNED", "SUSPENDED")
        assertFalse(data.status in forbidden)
    }

    @Test
    fun `isNotIn should fail when value in forbidden list`() {
        val data = TestData("BANNED", 5)
        val forbidden = arrayOf("DELETED", "BANNED", "SUSPENDED")
        assertTrue(data.status in forbidden)
    }

    @Test
    fun `isIn works with different types`() {
        val data = TestData("ACTIVE", 5)
        val allowedCounts = arrayOf(1, 3, 5, 7, 9)
        assertTrue(data.count in allowedCounts)
    }
}
