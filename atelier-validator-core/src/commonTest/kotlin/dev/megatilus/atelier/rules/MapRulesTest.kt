/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import dev.megatilus.atelier.validator.rules.containsAllKeys
import dev.megatilus.atelier.validator.rules.containsAnyKey
import dev.megatilus.atelier.validator.rules.containsKey
import dev.megatilus.atelier.validator.rules.containsValue
import dev.megatilus.atelier.validator.rules.doesNotContainKey
import dev.megatilus.atelier.validator.rules.doesNotContainValue
import dev.megatilus.atelier.validator.rules.eachEntry
import dev.megatilus.atelier.validator.rules.eachKey
import dev.megatilus.atelier.validator.rules.eachValue
import dev.megatilus.atelier.validator.rules.isEmpty
import dev.megatilus.atelier.validator.rules.isNotEmpty
import dev.megatilus.atelier.validator.rules.maxSize
import dev.megatilus.atelier.validator.rules.minSize
import dev.megatilus.atelier.validator.rules.size
import kotlin.test.Test
import kotlin.test.assertTrue

class MapRulesTest {

    data class TestEntity(
        val settings: Map<String, String>? = null,
        val metadata: Map<String, Any>? = null
    )

    @Test
    fun `isNotEmpty should succeed for non-empty map`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }

    @Test
    fun `isNotEmpty should fail for empty map`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(settings = emptyMap())) is ValidationResult.Failure)
    }

    @Test
    fun `isEmpty should succeed for empty map`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(settings = emptyMap())) is ValidationResult.Success)
    }

    @Test
    fun `isEmpty should succeed for null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(settings = null)) is ValidationResult.Success)
    }

    @Test
    fun `size should succeed when in range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                size(1..20)
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("key" to "value"))) is ValidationResult.Success)
    }

    @Test
    fun `size should fail when out of range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                size(1..20)
            }
        }

        assertTrue(validator.validate(TestEntity(settings = emptyMap())) is ValidationResult.Failure)
    }

    @Test
    fun `minSize should succeed when size is sufficient`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                minSize(1)
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }

    @Test
    fun `maxSize should fail when size exceeds limit`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                maxSize(2)
            }
        }

        val largeMap = (0..5).associate { "key$it" to "value$it" }
        assertTrue(validator.validate(TestEntity(settings = largeMap)) is ValidationResult.Failure)
    }

    @Test
    fun `containsKey should succeed when key is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsKey("theme")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }

    @Test
    fun `containsKey should fail when key is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsKey("theme")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("language" to "en"))) is ValidationResult.Failure)
    }

    @Test
    fun `doesNotContainKey should succeed when key is absent`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                doesNotContainKey("deprecated")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }

    @Test
    fun `doesNotContainKey should fail when key is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                doesNotContainKey("deprecated")
            }
        }

        assertTrue(
            validator.validate(TestEntity(settings = mapOf("deprecated" to "value"))) is ValidationResult.Failure
        )
    }

    @Test
    fun `containsAllKeys should succeed when all keys are present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsAllKeys(listOf("host", "port"))
            }
        }

        assertTrue(
            validator.validate(
                TestEntity(settings = mapOf("host" to "localhost", "port" to "3306", "username" to "root"))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `containsAllKeys should fail when any key is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsAllKeys(listOf("host", "port"))
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("host" to "localhost"))) is ValidationResult.Failure)
    }

    @Test
    fun `containsAnyKey should succeed when at least one key is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsAnyKey(listOf("password", "apiKey", "token"))
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("password" to "secret"))) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(settings = mapOf("token" to "abc123"))) is ValidationResult.Success)
    }

    @Test
    fun `containsAnyKey should fail when no keys are present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsAnyKey(listOf("password", "apiKey", "token"))
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("username" to "john"))) is ValidationResult.Failure)
    }

    @Test
    fun `containsValue should succeed when value is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsValue("admin")
            }
        }

        assertTrue(
            validator.validate(
                TestEntity(settings = mapOf("role1" to "admin", "role2" to "user"))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `containsValue should fail when value is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                containsValue("admin")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("role1" to "user"))) is ValidationResult.Failure)
    }

    @Test
    fun `doesNotContainValue should succeed when value is absent`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                doesNotContainValue("disabled")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }

    @Test
    fun `doesNotContainValue should fail when value is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                doesNotContainValue("disabled")
            }
        }

        assertTrue(
            validator.validate(TestEntity(settings = mapOf("feature" to "disabled"))) is ValidationResult.Failure
        )
    }

    @Test
    fun `eachEntry should succeed when all entries satisfy predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachEntry { key, value ->
                    key.startsWith("valid_") && value.isNotBlank()
                }
            }
        }

        assertTrue(
            validator.validate(
                TestEntity(settings = mapOf("valid_theme" to "dark", "valid_lang" to "en"))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `eachEntry should fail when any entry fails predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachEntry { key, _ ->
                    key.startsWith("valid_")
                }
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Failure)
    }

    @Test
    fun `eachKey should succeed when all keys satisfy predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachKey { it.startsWith("meta_") }
            }
        }

        assertTrue(
            validator.validate(
                TestEntity(settings = mapOf("meta_created" to "2025-01-01", "meta_updated" to "2025-03-13"))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `eachKey should fail when any key fails predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachKey { it.startsWith("meta_") }
            }
        }

        assertTrue(
            validator.validate(TestEntity(settings = mapOf("created" to "2025-01-01"))) is ValidationResult.Failure
        )
    }

    @Test
    fun `eachValue should succeed when all values satisfy predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachValue { it.isNotBlank() }
            }
        }

        assertTrue(
            validator.validate(
                TestEntity(settings = mapOf("theme" to "dark", "language" to "en"))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `eachValue should fail when any value fails predicate`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                eachValue { it.isNotBlank() }
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to ""))) is ValidationResult.Failure)
    }

    @Test
    fun `map rules should skip validation when map is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                minSize(1)
                containsKey("theme")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = null)) is ValidationResult.Success)
    }

    @Test
    fun `multiple map rules should all be validated`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::settings {
                isNotEmpty()
                minSize(1)
                containsKey("theme")
            }
        }

        assertTrue(validator.validate(TestEntity(settings = mapOf("theme" to "dark"))) is ValidationResult.Success)
    }
}
