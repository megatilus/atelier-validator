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

class CollectionRulesTest {

    data class TestEntity(
        val tags: List<String>? = null,
        val roles: List<String>? = null,
        val permissions: List<String>? = null
    )

    data class Author(val name: String?, val email: String?)
    data class Document(val authors: List<Author>? = null)

    @Test
    fun `isNotEmpty should succeed for non-empty collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty() hint "At least one tag required"
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("kotlin"))) is ValidationResult.Success)
    }

    @Test
    fun `isNotEmpty should fail for empty collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty() hint "At least one tag required"
            }
        }

        val result = validator.validate(TestEntity(tags = emptyList()))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("tags", result.errors.first().fieldName)
    }

    @Test
    fun `isNotEmpty should fail for null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = null)) is ValidationResult.Failure)
    }

    @Test
    fun `isEmpty should succeed for empty collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = emptyList())) is ValidationResult.Success)
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
    fun `isEmpty should fail for non-empty collection`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isEmpty()
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("kotlin"))) is ValidationResult.Failure)
    }

    @Test
    fun `size should succeed when in range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                size(1..10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("kotlin"))) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(tags = List(10) { "tag$it" })) is ValidationResult.Success)
    }

    @Test
    fun `size should fail when out of range`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                size(1..10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = emptyList())) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(tags = List(15) { "tag$it" })) is ValidationResult.Failure)
    }

    @Test
    fun `minSize should succeed when size is sufficient`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                minSize(2)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("a", "b"))) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(tags = listOf("a", "b", "c"))) is ValidationResult.Success)
    }

    @Test
    fun `minSize should fail when size is too small`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                minSize(2)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("a"))) is ValidationResult.Failure)
    }

    @Test
    fun `maxSize should succeed when size is within limit`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                maxSize(10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("kotlin"))) is ValidationResult.Success)
    }

    @Test
    fun `maxSize should fail when size exceeds limit`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                maxSize(10)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = List(15) { "tag$it" })) is ValidationResult.Failure)
    }

    @Test
    fun `exactSize should succeed when size matches`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                exactSize(3)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("a", "b", "c"))) is ValidationResult.Success)
    }

    @Test
    fun `exactSize should fail when size differs`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                exactSize(3)
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("a", "b"))) is ValidationResult.Failure)
        assertTrue(validator.validate(TestEntity(tags = listOf("a", "b", "c", "d"))) is ValidationResult.Failure)
    }

    @Test
    fun `contains should succeed when element is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                contains("admin")
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("admin", "user"))) is ValidationResult.Success)
    }

    @Test
    fun `contains should fail when element is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                contains("admin")
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("user"))) is ValidationResult.Failure)
    }

    @Test
    fun `doesNotContain should succeed when element is absent`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                doesNotContain("guest")
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("admin", "user"))) is ValidationResult.Success)
    }

    @Test
    fun `doesNotContain should fail when element is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                doesNotContain("guest")
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("guest", "user"))) is ValidationResult.Failure)
    }

    @Test
    fun `containsAll should succeed when all elements are present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::permissions {
                containsAll(listOf("read", "write"))
            }
        }

        assertTrue(
            validator.validate(TestEntity(permissions = listOf("read", "write", "delete"))) is ValidationResult.Success
        )
    }

    @Test
    fun `containsAll should fail when any element is missing`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::permissions {
                containsAll(listOf("read", "write"))
            }
        }

        assertTrue(validator.validate(TestEntity(permissions = listOf("read"))) is ValidationResult.Failure)
    }

    @Test
    fun `containsAny should succeed when at least one element is present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                containsAny(listOf("admin", "moderator"))
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("admin", "user"))) is ValidationResult.Success)
        assertTrue(validator.validate(TestEntity(roles = listOf("moderator"))) is ValidationResult.Success)
    }

    @Test
    fun `containsAny should fail when no elements are present`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::roles {
                containsAny(listOf("admin", "moderator"))
            }
        }

        assertTrue(validator.validate(TestEntity(roles = listOf("user"))) is ValidationResult.Failure)
    }

    @Test
    fun `each should succeed when all elements are valid`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
            Author::email { email() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator) hint "All authors must be valid"
            }
        }

        val result = validator.validate(
            Document(
                authors = listOf(
                    Author("John", "john@example.com"),
                    Author("Jane", "jane@example.com")
                )
            )
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `each should fail when any element is invalid`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
            Author::email { email() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator) hint "All authors must be valid"
            }
        }

        val result = validator.validate(
            Document(
                authors = listOf(
                    Author("John", "john@example.com"),
                    Author("", "invalid-email")
                )
            )
        )

        assertTrue(result is ValidationResult.Failure)
        assertEquals(1, result.errorCount)
        assertEquals("authors", result.errors.first().fieldName)
    }

    @Test
    fun `each should succeed for empty collection`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator)
            }
        }

        // Empty list -> all() returns true
        assertTrue(validator.validate(Document(authors = emptyList())) is ValidationResult.Success)
    }

    @Test
    fun `each should skip validation when collection is null`() {
        val authorValidator = AtelierValidator<Author> {
            Author::name { notBlank() }
        }

        val validator = AtelierValidator<Document> {
            Document::authors {
                each(authorValidator)
            }
        }

        assertTrue(validator.validate(Document(authors = null)) is ValidationResult.Success)
    }

    @Test
    fun `collection rules should skip validation when collection is null`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                minSize(1)
                maxSize(10)
                contains("kotlin")
            }
        }

        val result = validator.validate(TestEntity(tags = null))

        // All rules use constrainIfNotNull
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `multiple collection rules should all be validated`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty()
                size(1..10)
                contains("kotlin")
            }
        }

        assertTrue(validator.validate(TestEntity(tags = listOf("kotlin", "java"))) is ValidationResult.Success)
    }

    @Test
    fun `multiple collection rules should produce multiple errors`() {
        val validator = AtelierValidator<TestEntity> {
            TestEntity::tags {
                isNotEmpty() hint "Tags required"
                minSize(2) hint "At least 2 tags"
            }
        }

        val result = validator.validate(TestEntity(tags = emptyList()))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(2, result.errorCount)
    }
}
