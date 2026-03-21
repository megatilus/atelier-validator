/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.AtelierValidator
import dev.megatilus.atelier.results.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharSequenceRulesTest {

    data class TestEntity(
        val name: String? = null,
        val email: String? = null,
        val password: String? = null,
        val url: String? = null,
        val uuid: String? = null,
        val ipv4: String? = null,
        val ipv6: String? = null,
        val username: String? = null,
        val code: String? = null,
        val sku: String? = null
    )

    @Test
    fun `notBlank should succeed for non-blank string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank() hint "Name cannot be blank"
            }
        }

        assertTrue(validator.validate(TestEntity(name = "John")) is ValidationResult.Success)
    }

    @Test
    fun `notBlank should fail for blank string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "   ")) is ValidationResult.Failure)
    }

    @Test
    fun `notBlank should fail for empty string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "")) is ValidationResult.Failure)
    }

    @Test
    fun `notBlank should fail for null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notBlank()
            }
        }

        assertTrue(validator.validate(TestEntity(name = null)) is ValidationResult.Failure)
    }

    @Test
    fun `notEmpty should succeed for non-empty string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "John")) is ValidationResult.Success)
    }

    @Test
    fun `notEmpty should succeed for whitespace-only string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "   ")) is ValidationResult.Success)
    }

    @Test
    fun `notEmpty should fail for empty string`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "")) is ValidationResult.Failure)
    }

    @Test
    fun `notEmpty should fail for null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                notEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(name = null)) is ValidationResult.Failure)
    }

    @Test
    fun `length should succeed when in range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                length(2..10)
            }
        }

        assertTrue(validator.validate(TestEntity(name = "Jo")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(name = "John")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(name = "1234567890")) is ValidationResult.Success)
    }

    @Test
    fun `length should fail when out of range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                length(2..10)
            }
        }

        assertTrue(validator.validate(TestEntity(name = "J")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(name = "12345678901")) is ValidationResult.Failure)
    }

    @Test
    fun `minLength should succeed when long enough`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                minLength(8)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "password123")) is ValidationResult.Success)
    }

    @Test
    fun `minLength should fail when too short`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                minLength(8)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "pass")) is ValidationResult.Failure)
    }

    @Test
    fun `maxLength should succeed when short enough`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                maxLength(50)
            }
        }

        assertTrue(validator.validate(TestEntity(name = "John")) is ValidationResult.Success)
    }

    @Test
    fun `maxLength should fail when too long`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                maxLength(5)
            }
        }

        assertTrue(validator.validate(TestEntity(name = "Jonathan")) is ValidationResult.Failure)
    }

    @Test
    fun `exactLength should succeed when length matches`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                exactLength(5)
            }
        }

        assertTrue(validator.validate(TestEntity(code = "ABC12")) is ValidationResult.Success)
    }

    @Test
    fun `exactLength should fail when length differs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                exactLength(5)
            }
        }

        assertTrue(validator.validate(TestEntity(code = "ABC")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(code = "ABC1234")) is ValidationResult.Failure)
    }

    @Test
    fun `email should succeed for valid emails`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                email()
            }
        }

        assertTrue(validator.validate(TestEntity(email = "test@example.com")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(email = "user+tag@example.co.uk")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(email = "first.last@example.com")) is ValidationResult.Success)
    }

    @Test
    fun `email should fail for invalid emails`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                email()
            }
        }

        assertTrue(validator.validate(TestEntity(email = "invalid-email")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(email = "@example.com")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(email = "test@")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(email = "test @example.com")) is ValidationResult.Failure)
    }

    @Test
    fun `url should succeed for valid URLs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::url {
                url()
            }
        }

        assertTrue(validator.validate(TestEntity(url = "https://example.com")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(url = "http://example.com/path?query=1")) is ValidationResult.Success)
        assertTrue(
            validator.validate(TestEntity(url = "https://sub.example.com:8080/path")) is ValidationResult.Success
        )
    }

    @Test
    fun `url should fail for invalid URLs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::url {
                url()
            }
        }

        assertTrue(validator.validate(TestEntity(url = "not-a-url")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(url = "ftp://example.com")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(url = "example.com")) is ValidationResult.Failure)
    }

    @Test
    fun `uuid should succeed for valid UUIDs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::uuid {
                uuid()
            }
        }

        assertTrue(
            validator.validate(TestEntity(uuid = "550e8400-e29b-41d4-a716-446655440000")) is ValidationResult.Success
        )
        assertTrue(
            validator.validate(TestEntity(uuid = "123e4567-e89b-12d3-a456-426614174000")) is ValidationResult.Success
        )
    }

    @Test
    fun `uuid should fail for invalid UUIDs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::uuid {
                uuid()
            }
        }

        assertTrue(validator.validate(TestEntity(uuid = "invalid-uuid")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(uuid = "550e8400-e29b-41d4-a716")) is ValidationResult.Failure)
        assertTrue(
            validator.validate(TestEntity(uuid = "550e8400e29b41d4a716446655440000")) is ValidationResult.Failure
        )
    }

    @Test
    fun `ipv4 should succeed for valid IPv4 addresses`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::ipv4 {
                ipv4()
            }
        }

        assertTrue(validator.validate(TestEntity(ipv4 = "192.168.1.1")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(ipv4 = "0.0.0.0")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(ipv4 = "255.255.255.255")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(ipv4 = "10.0.0.1")) is ValidationResult.Success)
    }

    @Test
    fun `ipv4 should fail for invalid IPv4 addresses`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::ipv4 {
                ipv4()
            }
        }

        assertTrue(validator.validate(TestEntity(ipv4 = "256.1.1.1")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(ipv4 = "192.168.1")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(ipv4 = "192.168.1.1.1")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(ipv4 = "invalid")) is ValidationResult.Failure)
    }

    @Test
    fun `ipv6 should succeed for valid IPv6 addresses`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::ipv6 {
                ipv6()
            }
        }

        assertTrue(
            validator.validate(TestEntity(ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334")) is ValidationResult.Success
        )
        assertTrue(validator.validate(TestEntity(ipv6 = "2001:db8:85a3::8a2e:370:7334")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(ipv6 = "::1")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(ipv6 = "fe80::1")) is ValidationResult.Success)
    }

    @Test
    fun `ipv6 should fail for invalid IPv6 addresses`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::ipv6 {
                ipv6()
            }
        }

        assertTrue(validator.validate(TestEntity(ipv6 = "192.168.1.1")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(ipv6 = "invalid")) is ValidationResult.Failure)
        assertTrue(
            validator.validate(
                TestEntity(ipv6 = "02001:0db8:0000:0000:0000:0000:0000:0001")
            ) is ValidationResult.Failure
        )
    }

    @Test
    fun `strongPassword should succeed for valid strong password`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(
                    minLength = 8,
                    requireUppercase = true,
                    requireLowercase = true,
                    requireDigit = true,
                    requireSpecialChar = true
                )
            }
        }

        assertTrue(validator.validate(TestEntity(password = "Passw0rd!")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(password = "MyP@ssw0rd")) is ValidationResult.Success)
    }

    @Test
    fun `strongPassword should fail when missing uppercase`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(requireUppercase = true)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "password1!")) is ValidationResult.Failure)
    }

    @Test
    fun `strongPassword should fail when missing lowercase`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(requireLowercase = true)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "PASSWORD1!")) is ValidationResult.Failure)
    }

    @Test
    fun `strongPassword should fail when missing digit`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(requireDigit = true)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "Password!")) is ValidationResult.Failure)
    }

    @Test
    fun `strongPassword should fail when missing special char`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(requireSpecialChar = true)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "Password1")) is ValidationResult.Failure)
    }

    @Test
    fun `strongPassword should fail when too short`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                strongPassword(minLength = 12)
            }
        }

        assertTrue(validator.validate(TestEntity(password = "Pass1!")) is ValidationResult.Failure)
    }

    @Test
    fun `alphanumeric should succeed for letters and digits only`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::username {
                alphanumeric()
            }
        }

        assertTrue(validator.validate(TestEntity(username = "john123")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(username = "ABC")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(username = "123")) is ValidationResult.Success)
    }

    @Test
    fun `alphanumeric should fail for strings with special characters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::username {
                alphanumeric()
            }
        }

        assertTrue(validator.validate(TestEntity(username = "john_123")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(username = "john@123")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(username = "john-123")) is ValidationResult.Failure)
    }

    @Test
    fun `alpha should succeed for letters only`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                alpha()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "John")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(name = "ABC")) is ValidationResult.Success)
    }

    @Test
    fun `alpha should fail for strings with digits or special chars`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                alpha()
            }
        }

        assertTrue(validator.validate(TestEntity(name = "John123")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(name = "John!")) is ValidationResult.Failure)
    }

    @Test
    fun `numeric should succeed for digits only`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                numeric()
            }
        }

        assertTrue(validator.validate(TestEntity(code = "1234")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(code = "0")) is ValidationResult.Success)
    }

    @Test
    fun `numeric should fail for strings with letters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                numeric()
            }
        }

        assertTrue(validator.validate(TestEntity(code = "12a4")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(code = "abc")) is ValidationResult.Failure)
    }

    @Test
    fun `uppercase should succeed for all uppercase letters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                uppercase()
            }
        }

        assertTrue(validator.validate(TestEntity(code = "ABC")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(code = "ABC123")) is ValidationResult.Success)
    }

    @Test
    fun `uppercase should fail for strings with lowercase letters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                uppercase()
            }
        }

        assertTrue(validator.validate(TestEntity(code = "Abc")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(code = "abc")) is ValidationResult.Failure)
    }

    @Test
    fun `lowercase should succeed for all lowercase letters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::username {
                lowercase()
            }
        }

        assertTrue(validator.validate(TestEntity(username = "john")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(username = "john123")) is ValidationResult.Success)
    }

    @Test
    fun `lowercase should fail for strings with uppercase letters`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::username {
                lowercase()
            }
        }

        assertTrue(validator.validate(TestEntity(username = "John")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(username = "JOHN")) is ValidationResult.Failure)
    }

    @Test
    fun `startsWith should succeed when string starts with prefix`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::sku {
                startsWith("PROD-")
            }
        }

        assertTrue(validator.validate(TestEntity(sku = "PROD-12345")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(sku = "PROD-ABC")) is ValidationResult.Success)
    }

    @Test
    fun `startsWith should fail when string does not start with prefix`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::sku {
                startsWith("PROD-")
            }
        }

        assertTrue(validator.validate(TestEntity(sku = "TEST-12345")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(sku = "12345")) is ValidationResult.Failure)
    }

    @Test
    fun `endsWith should succeed when string ends with suffix`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::url {
                endsWith(".com")
            }
        }

        assertTrue(validator.validate(TestEntity(url = "example.com")) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(url = "https://example.com")) is ValidationResult.Success)
    }

    @Test
    fun `endsWith should fail when string does not end with suffix`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::url {
                endsWith(".com")
            }
        }

        assertTrue(validator.validate(TestEntity(url = "example.org")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(url = "example")) is ValidationResult.Failure)
    }

    @Test
    fun `containsString should succeed when substring is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                containsString("@company.com")
            }
        }

        assertTrue(validator.validate(TestEntity(email = "john@company.com")) is ValidationResult.Success)
    }

    @Test
    fun `containsString should fail when substring is not present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                containsString("@company.com")
            }
        }

        assertTrue(validator.validate(TestEntity(email = "john@gmail.com")) is ValidationResult.Failure)
    }

    @Test
    fun `doesNotContain should succeed when substring is not present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                doesNotContain("password")
            }
        }

        assertTrue(validator.validate(TestEntity(password = "MySecret123")) is ValidationResult.Success)
    }

    @Test
    fun `doesNotContain should fail when substring is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::password {
                doesNotContain("password")
            }
        }

        assertTrue(validator.validate(TestEntity(password = "password123")) is ValidationResult.Failure)
    }

    @Test
    fun `matches should succeed when pattern matches`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                matches(Regex("^[A-Z]{3}-\\d{3}$"))
            }
        }

        assertTrue(validator.validate(TestEntity(code = "ABC-123")) is ValidationResult.Success)
    }

    @Test
    fun `matches should fail when pattern does not match`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::code {
                matches(Regex("^[A-Z]{3}-\\d{3}$"))
            }
        }

        assertTrue(validator.validate(TestEntity(code = "abc-123")) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(code = "ABC-12")) is ValidationResult.Failure)
    }

    @Test
    fun `string rules should skip validation when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::name {
                minLength(5)
                maxLength(10)
                alpha()
            }
        }

        assertTrue(validator.validate(TestEntity(name = null)) is ValidationResult.Success)
    }

    @Test
    fun `multiple string rules should all be validated`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                notBlank()
                email()
                maxLength(100)
            }
        }

        assertTrue(validator.validate(TestEntity(email = "test@example.com")) is ValidationResult.Success)
    }

    @Test
    fun `multiple string rules should fail when any rule fails`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::email {
                email()
                maxLength(10)
            }
        }

        val result = validator.validate(TestEntity(email = "test@example.com"))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }
}
