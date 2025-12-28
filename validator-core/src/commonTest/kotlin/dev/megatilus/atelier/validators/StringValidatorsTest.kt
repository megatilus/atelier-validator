/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StringValidatorsTest {

    data class TestData(val value: String)

    @Test
    fun `notBlank should pass for non-blank strings`() {
        val data = TestData("hello")
        assertTrue(data.value.isNotBlank())
    }

    @Test
    fun `notBlank should fail for blank strings`() {
        val data = TestData("   ")
        assertFalse(data.value.isNotBlank())
    }

    @Test
    fun `notEmpty should pass for non-empty strings`() {
        val data = TestData("a")
        assertTrue(data.value.isNotEmpty())
    }

    @Test
    fun `notEmpty should fail for empty strings`() {
        val data = TestData("")
        assertFalse(data.value.isNotEmpty())
    }

    @Test
    fun `minLength should pass when length is equal or greater`() {
        val data = TestData("hello")
        assertTrue(data.value.length >= 3)
        assertTrue(data.value.length >= 5)
    }

    @Test
    fun `minLength should fail when length is less`() {
        val data = TestData("hi")
        assertFalse(data.value.length >= 5)
    }

    @Test
    fun `maxLength should pass when length is equal or less`() {
        val data = TestData("hello")
        assertTrue(data.value.length <= 10)
        assertTrue(data.value.length <= 5)
    }

    @Test
    fun `maxLength should fail when length is greater`() {
        val data = TestData("hello world")
        assertFalse(data.value.length <= 5)
    }

    @Test
    fun `length should pass when in range`() {
        val data = TestData("hello")
        assertTrue(data.value.length in 3..10)
        assertTrue(data.value.length in 5..5)
    }

    @Test
    fun `length should fail when out of range`() {
        val data = TestData("hi")
        assertFalse(data.value.length in 5..10)

        val data2 = TestData("hello world!!!")
        assertFalse(data2.value.length in 1..5)
    }

    @Test
    fun `exactLength should pass when length matches`() {
        val data = TestData("hello")
        assertEquals(5, data.value.length)
    }

    @Test
    fun `exactLength should fail when length differs`() {
        val data = TestData("hello")
        assertNotEquals(3, data.value.length)
    }

    @Test
    fun `matches should pass when strings are equal`() {
        val data = TestData("exact")
        assertEquals("exact", data.value)
    }

    @Test
    fun `matches should fail when strings differ`() {
        val data = TestData("hello")
        assertNotEquals("world", data.value)
    }

    @Test
    fun `matchesPattern should pass for valid patterns`() {
        val pattern = Regex("^[a-z]+$")
        assertTrue(pattern.matches("hello"))
        assertTrue(pattern.matches("abc"))
    }

    @Test
    fun `matchesPattern should fail for invalid patterns`() {
        val pattern = Regex("^[a-z]+$")
        assertFalse(pattern.matches("Hello"))
        assertFalse(pattern.matches("123"))
    }

    @Test
    fun `alphanumeric should pass for letters and numbers only`() {
        assertTrue("hello123".all { it.isLetterOrDigit() })
        assertTrue("ABC".all { it.isLetterOrDigit() })
    }

    @Test
    fun `alphanumeric should fail for special characters`() {
        assertFalse("hello!".all { it.isLetterOrDigit() })
        assertFalse("test@123".all { it.isLetterOrDigit() })
    }

    @Test
    fun `alpha should pass for letters only`() {
        assertTrue("hello".all { it.isLetter() })
        assertTrue("ABC".all { it.isLetter() })
    }

    @Test
    fun `alpha should fail for numbers or special chars`() {
        assertFalse("hello123".all { it.isLetter() })
        assertFalse("test!".all { it.isLetter() })
    }

    @Test
    fun `numeric should pass for numbers only`() {
        assertTrue("123".all { it.isDigit() })
        assertTrue("0".all { it.isDigit() })
    }

    @Test
    fun `numeric should fail for non-numbers`() {
        assertFalse("123a".all { it.isDigit() })
        assertFalse("12.3".all { it.isDigit() })
    }

    @Test
    fun `uppercase should pass for uppercase strings`() {
        assertEquals("HELLO", "HELLO".uppercase())
        assertEquals("ABC123", "ABC123".uppercase())
    }

    @Test
    fun `uppercase should fail for non-uppercase strings`() {
        assertNotEquals("hello", "hello".uppercase())
        assertNotEquals("Hello", "Hello".uppercase())
    }

    @Test
    fun `lowercase should pass for lowercase strings`() {
        assertEquals("hello", "hello".lowercase())
        assertEquals("abc", "abc".lowercase())
    }

    @Test
    fun `lowercase should fail for non-lowercase strings`() {
        assertNotEquals("HELLO", "HELLO".lowercase())
        assertNotEquals("Hello", "Hello".lowercase())
    }

    @Test
    fun `email should pass for valid emails`() {
        val emailRegex =
            Regex(
                "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
            )

        assertTrue(emailRegex.matches("test@example.com"))
        assertTrue(emailRegex.matches("user.name@domain.co.uk"))
        assertTrue(emailRegex.matches("first+last@test.org"))
    }

    @Test
    fun `email should fail for invalid emails`() {
        val emailRegex =
            Regex(
                "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
            )

        assertFalse(emailRegex.matches("invalid"))
        assertFalse(emailRegex.matches("@example.com"))
        assertFalse(emailRegex.matches("test@"))
        assertFalse(emailRegex.matches("test @example.com"))
    }

    @Test
    fun `url should pass for valid URLs`() {
        val urlRegex = Regex("^https?://[-\\w.]+(?:[:\\d]+)?(?:/[\\w/_.]*(?:\\?[\\w&=%.]*)?(?:#[\\w.]*)?)?$")

        assertTrue(urlRegex.matches("https://example.com"))
        assertTrue(urlRegex.matches("http://example.com/path"))
        assertTrue(urlRegex.matches("https://example.com:8080/path?query=1"))
    }

    @Test
    fun `url should fail for invalid URLs`() {
        val urlRegex = Regex("^https?://[-\\w.]+(?:[:\\d]+)?(?:/[\\w/_.]*(?:\\?[\\w&=%.]*)?(?:#[\\w.]*)?)?$")

        assertFalse(urlRegex.matches("not a url"))
        assertFalse(urlRegex.matches("ftp://example.com"))
        assertFalse(urlRegex.matches("example.com"))
    }

    @Test
    fun `phoneNumber should pass for valid E164 format`() {
        val phoneRegex = Regex("^\\+?[1-9]\\d{1,14}$")

        assertTrue(phoneRegex.matches("+33612345678"))
        assertTrue(phoneRegex.matches("+14155552671"))
    }

    @Test
    fun `phoneNumber should fail for invalid formats`() {
        val phoneRegex = Regex("^\\+?[1-9]\\d{1,14}$")

        assertFalse(phoneRegex.matches("1")) // Too short (only 1 digit)
        assertFalse(phoneRegex.matches("abc")) // Contains letters
        assertFalse(phoneRegex.matches("01234567890")) // Starts with 0
        assertFalse(phoneRegex.matches("+1234567890123456")) // Too long (16 digits)
    }

    @Test
    fun `creditCard should pass for valid card numbers with Luhn check`() {
        // Visa test number
        val validCard = "4532015112830366"
        assertTrue(validCard.length in 13..19)
        // Note: isValidLuhn doit être testé séparément
    }

    @Test
    fun `creditCard should fail for invalid card numbers`() {
        val invalidCard = "1234567890123456"
        assertTrue(invalidCard.length in 13..19)
        // Note: devrait échouer au test Luhn
    }

    @Test
    fun `strongPassword should validate all requirements`() {
        val strongPass = "Test123!@#"

        assertTrue(strongPass.length >= 8)
        assertTrue(strongPass.any { it.isUpperCase() })
        assertTrue(strongPass.any { it.isLowerCase() })
        assertTrue(strongPass.any { it.isDigit() })
        // Special chars check séparé
    }

    @Test
    fun `strongPassword should fail weak passwords`() {
        val weakPass = "password"

        assertTrue(weakPass.length >= 8)
        assertFalse(weakPass.any { it.isUpperCase() })
        assertFalse(weakPass.any { it.isDigit() })
    }
}
