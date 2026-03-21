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

class BooleanRulesTest {

    data class TestEntity(
        val isActive: Boolean?,
        val isVerified: Boolean?,
        val acceptedTerms: Boolean?
    )

    @Test
    fun `isTrue should succeed when value is true`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = null, acceptedTerms = true)
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isTrue should fail when value is false`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = null, acceptedTerms = false)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("acceptedTerms", result.errors.first().fieldName)
        assertEquals("Terms must be accepted", result.errors.first().message)
    }

    @Test
    fun `isTrue should fail when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = null, acceptedTerms = null)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `isFalse should succeed when value is false`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::isActive {
                isFalse() hint "Must be inactive"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = false, isVerified = null, acceptedTerms = null)
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isFalse should fail when value is true`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::isActive {
                isFalse() hint "Must be inactive"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = true, isVerified = null, acceptedTerms = null)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("isActive", result.errors.first().fieldName)
    }

    @Test
    fun `isFalse should fail when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::isActive {
                isFalse() hint "Must be inactive"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = null, acceptedTerms = null)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `multiple boolean rules should all be validated`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
            TestEntity::isVerified {
                isTrue() hint "Account must be verified"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = true, acceptedTerms = true)
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `multiple boolean rules should fail when any rule fails`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
            TestEntity::isVerified {
                isTrue() hint "Account must be verified"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = false, acceptedTerms = true)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("isVerified", result.errors.first().fieldName)
    }

    @Test
    fun `multiple boolean rules should produce multiple errors`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::acceptedTerms {
                isTrue() hint "Terms must be accepted"
            }
            TestEntity::isVerified {
                isTrue() hint "Account must be verified"
            }
        }

        val result = validator.validate(
            TestEntity(isActive = null, isVerified = false, acceptedTerms = false)
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(2, result.errorCount)
    }

    @Test
    fun `isTrue and isFalse cannot both succeed on same field`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::isActive {
                isTrue()
                isFalse()
            }
        }

        // true fails isFalse
        val resultTrue = validator.validate(
            TestEntity(isActive = true, isVerified = null, acceptedTerms = null)
        )
        assertTrue(resultTrue is ValidationResult.Failure)

        // false fails isTrue
        val resultFalse = validator.validate(
            TestEntity(isActive = false, isVerified = null, acceptedTerms = null)
        )
        assertTrue(resultFalse is ValidationResult.Failure)

        // null fails both
        val resultNull = validator.validate(
            TestEntity(isActive = null, isVerified = null, acceptedTerms = null)
        )
        assertTrue(resultNull is ValidationResult.Failure)
        assertEquals(2, resultNull.errorCount) // Both rules fail
    }
}
