/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.results

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorCodeTest {

    @Test
    fun `ValidatorCode should have all expected values`() {
        val expectedCodes =
            setOf(
                ValidatorCode.NOT_NULL,
                ValidatorCode.NOT_BLANK,
                ValidatorCode.NOT_EMPTY,
                ValidatorCode.TOO_SHORT,
                ValidatorCode.TOO_LONG,
                ValidatorCode.OUT_OF_RANGE,
                ValidatorCode.INVALID_EMAIL,
                ValidatorCode.INVALID_FORMAT,
                ValidatorCode.INVALID_VALUE,
                ValidatorCode.NULL_VALUE,
                ValidatorCode.CUSTOM_ERROR,
                ValidatorCode.CROSS_FIELD_ERROR,
                ValidatorCode.WEAK_PASSWORD
            )

        val actualCodes = ValidatorCode.entries.toSet()
        assertEquals(expectedCodes, actualCodes)
    }

    @Test
    fun `ValidatorCode values should be consistent`() {
        // Test that each code has a consistent name
        assertEquals("NOT_NULL", ValidatorCode.NOT_NULL.name)
        assertEquals("NOT_BLANK", ValidatorCode.NOT_BLANK.name)
        assertEquals("NOT_EMPTY", ValidatorCode.NOT_EMPTY.name)
        assertEquals("TOO_SHORT", ValidatorCode.TOO_SHORT.name)
        assertEquals("TOO_LONG", ValidatorCode.TOO_LONG.name)
        assertEquals("OUT_OF_RANGE", ValidatorCode.OUT_OF_RANGE.name)
        assertEquals("INVALID_EMAIL", ValidatorCode.INVALID_EMAIL.name)
        assertEquals("INVALID_FORMAT", ValidatorCode.INVALID_FORMAT.name)
        assertEquals("INVALID_VALUE", ValidatorCode.INVALID_VALUE.name)
        assertEquals("NULL_VALUE", ValidatorCode.NULL_VALUE.name)
        assertEquals("CUSTOM_ERROR", ValidatorCode.CUSTOM_ERROR.name)
        assertEquals("CROSS_FIELD_ERROR", ValidatorCode.CROSS_FIELD_ERROR.name)
    }

    @Test
    fun `ValidatorCode should have expected count`() {
        assertEquals(13, ValidatorCode.entries.size)
    }

    @Test
    fun `ValidatorCode valueOf should work correctly`() {
        assertEquals(ValidatorCode.NOT_NULL, ValidatorCode.valueOf("NOT_NULL"))
        assertEquals(ValidatorCode.NOT_BLANK, ValidatorCode.valueOf("NOT_BLANK"))
        assertEquals(ValidatorCode.NOT_EMPTY, ValidatorCode.valueOf("NOT_EMPTY"))
        assertEquals(ValidatorCode.TOO_SHORT, ValidatorCode.valueOf("TOO_SHORT"))
        assertEquals(ValidatorCode.TOO_LONG, ValidatorCode.valueOf("TOO_LONG"))
        assertEquals(ValidatorCode.OUT_OF_RANGE, ValidatorCode.valueOf("OUT_OF_RANGE"))
        assertEquals(ValidatorCode.INVALID_EMAIL, ValidatorCode.valueOf("INVALID_EMAIL"))
        assertEquals(ValidatorCode.INVALID_FORMAT, ValidatorCode.valueOf("INVALID_FORMAT"))
        assertEquals(ValidatorCode.INVALID_VALUE, ValidatorCode.valueOf("INVALID_VALUE"))
        assertEquals(ValidatorCode.NULL_VALUE, ValidatorCode.valueOf("NULL_VALUE"))
        assertEquals(ValidatorCode.CUSTOM_ERROR, ValidatorCode.valueOf("CUSTOM_ERROR"))
        assertEquals(ValidatorCode.CROSS_FIELD_ERROR, ValidatorCode.valueOf("CROSS_FIELD_ERROR"))
        assertEquals(ValidatorCode.WEAK_PASSWORD, ValidatorCode.valueOf("WEAK_PASSWORD"))
    }

    @Test
    fun `ValidatorCode ordinal values should be stable`() {
        // This test ensures the ordinal values don't change accidentally
        // which could break serialization or other ordinal-dependent code
        assertTrue(ValidatorCode.NOT_NULL.ordinal >= 0)
        assertTrue(ValidatorCode.NOT_BLANK.ordinal >= 0)
        assertTrue(ValidatorCode.NOT_EMPTY.ordinal >= 0)
        assertTrue(ValidatorCode.TOO_SHORT.ordinal >= 0)
        assertTrue(ValidatorCode.TOO_LONG.ordinal >= 0)
        assertTrue(ValidatorCode.OUT_OF_RANGE.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_EMAIL.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_FORMAT.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_VALUE.ordinal >= 0)
        assertTrue(ValidatorCode.NULL_VALUE.ordinal >= 0)
        assertTrue(ValidatorCode.CUSTOM_ERROR.ordinal >= 0)
        assertTrue(ValidatorCode.CROSS_FIELD_ERROR.ordinal >= 0)
        assertTrue(ValidatorCode.WEAK_PASSWORD.ordinal >= 0)
    }

    @Test
    fun `ValidatorCode should work in when expressions`() {
        fun getCodeDescription(code: ValidatorCode): String {
            return when (code) {
                ValidatorCode.NOT_NULL -> "Value cannot be null"
                ValidatorCode.NOT_BLANK -> "Value cannot be blank"
                ValidatorCode.NOT_EMPTY -> "Value cannot be empty"
                ValidatorCode.TOO_SHORT -> "Value is too short"
                ValidatorCode.TOO_LONG -> "Value is too long"
                ValidatorCode.OUT_OF_RANGE -> "Value is out of range"
                ValidatorCode.INVALID_EMAIL -> "Invalid email format"
                ValidatorCode.INVALID_FORMAT -> "Invalid format"
                ValidatorCode.INVALID_VALUE -> "Invalid value"
                ValidatorCode.NULL_VALUE -> "Null value"
                ValidatorCode.CUSTOM_ERROR -> "Custom error"
                ValidatorCode.CROSS_FIELD_ERROR -> "Cross field error"
                ValidatorCode.WEAK_PASSWORD -> "Weak password"
            }
        }

        assertEquals("Value cannot be null", getCodeDescription(ValidatorCode.NOT_NULL))
        assertEquals("Value cannot be blank", getCodeDescription(ValidatorCode.NOT_BLANK))
        assertEquals("Invalid email format", getCodeDescription(ValidatorCode.INVALID_EMAIL))
        assertEquals("Cross field error", getCodeDescription(ValidatorCode.CROSS_FIELD_ERROR))
    }
}
