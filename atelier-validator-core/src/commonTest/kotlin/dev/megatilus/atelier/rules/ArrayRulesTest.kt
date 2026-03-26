/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import dev.megatilus.atelier.validator.rules.contains
import dev.megatilus.atelier.validator.rules.each
import dev.megatilus.atelier.validator.rules.isEmpty
import dev.megatilus.atelier.validator.rules.isNotEmpty
import dev.megatilus.atelier.validator.rules.minSize
import dev.megatilus.atelier.validator.rules.notBlank
import dev.megatilus.atelier.validator.rules.size
import kotlin.test.Test
import kotlin.test.assertTrue

class ArrayRulesTest {

    class TestEntity(
        val tags: Array<String>? = null,
        val numbers: IntArray? = null
    )

    class Author(val name: String?)
    class Document(val authors: Array<Author>? = null)

    @Test
    fun `isNotEmpty should succeed for non-empty array`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = arrayOf("kotlin"))) is ValidationResult.Success)
    }

    @Test
    fun `isNotEmpty should fail for empty array`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = emptyArray())) is ValidationResult.Failure)
    }

    @Test
    fun `isEmpty should succeed for empty array`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = emptyArray())) is ValidationResult.Success)
    }

    @Test
    fun `isEmpty should succeed for null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = null)) is ValidationResult.Success)
    }

    @Test
    fun `size should succeed when in range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                size(1..10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = arrayOf("kotlin", "java"))) is ValidationResult.Success)
    }

    @Test
    fun `size should fail when out of range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                size(1..10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = emptyArray())) is ValidationResult.Failure)
    }

    @Test
    fun `contains should succeed when element is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                contains("kotlin")
            }
        }

        assertTrue(validator.validate(TestEntity(tags = arrayOf("kotlin", "java"))) is ValidationResult.Success)
    }

    @Test
    fun `contains should fail when element is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                contains("kotlin")
            }
        }

        assertTrue(validator.validate(TestEntity(tags = arrayOf("java"))) is ValidationResult.Failure)
    }

    @Test
    fun `each should succeed when all elements are valid`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator)
            }
        }

        assertTrue(
            validator.validate(
                Document(authors = arrayOf(Author("John"), Author("Jane")))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `each should fail when any element is invalid`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator)
            }
        }

        assertTrue(
            validator.validate(
                Document(authors = arrayOf(Author("John"), Author("")))
            ) is ValidationResult.Failure
        )
    }

    @Test
    fun `IntArray isNotEmpty should succeed for non-empty array`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::numbers {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(numbers = intArrayOf(1, 2, 3))) is ValidationResult.Success)
    }

    @Test
    fun `IntArray isNotEmpty should fail for empty array`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::numbers {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(numbers = intArrayOf())) is ValidationResult.Failure)
    }

    @Test
    fun `array rules should skip validation when array is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                minSize(1)
                contains("kotlin")
            }
        }

        assertTrue(validator.validate(TestEntity(tags = null)) is ValidationResult.Success)
    }
}
