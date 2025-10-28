# 🎨 Atelier Validator

A modern, type-safe validation library for **Kotlin Multiplatform** that makes data validation elegant, readable, and maintainable.

> [!IMPORTANT]
> Full documentation website coming soon

[![Maven Central](https://img.shields.io/maven-central/v/dev.megatilus.atelier/validator-core.svg?label=Maven%20Central)](https://search.maven.org/artifact/dev.megatilus.atelier/validator-core)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## Philosophy

**Type Safety First** — Rely on Kotlin’s type system to catch misconfigurations at compile time, not at runtime.  
**No Magic** — No reflection, no code generation, no hidden behavior. Just explicit, honest Kotlin.  
**Composable** — Build complex validation logic by composing small, readable validators.  
**Performance-Oriented** — Designed for speed and minimal allocations.  
**Developer-Centric** — A fluent, expressive API that reads like documentation.

---

## Features

- **Type-Safe**: Leverage Kotlin’s type system to catch validation issues at compile time
- **Fluent API**: Chain readable validation rules using a Kotlin DSL
- **Modular**: Import only the modules you need — core or optional extensions
- **Extensible**: Build your own validators and constraints easily
- **Performant**: Zero-reflection design ensures optimal performance on every platform
- **Multiplatform**: Works on JVM, JS, Native, iOS, Android, and more
- **Detailed Results**: Get field names, error codes, and clear messages
- **Flexible Modes**: Choose between fail-fast or collect-all validation strategies

---

## Installation

Add the dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core validation library
    implementation("dev.megatilus.atelier:validator-core:${version}")

    // Optional: kotlinx-datetime integration
    implementation("dev.megatilus.atelier:validator-kotlinx-datetime:${version}")
}
```

---

## Quick Start

Define your data class and create a validator:

```kotlin
data class User(
    val name: String,
    val email: String,
    val password: String,
    val age: Int,
    val tags: List<String>
)

val userValidator = atelierValidator<User> {
    User::name {
        notBlank()
        minLength(2)
        maxLength(50)
    }

    User::email {
        notBlank()
        email()
    }
    
    User::password {
        notBlank()
        strongPassword()
    }

    User::age {
        range(18, 120)
    }

    User::tags {
        notEmpty()
        minSize(1)
        maxSize(5)
    }
}
```

### Validate your data

```kotlin
val user = User(
    name = "John Doe",
    email = "john@example.com",
    password = "SecureP@ss123",
    age = 25,
    tags = listOf("developer", "kotlin")
)

val result = userValidator.validate(user)

when (result) {
    is ValidationResult.Success -> println("✅ Validation successful!")
    is ValidationResult.Failure -> {
        result.errors.forEach { error ->
            println("❌ ${error.fieldName}: ${error.message}")
        }
    }
}
```

Or with detailed output:

```kotlin
if (result is ValidationResult.Failure) {
    result.errors.forEach { error ->
        println("Field: ${error.fieldName}")
        println("Message: ${error.message}")
        println("Code: ${error.code}")
        println("Actual Value: ${error.actualValue}")
    }
}
```

---

## Custom Validators

Define custom validators effortlessly:

```kotlin
fun <T : Any> FieldValidatorBuilder<T, String>.username(
    message: String? = null
): FieldValidatorBuilder<T, String> = constraintExtension(
    hint = message ?: "Invalid username format",
    code = ValidatorCode.INVALID_FORMAT,
    predicate = { 
        it.length in 3..20 && 
            it.all { c -> c.isLetterOrDigit() || c == '_' || c == '-' }
    }
)

// Usage
val validator = atelierValidator<User> {
    User::username {
        username()
    }
}
```

---

## Validation Modes

### Collect All Errors (default)
Returns all validation errors

```kotlin
val result = validator.validate(user)
```

### Fail-Fast Mode
Stops at the first validation error

```kotlin
val result = validator.validateFirst(user)
```

---

## Error Details

Each validation error provides comprehensive context:

```kotlin
data class ValidationErrorDetail(
    val fieldName: String,    // e.g., "email"
    val message: String,      // e.g., "Invalid email format"
    val code: ValidatorCode,  // e.g., ValidatorCode.INVALID_FORMAT
    val actualValue: String   // The actual value that failed validation
)
```

### Working with Errors

```kotlin
when (result) {
    is ValidationResult.Failure -> {
        val allErrors = result.errors
        val emailErrors = result.errorsFor("email")
        val firstError = result.firstErrorFor("email")
        val errorsByField = result.errorsByField
        val count = result.errorCount
    }
}
```

--- 

## Modules

### `validator-core`
Core validation library with standard validators for strings, numbers, collections, booleans, arrays, sets, and maps.

### `validator-kotlinx-datetime`
Extension module providing date and time validators based on `kotlinx-datetime`.

---

## Contributing

Contributions are welcome!

---

## Roadmap

- ✅ Core validators for strings, numbers, collections, booleans, arrays, sets, and maps
- ✅ kotlinx-datetime integration (extension module)
- 🚧 Ktor integration
    - **server**
    - **client**
- 🚧 Sample app
