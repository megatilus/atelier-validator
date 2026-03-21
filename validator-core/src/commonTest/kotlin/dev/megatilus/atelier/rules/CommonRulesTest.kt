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

class CommonRulesTest {

    data class TestEntity(
        val id: String?,
        val status: String?,
        val role: String?
    )

    @Test
    fun `notNull should succeed for non-null value`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::id {
                notNull() hint "ID is required"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `notNull should fail for null value`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::id {
                notNull() hint "ID is required"
            }
        }

        val result = validator.validate(TestEntity(id = null, status = null, role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("id", result.errors.first().fieldName)
        assertEquals("ID is required", result.errors.first().message)
    }


    @Test
    fun `isNull should succeed for null value`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                isNull() hint "Status must be null"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isNull should fail for non-null value`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                isNull() hint "Status must be null"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "active", role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("status", result.errors.first().fieldName)
    }

    @Test
    fun `equalTo should succeed when values match`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                equalTo("active") hint "Status must be active"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "active", role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `equalTo should fail when values differ`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                equalTo("active") hint "Status must be active"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "inactive", role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("status", result.errors.first().fieldName)
    }

    @Test
    fun `equalTo should skip validation when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                equalTo("active")
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `notEqualTo should succeed when values differ`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                notEqualTo("banned") hint "Status cannot be banned"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "active", role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `notEqualTo should fail when values match`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                notEqualTo("banned") hint "Status cannot be banned"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "banned", role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `notEqualTo should skip validation when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                notEqualTo("banned")
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isIn should succeed when value is in collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isIn(listOf("admin", "user", "moderator")) hint "Invalid role"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = "admin"))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isIn should fail when value is not in collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isIn(listOf("admin", "user", "moderator")) hint "Invalid role"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = "guest"))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("role", result.errors.first().fieldName)
    }

    @Test
    fun `isIn should skip validation when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isIn(listOf("admin", "user", "moderator"))
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isNotIn should succeed when value is not in collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isNotIn(listOf("banned", "suspended")) hint "Role is restricted"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = "admin"))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `isNotIn should fail when value is in collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isNotIn(listOf("banned", "suspended")) hint "Role is restricted"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = "banned"))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
    }

    @Test
    fun `isNotIn should skip validation when value is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::role {
                isNotIn(listOf("banned", "suspended"))
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = null, role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `multiple rules should all be validated`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                notNull() hint "Status is required"
                isIn(listOf("active", "inactive")) hint "Invalid status"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "active", role = null))

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `multiple rules should fail when any rule fails`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::status {
                notNull() hint "Status is required"
                isIn(listOf("active", "inactive")) hint "Invalid status"
            }
        }

        val result = validator.validate(TestEntity(id = "123", status = "banned", role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("Invalid status", result.errors.first().message)
    }

    @Test
    fun `multiple rules should produce multiple errors`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::id {
                notNull() hint "ID is required"
            }
            TestEntity::status {
                notNull() hint "Status is required"
            }
        }

        val result = validator.validate(TestEntity(id = null, status = null, role = null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(2, result.errorCount)
    }
}
