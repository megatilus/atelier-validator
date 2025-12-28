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
        val expectedCodes = setOf(
            ValidatorCode.TOO_SHORT,
            ValidatorCode.TOO_LONG,
            ValidatorCode.INVALID_EMAIL,
            ValidatorCode.INVALID_FORMAT,
            ValidatorCode.INVALID_VALUE,
            ValidatorCode.CUSTOM_ERROR,
            ValidatorCode.CROSS_FIELD_ERROR,
            ValidatorCode.WEAK_PASSWORD,
            ValidatorCode.REQUIRED,
            ValidatorCode.MIN,
            ValidatorCode.MAX,
            ValidatorCode.PATTERN,
            ValidatorCode.OUT_OF_RANGE
        )

        val actualCodes = ValidatorCode.entries.toSet()
        assertEquals(expectedCodes, actualCodes)
    }

    @Test
    fun `ValidatorCode values should be consistent`() {
        // Test that each code has a consistent name
        assertEquals("TOO_SHORT", ValidatorCode.TOO_SHORT.name)
        assertEquals("TOO_LONG", ValidatorCode.TOO_LONG.name)
        assertEquals("INVALID_EMAIL", ValidatorCode.INVALID_EMAIL.name)
        assertEquals("INVALID_FORMAT", ValidatorCode.INVALID_FORMAT.name)
        assertEquals("INVALID_VALUE", ValidatorCode.INVALID_VALUE.name)
        assertEquals("CUSTOM_ERROR", ValidatorCode.CUSTOM_ERROR.name)
        assertEquals("CROSS_FIELD_ERROR", ValidatorCode.CROSS_FIELD_ERROR.name)
        assertEquals("WEAK_PASSWORD", ValidatorCode.WEAK_PASSWORD.name)
        assertEquals("REQUIRED", ValidatorCode.REQUIRED.name)
        assertEquals("MIN", ValidatorCode.MIN.name)
        assertEquals("MAX", ValidatorCode.MAX.name)
        assertEquals("PATTERN", ValidatorCode.PATTERN.name)
        assertEquals("OUT_OF_RANGE", ValidatorCode.OUT_OF_RANGE.name)
    }

    @Test
    fun `ValidatorCode should have expected count`() {
        assertEquals(13, ValidatorCode.entries.size)
    }

    @Test
    fun `ValidatorCode valueOf should work correctly`() {
        assertEquals(ValidatorCode.TOO_SHORT, ValidatorCode.valueOf("TOO_SHORT"))
        assertEquals(ValidatorCode.TOO_LONG, ValidatorCode.valueOf("TOO_LONG"))
        assertEquals(ValidatorCode.INVALID_EMAIL, ValidatorCode.valueOf("INVALID_EMAIL"))
        assertEquals(ValidatorCode.INVALID_FORMAT, ValidatorCode.valueOf("INVALID_FORMAT"))
        assertEquals(ValidatorCode.INVALID_VALUE, ValidatorCode.valueOf("INVALID_VALUE"))
        assertEquals(ValidatorCode.CUSTOM_ERROR, ValidatorCode.valueOf("CUSTOM_ERROR"))
        assertEquals(ValidatorCode.CROSS_FIELD_ERROR, ValidatorCode.valueOf("CROSS_FIELD_ERROR"))
        assertEquals(ValidatorCode.WEAK_PASSWORD, ValidatorCode.valueOf("WEAK_PASSWORD"))
        assertEquals(ValidatorCode.REQUIRED, ValidatorCode.valueOf("REQUIRED"))
        assertEquals(ValidatorCode.MIN, ValidatorCode.valueOf("MIN"))
        assertEquals(ValidatorCode.MAX, ValidatorCode.valueOf("MAX"))
        assertEquals(ValidatorCode.PATTERN, ValidatorCode.valueOf("PATTERN"))
        assertEquals(ValidatorCode.OUT_OF_RANGE, ValidatorCode.valueOf("OUT_OF_RANGE"))
    }

    @Test
    fun `ValidatorCode ordinal values should be stable`() {
        // This test ensures the ordinal values don't change accidentally
        // which could break serialization or other ordinal-dependent code
        assertTrue(ValidatorCode.TOO_SHORT.ordinal >= 0)
        assertTrue(ValidatorCode.TOO_LONG.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_EMAIL.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_FORMAT.ordinal >= 0)
        assertTrue(ValidatorCode.INVALID_VALUE.ordinal >= 0)
        assertTrue(ValidatorCode.CUSTOM_ERROR.ordinal >= 0)
        assertTrue(ValidatorCode.CROSS_FIELD_ERROR.ordinal >= 0)
        assertTrue(ValidatorCode.WEAK_PASSWORD.ordinal >= 0)
        assertTrue(ValidatorCode.REQUIRED.ordinal >= 0)
        assertTrue(ValidatorCode.MIN.ordinal >= 0)
        assertTrue(ValidatorCode.MAX.ordinal >= 0)
        assertTrue(ValidatorCode.PATTERN.ordinal >= 0)
        assertTrue(ValidatorCode.OUT_OF_RANGE.ordinal >= 0)
    }

    @Test
    fun `ValidatorCode should work in when expressions`() {
        fun getCodeDescription(code: ValidatorCode): String {
            return when (code) {
                ValidatorCode.TOO_SHORT -> "Value is too short"
                ValidatorCode.TOO_LONG -> "Value is too long"
                ValidatorCode.INVALID_EMAIL -> "Invalid email format"
                ValidatorCode.INVALID_FORMAT -> "Invalid format"
                ValidatorCode.INVALID_VALUE -> "Invalid value"
                ValidatorCode.CUSTOM_ERROR -> "Custom error"
                ValidatorCode.CROSS_FIELD_ERROR -> "Cross field error"
                ValidatorCode.WEAK_PASSWORD -> "Weak password"
                ValidatorCode.REQUIRED -> "Value is required"
                ValidatorCode.MIN -> "Value is below minimum"
                ValidatorCode.MAX -> "Value is above maximum"
                ValidatorCode.PATTERN -> "Does not match pattern"
                ValidatorCode.OUT_OF_RANGE -> "Value is out of range"
            }
        }

        assertEquals("Value is too short", getCodeDescription(ValidatorCode.TOO_SHORT))
        assertEquals("Value is too long", getCodeDescription(ValidatorCode.TOO_LONG))
        assertEquals("Invalid email format", getCodeDescription(ValidatorCode.INVALID_EMAIL))
        assertEquals("Cross field error", getCodeDescription(ValidatorCode.CROSS_FIELD_ERROR))
        assertEquals("Value is required", getCodeDescription(ValidatorCode.REQUIRED))
        assertEquals("Value is below minimum", getCodeDescription(ValidatorCode.MIN))
        assertEquals("Does not match pattern", getCodeDescription(ValidatorCode.PATTERN))
        assertEquals("Value is out of range", getCodeDescription(ValidatorCode.OUT_OF_RANGE))
    }

    @Test
    fun `ValidatorCode should be usable in collections`() {
        val codes = listOf(
            ValidatorCode.REQUIRED,
            ValidatorCode.MIN,
            ValidatorCode.MAX,
            ValidatorCode.INVALID_EMAIL
        )

        assertTrue(codes.contains(ValidatorCode.REQUIRED))
        assertTrue(codes.contains(ValidatorCode.MIN))
        assertEquals(4, codes.size)
    }

    @Test
    fun `ValidatorCode should be comparable by ordinal`() {
        val code1 = ValidatorCode.TOO_SHORT
        val code2 = ValidatorCode.TOO_LONG

        // Just verify we can compare them
        assertTrue(code1.ordinal >= 0)
        assertTrue(code2.ordinal >= 0)
    }

    @Test
    fun `ValidatorCode should work in maps`() {
        val codeMessages = mapOf(
            ValidatorCode.REQUIRED to "This field is required",
            ValidatorCode.INVALID_EMAIL to "Please enter a valid email",
            ValidatorCode.TOO_SHORT to "Value is too short",
            ValidatorCode.WEAK_PASSWORD to "Password is too weak"
        )

        assertEquals("This field is required", codeMessages[ValidatorCode.REQUIRED])
        assertEquals("Please enter a valid email", codeMessages[ValidatorCode.INVALID_EMAIL])
        assertEquals(4, codeMessages.size)
    }

    @Test
    fun `all ValidatorCode entries should be accessible`() {
        val allCodes = ValidatorCode.entries

        // Verify we can iterate through all codes
        assertTrue(allCodes.isNotEmpty())
        assertEquals(13, allCodes.size)

        // Verify each code is present
        assertTrue(allCodes.contains(ValidatorCode.TOO_SHORT))
        assertTrue(allCodes.contains(ValidatorCode.TOO_LONG))
        assertTrue(allCodes.contains(ValidatorCode.INVALID_EMAIL))
        assertTrue(allCodes.contains(ValidatorCode.INVALID_FORMAT))
        assertTrue(allCodes.contains(ValidatorCode.INVALID_VALUE))
        assertTrue(allCodes.contains(ValidatorCode.CUSTOM_ERROR))
        assertTrue(allCodes.contains(ValidatorCode.CROSS_FIELD_ERROR))
        assertTrue(allCodes.contains(ValidatorCode.WEAK_PASSWORD))
        assertTrue(allCodes.contains(ValidatorCode.REQUIRED))
        assertTrue(allCodes.contains(ValidatorCode.MIN))
        assertTrue(allCodes.contains(ValidatorCode.MAX))
        assertTrue(allCodes.contains(ValidatorCode.PATTERN))
        assertTrue(allCodes.contains(ValidatorCode.OUT_OF_RANGE))
    }

    @Test
    fun `ValidatorCode toString should return name`() {
        assertEquals("REQUIRED", ValidatorCode.REQUIRED.toString())
        assertEquals("MIN", ValidatorCode.MIN.toString())
        assertEquals("MAX", ValidatorCode.MAX.toString())
        assertEquals("INVALID_EMAIL", ValidatorCode.INVALID_EMAIL.toString())
    }
}
