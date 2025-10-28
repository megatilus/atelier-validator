/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.results.ValidationResult
import dev.megatilus.atelier.results.ValidatorCode
import dev.megatilus.atelier.validators.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {

    // ===== TEST DATA CLASSES =====

    private data class User(
        val id: Long,
        val name: String,
        val email: String?,
        val age: Int,
        val isActive: Boolean,
        val tags: List<String>,
        val metadata: Map<String, String>,
        val scores: IntArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is User) return false
            if (id != other.id) return false
            if (name != other.name) return false
            if (email != other.email) return false
            if (age != other.age) return false
            if (isActive != other.isActive) return false
            if (tags != other.tags) return false
            if (metadata != other.metadata) return false
            if (!scores.contentEquals(other.scores)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + (email?.hashCode() ?: 0)
            result = 31 * result + age
            result = 31 * result + isActive.hashCode()
            result = 31 * result + tags.hashCode()
            result = 31 * result + metadata.hashCode()
            result = 31 * result + scores.contentHashCode()
            return result
        }
    }

    private data class Product(
        val name: String,
        val price: Double,
        val category: String,
        val inStock: Boolean,
        val ratings: List<Int>?,
        val attributes: Array<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Product) return false
            if (name != other.name) return false
            if (price != other.price) return false
            if (category != other.category) return false
            if (inStock != other.inStock) return false
            if (ratings != other.ratings) return false
            if (!attributes.contentEquals(other.attributes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + price.hashCode()
            result = 31 * result + category.hashCode()
            result = 31 * result + inStock.hashCode()
            result = 31 * result + (ratings?.hashCode() ?: 0)
            result = 31 * result + attributes.contentHashCode()
            return result
        }
    }

    private data class Order(
        val orderId: String,
        val userId: Long?,
        val products: List<Product>,
        val total: Double,
        val discountCode: String?,
        val shippingAddress: Address?,
        val isGift: Boolean
    )

    private data class Address(
        val street: String,
        val city: String,
        val zipCode: String,
        val country: String
    )

    // ===== COMPLEX VALIDATION SCENARIOS =====

    @Test
    fun `complete user validation with all field types`() {
        val userValidator =
            atelierValidator<User> {
                // Numeric validations
                field(User::id).positive("User ID must be positive")
                field(User::age).range(13, 120, "Age must be between 13 and 120")

                // String validations
                field(User::name)
                    .notBlank("Name is required")
                    .minLength(2, "Name must be at least 2 characters")
                    .maxLength(50, "Name cannot exceed 50 characters")
                    .matchesPattern(
                        Regex("[a-zA-Z\\s]+"),
                        "Name can only contain letters and spaces"
                    )

                // Nullable string validations
                field(User::email)
                    .email("Invalid email format")
                    .maxLength(100, "Email too long")

                // Boolean validations
                field(User::isActive).isTrue("User must be active")

                // Collection validations
                field(User::tags)
                    .notEmpty("User must have at least one tag")
                    .maxSize(10, "Too many tags")
                // Tags validation - each tag must be non-blank (custom validation needed)

                // Map validations
                field(User::metadata)
                    .containsKey("source", "Source metadata is required")
                    .maxSize(3, "Too many metadata")

                // Array validations
                // Note: IntArray is not supported by notEmpty validator (it's not Array<Int>)
                // field(User::scores)
                //     .notEmpty("Scores cannot be empty")
                // .exactSize(3, "Must have exactly 3 scores")
                // Array-level validations would need custom constraints
            }

        // Valid user
        val validUser =
            User(
                id = 123L,
                name = "John Doe",
                email = "john.doe@example.com",
                age = 30,
                isActive = true,
                tags = listOf("premium", "verified"),
                metadata = mapOf("source" to "web", "campaign" to "summer2024"),
                scores = intArrayOf(85, 90, 95)
            )

        val validResult = userValidator.validate(validUser)
        assertTrue(validResult.isSuccess, "Valid user should pass validation")

        // Invalid user with multiple issues
        val invalidUser =
            User(
                id = -1L, // negative ID
                name = "J", // too short
                email = "invalid-email", // invalid format
                age = 12, // too young
                isActive = false, // not active
                tags = emptyList(), // empty tags
                metadata = emptyMap(), // missing required key
                scores = intArrayOf(-10, 5) // negative score, wrong size, not sorted
            )

        val invalidResult = userValidator.validate(invalidUser)
        assertTrue(invalidResult.isFailure, "Invalid user should fail validation")

        val failure = invalidResult as ValidationResult.Failure
        assertTrue(failure.errorCount >= 7, "Should have multiple validation errors")

        // Check that all fields have errors (excluding scores since IntArray is not supported)
        val errorFields = failure.errors.map { it.fieldName }.toSet()
        val expectedFields =
            setOf("id", "name", "email", "age", "isActive", "tags", "metadata")
        assertTrue(
            errorFields.containsAll(expectedFields),
            "All problematic fields should have errors"
        )
    }

    @Test
    fun `product validation with conditional logic`() {
        val productValidator =
            atelierValidator<Product> {
                // Basic validations
                field(Product::name)
                    .notBlank("Product name is required")
                    .length(3, 100, "Product name must be between 3 and 100 characters")

                field(Product::price)
                    .positive("Price must be positive")
                    .max(10000.0, "Price cannot exceed $10,000")

                field(Product::category)
                    .notBlank("Category is required")
                // oneOf constraint would need custom validation for strings

                field(Product::inStock).isTrue("Product must be in stock")

                // Nullable collection validation
                field(Product::ratings)
                    .minSize(1, "Must have at least one rating")
                // Each rating validation would need custom constraint

                // Array validation
                field(Product::attributes)
                    .notEmpty("Product must have attributes")
                // Duplicate and content validation would need custom constraints
            }

        // Valid product
        val validProduct =
            Product(
                name = "Wireless Headphones",
                price = 99.99,
                category = "electronics",
                inStock = true,
                ratings = listOf(4, 5, 4, 5),
                attributes = arrayOf("wireless", "noise-cancelling", "bluetooth")
            )

        assertTrue(productValidator.validate(validProduct).isSuccess)

        // Product with multiple validation failures
        val invalidProduct =
            Product(
                name = "AB", // too short
                price = -50.0, // negative
                category = "invalid-category", // not in allowed list
                inStock = false, // not in stock
                ratings = listOf(0, 6, 3), // invalid rating values
                attributes = arrayOf("") // blank attribute
            )

        val result = productValidator.validate(invalidProduct)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertTrue(failure.errorCount >= 3, "Should have multiple validation errors")
    }

    @Test
    fun `nested object validation with address`() {
        val addressValidator =
            atelierValidator<Address> {
                field(Address::street)
                    .notBlank("Street is required")
                    .maxLength(100, "Street address too long")

                field(Address::city)
                    .notBlank("City is required")
                    .maxLength(50, "City name too long")
                    .matchesPattern(
                        Regex("[a-zA-Z\\s]+"),
                        "City name can only contain letters and spaces"
                    )

                field(Address::zipCode)
                    .notBlank("Zip code is required")
                    .matchesPattern(Regex("\\d{5}(-\\d{4})?"), "Invalid zip code format")

                field(Address::country)
                    .notBlank("Country is required")
                    .minLength(2, "Country code must be at least 2 characters")
            }

        val orderValidator =
            atelierValidator<Order> {
                field(Order::orderId)
                    .notBlank("Order ID is required")
                    .matchesPattern(Regex("[A-Z]{2}\\d{6}"), "Order ID must be format: XX123456")

                field(Order::userId).positive("User ID must be positive")

                field(Order::products)
                    .notEmpty("Order must contain products")
                    .maxSize(50, "Too many products in single order")

                field(Order::total)
                    .positive("Total must be positive")
                    .max(100000.0, "Order total too high")

                field(Order::discountCode)
                    .length(5, 20, "Discount code must be 5-20 characters")
                    .matchesPattern(
                        Regex("[A-Z0-9]+"),
                        "Discount code must be alphanumeric uppercase"
                    )

                // Note: For nested object validation, we'd need to implement custom validation
                // or use a more advanced validation approach
            }

        // Valid order
        val validOrder =
            Order(
                orderId = "OR123456",
                userId = 789L,
                products =
                listOf(
                    Product(
                        "Item 1",
                        50.0,
                        "electronics",
                        true,
                        listOf(5),
                        arrayOf("new")
                    )
                ),
                total = 50.0,
                discountCode = "SAVE20",
                shippingAddress = Address("123 Main St", "Springfield", "12345", "US"),
                isGift = false
            )

        assertTrue(orderValidator.validate(validOrder).isSuccess)

        // Invalid order
        val invalidOrder =
            Order(
                orderId = "invalid", // wrong format
                userId = -1L, // negative
                products = emptyList(), // empty
                total = -100.0, // negative
                discountCode = "abc", // wrong format
                shippingAddress = null,
                isGift = true
            )

        val result = orderValidator.validate(invalidOrder)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertTrue(failure.errorCount >= 5)
    }

    @Test
    fun `validateFirst should return only first error in complex validation`() {
        val validator =
            atelierValidator<User> {
                field(User::id).positive("ID must be positive")
                field(User::name).notBlank("Name is required")
                field(User::email).email("Invalid email")
                field(User::age).min(18, "Must be adult")
            }

        val invalidUser =
            User(
                id = -1L,
                name = "",
                email = "invalid",
                age = 15,
                isActive = true,
                tags = emptyList(),
                metadata = emptyMap(),
                scores = intArrayOf()
            )

        val result = validator.validateFirst(invalidUser)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertEquals(1, failure.errorCount, "validateFirst should return only one error")
    }

    @Test
    fun `validation result utility methods work correctly`() {
        val validator =
            atelierValidator<User> {
                field(User::name).notBlank("Name is required")
                field(User::email).email("Invalid email")
                field(User::age).min(18, "Must be adult")
                field(User::name).maxLength(10, "Name too long")
            }

        val invalidUser =
            User(
                id = 1L,
                name = "This is a very long name",
                email = "invalid-email",
                age = 15,
                isActive = true,
                tags = emptyList(),
                metadata = emptyMap(),
                scores = intArrayOf()
            )

        val result = validator.validate(invalidUser)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure

        // Test errorsByField grouping
        val errorsByField = failure.errorsByField
        assertTrue(errorsByField.containsKey("name"))
        assertTrue(errorsByField.containsKey("email"))
        assertTrue(errorsByField.containsKey("age"))

        // Name should have 2 errors (notBlank passes, but maxLength fails)
        val nameErrors = failure.errorsFor("name")
        assertEquals(1, nameErrors.size) // Only maxLength should fail

        // Test firstErrorFor
        val firstEmailError = failure.firstErrorFor("email")
        assertEquals("email", firstEmailError?.fieldName)
        assertEquals(ValidatorCode.INVALID_EMAIL, firstEmailError?.code)

        // Test non-existent field
        val nonExistentErrors = failure.errorsFor("nonExistent")
        assertTrue(nonExistentErrors.isEmpty())

        val nonExistentFirstError = failure.firstErrorFor("nonExistent")
        assertEquals(null, nonExistentFirstError)
    }

    @Test
    fun `validator handles edge cases correctly`() {
        val validator =
            atelierValidator<User> {
                field(User::name).notBlank().minLength(2)
                field(User::email).email()
                field(User::tags).notEmpty()
            }

        // Test with minimal valid data
        val minimalUser =
            User(
                id = 1L,
                name = "Jo",
                email = "jo@example.com",
                age = 25,
                isActive = true,
                tags = listOf("user"),
                metadata = emptyMap(),
                scores = intArrayOf()
            )

        assertTrue(validator.validate(minimalUser).isSuccess)

        // Test with null email (should pass email validation for nullable field)
        val userWithNullEmail =
            User(
                id = 1L,
                name = "John",
                email = null,
                age = 25,
                isActive = true,
                tags = listOf("user"),
                metadata = emptyMap(),
                scores = intArrayOf()
            )

        assertTrue(validator.validate(userWithNullEmail).isSuccess)

        // Test with empty collections where allowed
        val userWithEmptyCollections =
            User(
                id = 1L,
                name = "John",
                email = null,
                age = 25,
                isActive = true,
                tags = listOf("user"), // This still needs to be non-empty
                metadata = emptyMap(), // This can be empty
                scores = intArrayOf() // This can be empty for this validator
            )

        assertTrue(validator.validate(userWithEmptyCollections).isSuccess)
    }

    @Test
    fun `validation error messages are descriptive and helpful`() {
        val validator =
            atelierValidator<Product> {
                field(Product::name)
                    .notBlank("Product name cannot be empty")
                    .length(3, 50, "Product name must be between 3 and 50 characters")

                field(Product::price)
                    .positive("Price must be greater than zero")
                    .max(1000.0, "Price cannot exceed $1,000")

                field(Product::category).notBlank("Category is required")

                field(Product::attributes)
                    .minSize(2, "Product must have at least 2 attributes")
                    .maxSize(10, "Product cannot have more than 10 attributes")
            }

        val invalidProduct =
            Product(
                name = "AB", // Too short
                price = -10.0, // Negative
                category = "invalid", // Not in allowed list
                inStock = true,
                ratings = null,
                attributes = arrayOf("single") // Too few attributes
            )

        val result = validator.validate(invalidProduct)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        val messages = failure.errors.map { it.message }

        assertTrue(messages.contains("Product name must be between 3 and 50 characters"))
        assertTrue(messages.contains("Price must be greater than zero"))
        assertTrue(messages.contains("Product must have at least 2 attributes"))
    }

    @Test
    fun `performance test with large datasets`() {
        val validator =
            atelierValidator<User> {
                field(User::name).notBlank().length(2, 50)
                field(User::email).email()
                field(User::age).range(13, 120)
                field(User::tags).notEmpty().maxSize(10)
            }

        // Create a large list of users to validate
        val users =
            (1..1000).map { i ->
                User(
                    id = i.toLong(),
                    name = "User $i",
                    email = "user$i@example.com",
                    age = 20 + (i % 50),
                    isActive = i % 2 == 0,
                    tags = listOf("tag$i"),
                    metadata = mapOf("id" to i.toString()),
                    scores = intArrayOf(i % 100)
                )
            }

        var successCount = 0
        var failureCount = 0

        users.forEach { user ->
            when (validator.validate(user)) {
                is ValidationResult.Success -> successCount++
                is ValidationResult.Failure -> failureCount++
            }
        }

        assertEquals(1000, successCount + failureCount)
        assertEquals(1000, successCount) // All should be valid
        assertEquals(0, failureCount)
    }

    @Test
    fun `validator reuse is safe and efficient`() {
        val validator =
            atelierValidator<User> {
                field(User::name).notBlank()
                field(User::email).email()
            }

        val user1 =
            User(
                1L,
                "John",
                "john@example.com",
                25,
                true,
                listOf("a"),
                emptyMap(),
                intArrayOf()
            )
        val user2 =
            User(
                2L,
                "Jane",
                "jane@example.com",
                30,
                false,
                listOf("b"),
                emptyMap(),
                intArrayOf()
            )
        val user3 =
            User(3L, "", "invalid", 35, true, listOf("c"), emptyMap(), intArrayOf()) // Invalid

        // Validate multiple users with same validator instance
        assertTrue(validator.validate(user1).isSuccess)
        assertTrue(validator.validate(user2).isSuccess)
        assertTrue(validator.validate(user3).isFailure)

        // Validator should maintain state correctly
        assertTrue(validator.validate(user1).isSuccess) // Revalidate first user
    }

    @Test
    fun `custom validation scenarios with real-world complexity`() {
        data class BankAccount(
            val accountNumber: String,
            val routingNumber: String,
            val balance: Double,
            val accountType: String,
            val isActive: Boolean,
            val transactions: List<Double>,
            val allowedCountries: Set<String>
        )

        val bankAccountValidator =
            atelierValidator<BankAccount> {
                // Account number validation (simplified)
                field(BankAccount::accountNumber)
                    .notBlank("Account number is required")
                    .exactLength(10, "Account number must be exactly 10 digits")
                    .matchesPattern(Regex("\\d{10}"), "Account number must contain only digits")

                // Routing number validation
                field(BankAccount::routingNumber)
                    .exactLength(9, "Routing number must be 9 digits")
                    .matchesPattern(Regex("\\d{9}"), "Routing number must contain only digits")

                // Balance validation
                field(BankAccount::balance)
                    .min(0.0, "Balance cannot be negative")
                    .max(10000000.0, "Balance too high for this account type")

                // Account type validation
                field(BankAccount::accountType)
                    .notBlank("Account type is required")

                // Status validation
                field(BankAccount::isActive).isTrue("Account must be active")

                // Transaction history
                field(BankAccount::transactions)
                    .maxSize(1000, "Too many transactions to validate")
                // Transaction amount validation would need custom constraint

                // Allowed countries
                field(BankAccount::allowedCountries)
                    .notEmpty("Must specify at least one allowed country")
                // Country code validation would need custom constraint
            }

        val validAccount =
            BankAccount(
                accountNumber = "1234567890",
                routingNumber = "987654321",
                balance = 1500.50,
                accountType = "checking",
                isActive = true,
                transactions = listOf(100.0, -50.0, 200.0, -75.25),
                allowedCountries = setOf("US", "CA", "GB")
            )

        assertTrue(bankAccountValidator.validate(validAccount).isSuccess)

        val invalidAccount =
            BankAccount(
                accountNumber = "123", // too short
                routingNumber = "abc123456", // contains letters
                balance = -100.0, // negative
                accountType = "credit", // invalid type
                isActive = false, // not active
                transactions = listOf(100000.0), // transaction too large
                allowedCountries = setOf("usa", "canada") // wrong format
            )

        val result = bankAccountValidator.validate(invalidAccount)
        assertTrue(result.isFailure)

        val failure = result as ValidationResult.Failure
        assertTrue(failure.errorCount >= 5, "Should have multiple validation errors")

        // Verify specific error codes
        val errorCodes = failure.errors.map { it.code }.toSet()
        assertTrue(errorCodes.contains(ValidatorCode.OUT_OF_RANGE))
        assertTrue(errorCodes.contains(ValidatorCode.INVALID_FORMAT))
        assertTrue(errorCodes.contains(ValidatorCode.INVALID_VALUE))
    }
}
