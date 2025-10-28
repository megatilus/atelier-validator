/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validators

import dev.megatilus.atelier.atelierValidator
import dev.megatilus.atelier.field
import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumericValidatorsTest {

    private data class TestObject(
        val intField: Int,
        val longField: Long,
        val floatField: Float,
        val doubleField: Double,
        val byteField: Byte,
        val shortField: Short,
        val nullableIntField: Int?,
        val nullableDoubleField: Double?
    )

    // ===== COMPARABLE MIN/MAX/RANGE TESTS =====

    @Test
    fun `min should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).min(10, "Must be at least 10")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(10, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(15, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(9, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-10, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.OUT_OF_RANGE, failure.errors.first().code)
            assertEquals("Must be at least 10", failure.errors.first().message)
        }
    }

    @Test
    fun `max should validate correctly for Double`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::doubleField).max(100.5, "Must be at most 100.5")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 50.0, 0, 0, null, null),
                TestObject(0, 0, 0f, 100.5, 0, 0, null, null),
                TestObject(0, 0, 0f, -10.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.doubleField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 100.6, 0, 0, null, null),
                TestObject(0, 0, 0f, 200.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.doubleField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.OUT_OF_RANGE, failure.errors.first().code)
        }
    }

    @Test
    fun `range inclusive should validate correctly`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField)
                    .range(10, 20, "Must be between 10 and 20 inclusive")
            }

        // Valid cases (inclusive)
        val validObjects =
            listOf(
                TestObject(10, 0, 0f, 0.0, 0, 0, null, null), // boundary
                TestObject(15, 0, 0f, 0.0, 0, 0, null, null), // middle
                TestObject(20, 0, 0f, 0.0, 0, 0, null, null) // boundary
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(9, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(21, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.OUT_OF_RANGE, failure.errors.first().code)
        }
    }

    @Test
    fun `range exclusive should validate correctly`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField)
                    .between(10, 20, "Must be between 10 and 20 exclusive")
            }

        // Valid cases (exclusive)
        val validObjects =
            listOf(
                TestObject(11, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(15, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(19, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (boundaries should fail for exclusive)
        val invalidObjects =
            listOf(
                TestObject(10, 0, 0f, 0.0, 0, 0, null, null), // boundary
                TestObject(20, 0, 0f, 0.0, 0, 0, null, null), // boundary
                TestObject(9, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(21, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")
        }
    }

    @Test
    fun `between should work as exclusive range alias`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).between(10, 20, "Must be between 10 and 20")
            }

        // Should behave like exclusive range
        val validObject = TestObject(15, 0, 0f, 0.0, 0, 0, null, null)
        assertTrue(validator.validate(validObject).isSuccess)

        val invalidBoundaryObject = TestObject(10, 0, 0f, 0.0, 0, 0, null, null)
        assertTrue(validator.validate(invalidBoundaryObject).isFailure)
    }

    @Test
    fun `oneOf should validate correctly`() {
        val validator =
            atelierValidator<TestObject> { field(TestObject::intField).oneOf(1, 5, 10, 25) }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(10, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(25, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(3, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(50, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `notOneOf should validate correctly`() {
        val validator =
            atelierValidator<TestObject> { field(TestObject::intField).notOneOf(0, -1, 999) }

        // Valid cases (not in the forbidden list)
        val validObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(50, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-5, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (in the forbidden list)
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(999, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    // ===== NULLABLE COMPARABLE TESTS =====

    @Test
    fun `nullable min should handle null correctly`() {
        val validator = atelierValidator<TestObject> { field(TestObject::nullableIntField).min(10) }

        // Null should pass
        val nullObject = TestObject(0, 0, 0f, 0.0, 0, 0, null, null)
        val nullResult = validator.validate(nullObject)
        assertTrue(nullResult.isSuccess)

        // Valid non-null should pass
        val validObject = TestObject(0, 0, 0f, 0.0, 0, 0, 15, null)
        val validResult = validator.validate(validObject)
        assertTrue(validResult.isSuccess)

        // Invalid non-null should fail
        val invalidObject = TestObject(0, 0, 0f, 0.0, 0, 0, 5, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    // ===== INT SPECIFIC TESTS =====

    @Test
    fun `positive should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).positive("Must be positive")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-100, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.OUT_OF_RANGE, failure.errors.first().code)
        }
    }

    @Test
    fun `negative should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).negative("Must be negative")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")
        }
    }

    @Test
    fun `nonNegative should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).isGreaterThanOrEqualTo(0, "Must be non-negative")
            }

        // Valid cases (>= 0)
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (< 0)
        val invalidObjects =
            listOf(
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-100, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")
        }
    }

    @Test
    fun `nonPositive should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).isLessThanOrEqualTo(0, "Must be non-positive")
            }

        // Valid cases (<= 0)
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (> 0)
        val invalidObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")
        }
    }

    @Test
    fun `even should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be even"
                ) { it % 2 == 0 }
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(2, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-4, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (odd)
        val invalidObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(3, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(101, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `odd should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be odd"
                ) { it % 2 != 0 }
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(3, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(101, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases (even)
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(2, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-4, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `multipleOf should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).multipleOf(5, "Must be multiple of 5")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(10, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-15, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(100, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(3, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(7, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-13, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `powerOfTwo should validate correctly for Int`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be power of 2"
                ) { it > 0 && (it and (it - 1)) == 0 }
            }

        // Valid cases (powers of 2)
        val validObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null), // 2^0
                TestObject(2, 0, 0f, 0.0, 0, 0, null, null), // 2^1
                TestObject(4, 0, 0f, 0.0, 0, 0, null, null), // 2^2
                TestObject(8, 0, 0f, 0.0, 0, 0, null, null), // 2^3
                TestObject(16, 0, 0f, 0.0, 0, 0, null, null), // 2^4
                TestObject(256, 0, 0f, 0.0, 0, 0, null, null), // 2^8
                TestObject(1024, 0, 0f, 0.0, 0, 0, null, null) // 2^10
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null), // 0 is not a power of 2
                TestObject(-1, 0, 0f, 0.0, 0, 0, null, null), // Negative numbers
                TestObject(3, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(5, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(6, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(7, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(9, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(15, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    // ===== LONG SPECIFIC TESTS =====

    @Test
    fun `positive should validate correctly for Long`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::longField).positive("Must be positive")
            }

        // Valid cases
        val validObject = TestObject(0, 1L, 0f, 0.0, 0, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid cases
        val invalidObject = TestObject(0, 0L, 0f, 0.0, 0, 0, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `even should validate correctly for Long`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::longField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be even"
                ) { it % 2L == 0L }
            }

        // Valid case
        val validObject = TestObject(0, 4L, 0f, 0.0, 0, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid case
        val invalidObject = TestObject(0, 3L, 0f, 0.0, 0, 0, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `multipleOf should validate correctly for Long`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::longField).multipleOf(3L, "Must be multiple of 3")
            }

        // Valid case
        val validObject = TestObject(0, 9L, 0f, 0.0, 0, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid case
        val invalidObject = TestObject(0, 10L, 0f, 0.0, 0, 0, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)

        val failure = invalidResult as ValidationResult.Failure
        assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
    }

    // ===== FLOAT SPECIFIC TESTS =====

    @Test
    fun `positive should validate correctly for Float`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::floatField).positive("Must be positive")
            }

        // Valid cases
        val validObject = TestObject(0, 0, 1.5f, 0.0, 0, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid cases
        val invalidObject = TestObject(0, 0, 0f, 0.0, 0, 0, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `isFinite should validate correctly for Float`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::floatField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be finite"
                ) { it.isFinite() }
            }

        // Valid cases (finite numbers)
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 123.45f, 0.0, 0, 0, null, null),
                TestObject(0, 0, -999.999f, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.MAX_VALUE, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.MIN_VALUE, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for finite value ${obj.floatField}")
        }

        // Invalid cases (infinite or NaN)
        val invalidObjects =
            listOf(
                TestObject(0, 0, Float.POSITIVE_INFINITY, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.NEGATIVE_INFINITY, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.NaN, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for non-finite value ${obj.floatField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `isNaN should validate correctly for Float`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::floatField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be NaN"
                ) { it.isNaN() }
            }

        // Valid case (NaN)
        val validObject = TestObject(0, 0, Float.NaN, 0.0, 0, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid cases (not NaN)
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 123.45f, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.POSITIVE_INFINITY, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for non-NaN value ${obj.floatField}")
        }
    }

    @Test
    fun `isNotNaN should validate correctly for Float`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::floatField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must not be NaN"
                ) { !it.isNaN() }
            }

        // Valid cases (not NaN)
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 123.45f, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.POSITIVE_INFINITY, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for non-NaN value ${obj.floatField}")
        }

        // Invalid case (NaN)
        val invalidObject = TestObject(0, 0, Float.NaN, 0.0, 0, 0, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)

        val failure = invalidResult as ValidationResult.Failure
        assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
    }

    @Test
    fun `isInfinite should validate correctly for Float`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::floatField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be infinite"
                ) { it.isInfinite() }
            }

        // Valid cases (infinite)
        val validObjects =
            listOf(
                TestObject(0, 0, Float.POSITIVE_INFINITY, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.NEGATIVE_INFINITY, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for infinite value ${obj.floatField}")
        }

        // Invalid cases (finite or NaN)
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 123.45f, 0.0, 0, 0, null, null),
                TestObject(0, 0, Float.NaN, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for non-infinite value ${obj.floatField}")
        }
    }

    // ===== DOUBLE SPECIFIC TESTS =====

    @Test
    fun `positive should validate correctly for Double`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::doubleField).positive("Must be positive")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.1, 0, 0, null, null),
                TestObject(0, 0, 0f, 123.456, 0, 0, null, null),
                TestObject(0, 0, 0f, Double.MAX_VALUE, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for positive value ${obj.doubleField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 0f, -0.1, 0, 0, null, null),
                TestObject(0, 0, 0f, -123.456, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for non-positive value ${obj.doubleField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.OUT_OF_RANGE, failure.errors.first().code)
        }
    }

    @Test
    fun `isFinite should validate correctly for Double`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::doubleField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be finite"
                ) { it.isFinite() }
            }

        // Valid cases (finite numbers)
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 0f, 123.456, 0, 0, null, null),
                TestObject(0, 0, 0f, -999.999, 0, 0, null, null),
                TestObject(0, 0, 0f, Double.MAX_VALUE, 0, 0, null, null),
                TestObject(0, 0, 0f, Double.MIN_VALUE, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for finite value ${obj.doubleField}")
        }

        // Invalid cases (infinite or NaN)
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, Double.POSITIVE_INFINITY, 0, 0, null, null),
                TestObject(0, 0, 0f, Double.NEGATIVE_INFINITY, 0, 0, null, null),
                TestObject(0, 0, 0f, Double.NaN, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for non-finite value ${obj.doubleField}")

            val failure = result as ValidationResult.Failure
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    // ===== BYTE AND SHORT TESTS =====

    @Test
    fun `positive should validate correctly for Byte`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::byteField).positive("Must be positive")
            }

        // Valid case
        val validObject = TestObject(0, 0, 0f, 0.0, 5, 0, null, null)
        val result = validator.validate(validObject)
        assertTrue(result.isSuccess)

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 0f, 0.0, -1, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.byteField}")
        }
    }

    @Test
    fun `nonNegative should validate correctly for Short`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::shortField).isGreaterThanOrEqualTo(0, "Must be non-negative")
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(0, 0, 0f, 0.0, 0, 10, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for value ${obj.shortField}")
        }

        // Invalid case
        val invalidObject = TestObject(0, 0, 0f, 0.0, 0, -1, null, null)
        val invalidResult = validator.validate(invalidObject)
        assertTrue(invalidResult.isFailure)
    }

    // ===== CHAINED VALIDATIONS TESTS =====

    @Test
    fun `chained numeric validations should work correctly`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField)
                    .positive("Must be positive")
                    .max(100, "Must be at most 100")
                    .custom(code = ValidatorCode.INVALID_VALUE, message = "Must be even") {
                        it % 2 == 0
                    }
            }

        // Valid case
        val validObject = TestObject(50, 0, 0f, 0.0, 0, 0, null, null)
        val validResult = validator.validate(validObject)
        assertTrue(validResult.isSuccess)

        // Test each constraint failure
        val testCases =
            listOf(
                TestObject(-2, 0, 0f, 0.0, 0, 0, null, null) to
                    ValidatorCode.OUT_OF_RANGE, // negative
                TestObject(150, 0, 0f, 0.0, 0, 0, null, null) to
                    ValidatorCode.OUT_OF_RANGE, // too big
                TestObject(51, 0, 0f, 0.0, 0, 0, null, null) to
                    ValidatorCode.INVALID_VALUE // odd
            )

        testCases.forEach { (obj, expectedCode) ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for value ${obj.intField}")

            val failure = result as ValidationResult.Failure
            assertTrue(
                failure.errors.any { it.code == expectedCode },
                "Should have error with code $expectedCode for value ${obj.intField}"
            )
        }
    }

    // ===== EDGE CASES AND ERROR MESSAGE TESTS =====

    @Test
    fun `validators should use default messages when not specified`() {
        val validator = atelierValidator<TestObject> { field(TestObject::intField).positive() }

        val invalidObject = TestObject(-1, 0, 0f, 0.0, 0, 0, null, null)
        val result = validator.validate(invalidObject)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals("Must be positive", failure.errors.first().message)
    }

    @Test
    fun `complex numeric validation scenario`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).range(1, 1000, "Age must be between 1 and 1000")
                field(TestObject::doubleField)
                    .positive("Price must be positive")
                    .max(9999.99, "Price cannot exceed 9999.99")
                field(TestObject::longField).multipleOf(10L, "Quantity must be multiple of 10")
            }

        // Valid case
        val validObject = TestObject(25, 100L, 0f, 299.99, 0, 0, null, null)
        assertTrue(validator.validate(validObject).isSuccess)

        // Multiple failures
        val invalidObject = TestObject(0, 7L, 0f, -10.0, 0, 0, null, null)
        val result = validator.validate(invalidObject)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(3, failure.errorCount) // All three fields should fail
    }

    // ===== CUSTOM VALIDATOR TESTS =====

    @Test
    fun `custom validator should work with simple numeric predicate`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be divisible by 7"
                ) { it % 7 == 0 }
            }

        // Valid cases
        val validObjects =
            listOf(
                TestObject(0, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(7, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(14, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(-7, 0, 0f, 0.0, 0, 0, null, null)
            )

        validObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isSuccess, "Should pass for ${obj.intField}")
        }

        // Invalid cases
        val invalidObjects =
            listOf(
                TestObject(1, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(8, 0, 0f, 0.0, 0, 0, null, null),
                TestObject(15, 0, 0f, 0.0, 0, 0, null, null)
            )

        invalidObjects.forEach { obj ->
            val result = validator.validate(obj)
            assertTrue(result.isFailure, "Should fail for ${obj.intField}")
            val failure = result as ValidationResult.Failure
            assertEquals("Must be divisible by 7", failure.errors.first().message)
            assertEquals(ValidatorCode.INVALID_VALUE, failure.errors.first().code)
        }
    }

    @Test
    fun `custom validator should work with complex business logic`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be a prime number"
                ) { num ->
                    if (num < 2) return@custom false
                    for (i in 2..kotlin.math.sqrt(num.toDouble()).toInt()) {
                        if (num % i == 0) return@custom false
                    }
                    true
                }
            }

        // Valid cases (prime numbers)
        val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31)
        primes.forEach { prime ->
            val result = validator.validate(TestObject(prime, 0, 0f, 0.0, 0, 0, null, null))
            assertTrue(result.isSuccess, "Should pass for prime $prime")
        }

        // Invalid cases (non-prime numbers)
        val nonPrimes = listOf(0, 1, 4, 6, 8, 9, 10, 12, 15, 20, 25)
        nonPrimes.forEach { num ->
            val result = validator.validate(TestObject(num, 0, 0f, 0.0, 0, 0, null, null))
            assertTrue(result.isFailure, "Should fail for non-prime $num")
        }
    }

    @Test
    fun `custom validator should be chainable with other numeric validators`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField)
                    .positive("Must be positive")
                    .range(1, 100, "Must be between 1 and 100")
                    .custom(
                        code = ValidatorCode.INVALID_VALUE,
                        message = "Must be a perfect square"
                    ) {
                        val sqrt = kotlin.math.sqrt(it.toDouble()).toInt()
                        sqrt * sqrt == it
                    }
            }

        // Valid cases (perfect squares in range)
        val validObjects = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
        validObjects.forEach { value ->
            val result = validator.validate(TestObject(value, 0, 0f, 0.0, 0, 0, null, null))
            assertTrue(result.isSuccess, "Should pass for $value")
        }

        // Invalid - not positive
        val negativeResult = validator.validate(TestObject(-4, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(negativeResult.isFailure)

        // Invalid - out of range
        val outOfRangeResult = validator.validate(TestObject(121, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(outOfRangeResult.isFailure)

        // Invalid - not a perfect square
        val notSquareResult = validator.validate(TestObject(50, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(notSquareResult.isFailure)
        val failure = notSquareResult as ValidationResult.Failure
        assertEquals("Must be a perfect square", failure.errors.first().message)
    }

    @Test
    fun `custom validator for Double precision validation`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::doubleField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must have at most 2 decimal places"
                ) { value ->
                    val scaled = (value * 100).toLong()
                    kotlin.math.abs(scaled.toDouble() - value * 100) < 0.0001
                }
            }

        // Valid cases
        val validCases = listOf(0.0, 1.5, 10.99, 123.45, 0.01, 99.00, 5.0, 1234.56)
        validCases.forEach { value ->
            val result = validator.validate(TestObject(0, 0, 0f, value, 0, 0, null, null))
            assertTrue(result.isSuccess, "Should pass for $value")
        }

        // Invalid cases (more than 2 decimal places)
        val invalidCases = listOf(0.001, 1.234, 10.999, 123.456)
        invalidCases.forEach { value ->
            val result = validator.validate(TestObject(0, 0, 0f, value, 0, 0, null, null))
            assertTrue(result.isFailure, "Should fail for $value")
        }
    }

    @Test
    fun `custom validator with nullable numeric fields`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::nullableIntField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be a Fibonacci number"
                ) { value ->
                    if (value == null) return@custom true
                    val fibs = listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144)
                    value in fibs
                }
            }

        // Null should pass
        val nullResult = validator.validate(TestObject(0, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(nullResult.isSuccess)

        // Valid Fibonacci numbers
        val validFibs = listOf(0, 1, 2, 3, 5, 8, 13, 21)
        validFibs.forEach { fib ->
            val result = validator.validate(TestObject(0, 0, 0f, 0.0, 0, 0, fib, null))
            assertTrue(result.isSuccess, "Should pass for Fibonacci number $fib")
        }

        // Invalid - not Fibonacci
        val invalidResult = validator.validate(TestObject(0, 0, 0f, 0.0, 0, 0, 7, null))
        assertTrue(invalidResult.isFailure)
        val failure = invalidResult as ValidationResult.Failure
        assertEquals("Must be a Fibonacci number", failure.errors.first().message)
    }

    @Test
    fun `custom validator for age validation scenario`() {
        data class Person(val age: Int)

        val validator =
            atelierValidator<Person> {
                field(Person::age).range(0, 150, "Age must be between 0 and 150").custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be at least 18 to register"
                ) { it >= 18 }
            }

        // Valid cases
        val validAges = listOf(18, 21, 30, 65, 100)
        validAges.forEach { age ->
            val result = validator.validate(Person(age))
            assertTrue(result.isSuccess, "Should pass for age $age")
        }

        // Invalid - under 18
        val underageResult = validator.validate(Person(17))
        assertTrue(underageResult.isFailure)
        val underageFailure = underageResult as ValidationResult.Failure
        assertEquals("Must be at least 18 to register", underageFailure.errors.first().message)

        // Invalid - over range
        val overAgeResult = validator.validate(Person(200))
        assertTrue(overAgeResult.isFailure)
    }

    @Test
    fun `custom validator for price validation with tax calculation`() {
        data class Product(val price: Double, val taxRate: Double)

        val validator =
            atelierValidator<Product> {
                field(Product::price).positive("Price must be positive").custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Total price with tax cannot exceed 1000"
                ) { price ->
                    // This is a simplified example - in real scenario,
                    // you'd use object-aware validation
                    price < 900 // Assuming max 11% tax
                }
            }

        // Valid cases
        val validResult = validator.validate(Product(500.0, 0.1))
        assertTrue(validResult.isSuccess)

        // Invalid - price too high
        val invalidResult = validator.validate(Product(950.0, 0.1))
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `custom validator with default ValidatorCode CUSTOM_ERROR for numeric`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField).custom(message = "Must be between 10 and 20") {
                    it in 10..20
                }
            }

        val invalidObject = TestObject(5, 0, 0f, 0.0, 0, 0, null, null)
        val result = validator.validate(invalidObject)

        assertTrue(result.isFailure)
        val failure = result as ValidationResult.Failure
        assertEquals(ValidatorCode.CUSTOM_ERROR, failure.errors.first().code)
        assertEquals("Must be between 10 and 20", failure.errors.first().message)
    }

    @Test
    fun `multiple custom validators on same numeric field`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::intField)
                    .custom(
                        code = ValidatorCode.INVALID_VALUE,
                        message = "Must be greater than 10"
                    ) { it > 10 }
                    .custom(
                        code = ValidatorCode.INVALID_VALUE,
                        message = "Must be less than 100"
                    ) { it < 100 }
                    .custom(
                        code = ValidatorCode.INVALID_FORMAT,
                        message = "Must not end with 0"
                    ) { it % 10 != 0 }
            }

        // Valid case
        val validObject = TestObject(55, 0, 0f, 0.0, 0, 0, null, null)
        assertTrue(validator.validate(validObject).isSuccess)

        // Invalid - too small
        val tooSmallResult = validator.validate(TestObject(5, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(tooSmallResult.isFailure)
        val tooSmallFailure = tooSmallResult as ValidationResult.Failure
        assertTrue(tooSmallFailure.errors.any { it.message == "Must be greater than 10" })

        // Invalid - too large
        val tooLargeResult = validator.validate(TestObject(150, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(tooLargeResult.isFailure)
        val tooLargeFailure = tooLargeResult as ValidationResult.Failure
        assertTrue(tooLargeFailure.errors.any { it.message == "Must be less than 100" })

        // Invalid - ends with 0
        val endsWithZeroResult = validator.validate(TestObject(50, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(endsWithZeroResult.isFailure)
        val endsWithZeroFailure = endsWithZeroResult as ValidationResult.Failure
        assertTrue(endsWithZeroFailure.errors.any { it.message == "Must not end with 0" })

        // Invalid - multiple rules fail
        val multipleFailResult = validator.validate(TestObject(10, 0, 0f, 0.0, 0, 0, null, null))
        assertTrue(multipleFailResult.isFailure)
        val multipleFailFailure = multipleFailResult as ValidationResult.Failure
        assertEquals(2, multipleFailFailure.errorCount)
    }

    @Test
    fun `custom validator for floating point comparison tolerance`() {
        val validator =
            atelierValidator<TestObject> {
                field(TestObject::doubleField).custom(
                    code = ValidatorCode.INVALID_VALUE,
                    message = "Must be approximately equal to PI"
                ) { value -> kotlin.math.abs(value - kotlin.math.PI) < 0.01 }
            }

        // Valid cases (close to PI)
        val validCases = listOf(3.14, 3.1415, 3.142, 3.15)
        validCases.forEach { value ->
            val result = validator.validate(TestObject(0, 0, 0f, value, 0, 0, null, null))
            assertTrue(result.isSuccess, "Should pass for $value (close to PI)")
        }

        // Invalid cases (not close to PI)
        val invalidCases = listOf(3.0, 3.2, 4.0, 2.0)
        invalidCases.forEach { value ->
            val result = validator.validate(TestObject(0, 0, 0f, value, 0, 0, null, null))
            assertTrue(result.isFailure, "Should fail for $value (not close to PI)")
        }
    }
}
