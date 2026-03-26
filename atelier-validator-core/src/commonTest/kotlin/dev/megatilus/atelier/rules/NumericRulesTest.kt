/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import dev.megatilus.atelier.validator.rules.isNegative
import dev.megatilus.atelier.validator.rules.isPositive
import dev.megatilus.atelier.validator.rules.max
import dev.megatilus.atelier.validator.rules.min
import dev.megatilus.atelier.validator.rules.range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumericRulesTest {

    data class IntEntity(val value: Int? = null)
    data class LongEntity(val value: Long? = null)
    data class FloatEntity(val value: Float? = null)
    data class DoubleEntity(val value: Double? = null)

    @Test
    fun `Int min should succeed when value is greater or equal`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                min(18) hint "Must be at least 18"
            }
        }

        assertTrue(validator.validate(IntEntity(value = 18)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = 25)) is ValidationResult.Success)
    }

    @Test
    fun `Int min should fail when value is less`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                min(18) hint "Must be at least 18"
            }
        }

        val result = validator.validate(IntEntity(value = 16))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("Must be at least 18", result.errors.first().message)
    }

    @Test
    fun `Int max should succeed when value is less or equal`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                max(120)
            }
        }

        assertTrue(validator.validate(IntEntity(value = 120)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = 100)) is ValidationResult.Success)
    }

    @Test
    fun `Int max should fail when value is greater`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                max(120)
            }
        }

        assertTrue(validator.validate(IntEntity(value = 150)) is ValidationResult.Failure)
    }

    @Test
    fun `Int range should succeed when value is in range`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                range(1..10)
            }
        }

        assertTrue(validator.validate(IntEntity(value = 1)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = 5)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = 10)) is ValidationResult.Success)
    }

    @Test
    fun `Int range should fail when value is out of range`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                range(1..10)
            }
        }

        assertTrue(validator.validate(IntEntity(value = 0)) is ValidationResult.Failure)
        assertTrue(validator.validate(IntEntity(value = 11)) is ValidationResult.Failure)
    }

    @Test
    fun `Int isPositive should succeed for positive values`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(IntEntity(value = 1)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = 100)) is ValidationResult.Success)
    }

    @Test
    fun `Int isPositive should fail for zero and negative values`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(IntEntity(value = 0)) is ValidationResult.Failure)
        assertTrue(validator.validate(IntEntity(value = -10)) is ValidationResult.Failure)
    }

    @Test
    fun `Int isNegative should succeed for negative values`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(IntEntity(value = -1)) is ValidationResult.Success)
        assertTrue(validator.validate(IntEntity(value = -100)) is ValidationResult.Success)
    }

    @Test
    fun `Int isNegative should fail for zero and positive values`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(IntEntity(value = 0)) is ValidationResult.Failure)
        assertTrue(validator.validate(IntEntity(value = 10)) is ValidationResult.Failure)
    }

    @Test
    fun `Int rules should skip validation when value is null`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                min(10)
                max(100)
                isPositive()
            }
        }

        val result = validator.validate(IntEntity(value = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `Long min should succeed when value is greater or equal`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                min(1024L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 1024L)) is ValidationResult.Success)
        assertTrue(validator.validate(LongEntity(value = 2048L)) is ValidationResult.Success)
    }

    @Test
    fun `Long min should fail when value is less`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                min(1024L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 512L)) is ValidationResult.Failure)
    }

    @Test
    fun `Long max should succeed when value is less or equal`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                max(10_000_000L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 5_000_000L)) is ValidationResult.Success)
    }

    @Test
    fun `Long max should fail when value is greater`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                max(10_000_000L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 15_000_000L)) is ValidationResult.Failure)
    }

    @Test
    fun `Long range should succeed when value is in range`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                range(0L..1000L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 500L)) is ValidationResult.Success)
    }

    @Test
    fun `Long range should fail when value is out of range`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                range(0L..1000L)
            }
        }

        assertTrue(validator.validate(LongEntity(value = 1500L)) is ValidationResult.Failure)
    }

    @Test
    fun `Long isPositive should succeed for positive values`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(LongEntity(value = 100L)) is ValidationResult.Success)
    }

    @Test
    fun `Long isPositive should fail for zero and negative`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(LongEntity(value = 0L)) is ValidationResult.Failure)
        assertTrue(validator.validate(LongEntity(value = -10L)) is ValidationResult.Failure)
    }

    @Test
    fun `Long isNegative should succeed for negative values`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(LongEntity(value = -100L)) is ValidationResult.Success)
    }

    @Test
    fun `Long isNegative should fail for zero and positive`() {
        val validator = AtelierValidator<LongEntity> {
            LongEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(LongEntity(value = 0L)) is ValidationResult.Failure)
        assertTrue(validator.validate(LongEntity(value = 100L)) is ValidationResult.Failure)
    }


    @Test
    fun `Float min should succeed when value is greater or equal`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                min(0.1f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.1f)) is ValidationResult.Success)
        assertTrue(validator.validate(FloatEntity(value = 1.5f)) is ValidationResult.Success)
    }

    @Test
    fun `Float min should fail when value is less`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                min(0.1f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.05f)) is ValidationResult.Failure)
    }

    @Test
    fun `Float max should succeed when value is less or equal`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                max(0.9f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.5f)) is ValidationResult.Success)
    }

    @Test
    fun `Float max should fail when value is greater`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                max(0.9f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.95f)) is ValidationResult.Failure)
    }

    @Test
    fun `Float range should succeed when value is in range`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                range(-40.0f..85.0f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 25.0f)) is ValidationResult.Success)
    }

    @Test
    fun `Float range should fail when value is out of range`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                range(-40.0f..85.0f)
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 100.0f)) is ValidationResult.Failure)
    }

    @Test
    fun `Float isPositive should succeed for positive values`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 19.99f)) is ValidationResult.Success)
    }

    @Test
    fun `Float isPositive should fail for zero and negative`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.0f)) is ValidationResult.Failure)
        assertTrue(validator.validate(FloatEntity(value = -5.0f)) is ValidationResult.Failure)
    }

    @Test
    fun `Float isNegative should succeed for negative values`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(FloatEntity(value = -10.0f)) is ValidationResult.Success)
    }

    @Test
    fun `Float isNegative should fail for zero and positive`() {
        val validator = AtelierValidator<FloatEntity> {
            FloatEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(FloatEntity(value = 0.0f)) is ValidationResult.Failure)
        assertTrue(validator.validate(FloatEntity(value = 5.0f)) is ValidationResult.Failure)
    }

    @Test
    fun `Double min should succeed when value is greater or equal`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                min(-90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 48.8566)) is ValidationResult.Success)
    }

    @Test
    fun `Double min should fail when value is less`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                min(-90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = -100.0)) is ValidationResult.Failure)
    }

    @Test
    fun `Double max should succeed when value is less or equal`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                max(90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 48.8566)) is ValidationResult.Success)
    }

    @Test
    fun `Double max should fail when value is greater`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                max(90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 100.0)) is ValidationResult.Failure)
    }

    @Test
    fun `Double range should succeed when value is in range`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                range(-90.0..90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 48.8566)) is ValidationResult.Success)
    }

    @Test
    fun `Double range should fail when value is out of range`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                range(-90.0..90.0)
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 100.0)) is ValidationResult.Failure)
    }

    @Test
    fun `Double isPositive should succeed for positive values`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 1000.50)) is ValidationResult.Success)
    }

    @Test
    fun `Double isPositive should fail for zero and negative`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                isPositive()
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 0.0)) is ValidationResult.Failure)
        assertTrue(validator.validate(DoubleEntity(value = -50.0)) is ValidationResult.Failure)
    }

    @Test
    fun `Double isNegative should succeed for negative values`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = -100.0)) is ValidationResult.Success)
    }

    @Test
    fun `Double isNegative should fail for zero and positive`() {
        val validator = AtelierValidator<DoubleEntity> {
            DoubleEntity::value {
                isNegative()
            }
        }

        assertTrue(validator.validate(DoubleEntity(value = 0.0)) is ValidationResult.Failure)
        assertTrue(validator.validate(DoubleEntity(value = 50.0)) is ValidationResult.Failure)
    }

    @Test
    fun `multiple numeric rules should all be validated`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                min(0)
                max(100)
                isPositive()
            }
        }

        assertTrue(validator.validate(IntEntity(value = 50)) is ValidationResult.Success)
    }

    @Test
    fun `multiple numeric rules should fail when any rule fails`() {
        val validator = AtelierValidator<IntEntity> {
            IntEntity::value {
                min(0)
                max(100)
            }
        }

        assertTrue(validator.validate(IntEntity(value = 150)) is ValidationResult.Failure)
    }
}
