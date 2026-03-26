/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validator.results.ValidationResult
import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.rules.alphanumeric
import dev.megatilus.atelier.validator.rules.each
import dev.megatilus.atelier.validator.rules.email
import dev.megatilus.atelier.validator.rules.isNotEmpty
import dev.megatilus.atelier.validator.rules.isTrue
import dev.megatilus.atelier.validator.rules.matches
import dev.megatilus.atelier.validator.rules.max
import dev.megatilus.atelier.validator.rules.maxLength
import dev.megatilus.atelier.validator.rules.maxSize
import dev.megatilus.atelier.validator.rules.min
import dev.megatilus.atelier.validator.rules.minLength
import dev.megatilus.atelier.validator.rules.minSize
import dev.megatilus.atelier.validator.rules.nested
import dev.megatilus.atelier.validator.rules.notBlank
import dev.megatilus.atelier.validator.rules.notNull
import dev.megatilus.atelier.validator.rules.strongPassword
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrationTest {
    
    @Test
    fun `user registration should validate all fields`() {
        data class UserRegistration(
            val username: String?,
            val email: String?,
            val password: String?,
            val confirmPassword: String?,
            val age: Int?,
            val termsAccepted: Boolean?
        )

        val validator = AtelierValidator<UserRegistration> {
            UserRegistration::username {
                notBlank() hint "Username is required"
                minLength(3) hint "Username must be at least 3 characters"
                maxLength(20) hint "Username must be at most 20 characters"
                alphanumeric() hint "Username must contain only letters and numbers"
            }

            UserRegistration::email {
                notBlank() hint "Email is required"
                email() hint "Invalid email format"
            }

            UserRegistration::password {
                notBlank() hint "Password is required"
                strongPassword(
                    minLength = 8,
                    requireUppercase = true,
                    requireLowercase = true,
                    requireDigit = true,
                    requireSpecialChar = true
                ) hint "Password is too weak"
            }

            UserRegistration::age {
                notNull() hint "Age is required"
                min(18) hint "You must be at least 18 years old"
                max(120) hint "Invalid age"
            }

            UserRegistration::termsAccepted {
                isTrue() hint "You must accept the terms and conditions"
            }
        }

        // Valid registration
        val validUser = UserRegistration(
            username = "john123",
            email = "john@example.com",
            password = "SecurePass1!",
            confirmPassword = "SecurePass1!",
            age = 25,
            termsAccepted = true
        )

        assertTrue(validator.validate(validUser) is ValidationResult.Success)

        // Invalid registration
        val invalidUser = UserRegistration(
            username = "jo",
            email = "invalid-email",
            password = "weak",
            confirmPassword = "different",
            age = 16,
            termsAccepted = false
        )

        val result = validator.validate(invalidUser)
        assertTrue(result is ValidationResult.Failure)
        assertTrue(result.errorCount >= 5) // Multiple validation errors
    }

    @Test
    fun `product validation should validate all fields`() {
        data class Product(
            val sku: String?,
            val name: String?,
            val description: String?,
            val price: Double?,
            val stock: Int?,
            val tags: List<String>?
        )

        val validator = AtelierValidator<Product> {
            Product::sku {
                notBlank()
                matches(Regex("^PROD-\\d{6}$")) hint "SKU must match format PROD-XXXXXX"
            }

            Product::name {
                notBlank()
                minLength(3)
                maxLength(100)
            }

            Product::description {
                notBlank()
                maxLength(1000)
            }

            Product::price {
                notNull()
                min(0.01)
                max(999999.99)
            }

            Product::stock {
                notNull()
                min(0)
            }

            Product::tags {
                isNotEmpty() hint "At least one tag is required"
                minSize(1)
                maxSize(10)
            }
        }

        val validProduct = Product(
            sku = "PROD-123456",
            name = "Awesome Product",
            description = "This is an awesome product description",
            price = 29.99,
            stock = 100,
            tags = listOf("electronics", "gadgets")
        )

        assertTrue(validator.validate(validProduct) is ValidationResult.Success)
    }

    @Test
    fun `nested validation should work for complex objects`() {
        data class Address(
            val street: String?,
            val city: String?,
            val zipCode: String?,
            val country: String?
        )

        data class ContactInfo(
            val phone: String?,
            val email: String?
        )

        data class Company(
            val name: String?,
            val address: Address?,
            val contact: ContactInfo?
        )

        val addressValidator = AtelierValidator<Address> {
            Address::street { notBlank() }
            Address::city { notBlank() }
            Address::zipCode {
                notBlank()
                matches(Regex("^\\d{5}$"))
            }
            Address::country { notBlank() }
        }

        val contactValidator = AtelierValidator<ContactInfo> {
            ContactInfo::phone {
                notBlank()
                matches(Regex("^\\+?[0-9]{10,15}$"))
            }
            ContactInfo::email {
                notBlank()
                email()
            }
        }

        val companyValidator = AtelierValidator<Company> {
            Company::name {
                notBlank()
                minLength(2)
            }
            Company::address {
                nested(addressValidator)
            }
            Company::contact {
                nested(contactValidator)
            }
        }

        val validCompany = Company(
            name = "Acme Corp",
            address = Address(
                street = "123 Main St",
                city = "Paris",
                zipCode = "75001",
                country = "France"
            ),
            contact = ContactInfo(
                phone = "+33612345678",
                email = "contact@acme.com"
            )
        )

        assertTrue(companyValidator.validate(validCompany) is ValidationResult.Success)

        // Invalid nested object
        val invalidCompany = validCompany.copy(
            address = Address(
                street = "",
                city = "",
                zipCode = "invalid",
                country = ""
            )
        )

        assertTrue(companyValidator.validate(invalidCompany) is ValidationResult.Failure)
    }

    @Test
    fun `collection validation should validate all elements`() {
        data class Tag(val name: String?, val color: String?)
        data class Article(val title: String?, val tags: List<Tag>?)

        val tagValidator = AtelierValidator<Tag> {
            Tag::name {
                notBlank()
                minLength(2)
                maxLength(20)
            }
            Tag::color {
                notBlank()
                matches(Regex("^#[0-9A-Fa-f]{6}$")) hint "Color must be hex format"
            }
        }

        val articleValidator = AtelierValidator<Article> {
            Article::title {
                notBlank()
                minLength(5)
                maxLength(200)
            }
            Article::tags {
                isNotEmpty()
                minSize(1)
                maxSize(5)
                each(tagValidator) hint "All tags must be valid"
            }
        }

        val validArticle = Article(
            title = "How to Build Validators",
            tags = listOf(
                Tag(name = "kotlin", color = "#7F52FF"),
                Tag(name = "validation", color = "#00FF00")
            )
        )

        assertTrue(articleValidator.validate(validArticle) is ValidationResult.Success)

        // Invalid tags
        val invalidArticle = validArticle.copy(
            tags = listOf(
                Tag(name = "", color = "red"), // Invalid
                Tag(name = "valid", color = "#FF0000")
            )
        )

        assertTrue(articleValidator.validate(invalidArticle) is ValidationResult.Failure)
    }

    @Test
    fun `different validators for different scenarios`() {
        data class User(
            val email: String?,
            val password: String?,
            val name: String?
        )

        val registrationValidator = AtelierValidator<User> {
            User::email {
                notBlank()
                email()
            }
            User::password {
                notBlank()
                strongPassword(minLength = 12)
            }
            User::name {
                notBlank()
                minLength(2)
            }
        }

        val updateValidator = AtelierValidator<User> {
            User::name {
                minLength(2)
            }
        }

        val user = User(
            email = "john@example.com",
            password = "weak",
            name = "John"
        )

        assertTrue(registrationValidator.validate(user) is ValidationResult.Failure)

        assertTrue(updateValidator.validate(user) is ValidationResult.Success)
    }

    @Test
    fun `validation should collect all errors across all fields`() {
        data class Form(
            val field1: String?,
            val field2: String?,
            val field3: String?,
            val field4: String?
        )

        val validator = AtelierValidator<Form> {
            Form::field1 { notBlank() hint "Field 1 required" }
            Form::field2 { notBlank() hint "Field 2 required" }
            Form::field3 { notBlank() hint "Field 3 required" }
            Form::field4 { notBlank() hint "Field 4 required" }
        }

        val result = validator.validate(Form(null, null, null, null))

        assertTrue(result is ValidationResult.Failure)
        assertEquals(4, result.errorCount)
        assertEquals(1, result.errorsFor("field1").size)
        assertEquals(1, result.errorsFor("field2").size)
        assertEquals(1, result.errorsFor("field3").size)
        assertEquals(1, result.errorsFor("field4").size)
    }
}
