# Atelier Validator

A modern, type-safe validation library for **Kotlin Multiplatform** that makes data validation elegant, readable, and maintainable.

> [!IMPORTANT]
> Version 3.0.0 is in active development — major rewrite with a new API.
> Documentation website and sample app coming soon.

![KMP](https://img.shields.io/badge/KMP-JVM%20%7C%20JS%20%7C%20iOS%20%7C%20tvOS%20%7C%20watchOS%20%7C%20Native-blue)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/dev.megatilus.atelier/validator-core.svg?label=Maven%20Central)](https://search.maven.org/artifact/dev.megatilus.atelier/validator-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## Why Atelier Validator

- **Type Safety First** — Rely on Kotlin's type system to catch misconfigurations at compile time, not at runtime.
- **No Magic** — No reflection, no code generation, no hidden behavior. Just explicit, honest Kotlin.
- **Composable** — Build complex validation logic by composing small, readable validators.
- **Performance-Oriented** — Designed for speed and minimal allocations.
- **Developer-Centric** — A fluent, expressive API that reads like documentation.

---

## Features

- **Type-Safe** — Leverage Kotlin's type system to catch validation issues at compile time
- **Fluent DSL** — Chain readable validation rules using a Kotlin DSL
- **Modular** — Import only the modules you need
- **Extensible** — Build your own reusable rules with a single annotation
- **Performant** — Zero-reflection design ensures optimal performance on every platform
- **Multiplatform** — Works on JVM, JS, iOS, and Native
- **Detailed Results** — Get field names, error codes, and clear messages
- **Flexible Modes** — Choose between fail-fast or collect-all validation strategies

---

## Installation

Add the dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core validation library
    implementation("dev.megatilus.atelier:validator-core:$version")

    // Optional: kotlinx-datetime integration
    implementation("dev.megatilus.atelier:validator-kotlinx-datetime:$version")

    // Optional: Ktor server integration
    implementation("dev.megatilus.atelier:validator-ktor-server:$version")

    // Optional: Ktor client integration
    implementation("dev.megatilus.atelier:validator-ktor-client:$version")
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
    val age: Int?,
    val tags: List<String>?
)

val userValidator = AtelierValidator<User> {
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
        isNotEmpty()
        minSize(1)
        maxSize(5)
    }
}
```

### Validate your data

```kotlin
val result = userValidator.validate(user)

when (result) {
    is ValidationResult.Success -> println("✅ Validation passed")
    is ValidationResult.Failure -> {
        result.errors.forEach { error ->
            println("❌ ${error.fieldName}: ${error.message}")
        }
    }
}
```

### Fail-fast mode

```kotlin
val result = userValidator.validateFirst(user)
```

---

## Customizing rules

Override the message or error code on any rule with `hint` and `withCode`:

```kotlin
User::email {
    notBlank() hint "Email is required"
    email() hint "Invalid email format" withCode ValidationErrorCode("custom_email")
}
```

### Custom inline rules

```kotlin
User::name {
    customRule { it == null || it.startsWith("user_") } hint "Must start with user_"
}
```

### Reusable rule extensions

```kotlin
@OptIn(InternalRuleDefinitionApi::class)
fun ValidationRule<String?>.username(): Rule = constrainIfNotNull(
    message = "Must be 3–20 characters, letters, digits, _ or - only",
    code = ValidationErrorCode("invalid_username"),
    predicate = { it.length in 3..20 && it.all { c -> c.isLetterOrDigit() || c == '_' || c == '-' } }
)

// Usage
User::username {
    username() hint "Invalid username"
}
```

---

## Working with errors

```kotlin
if (result is ValidationResult.Failure) {
    val allErrors     = result.errors
    val emailErrors   = result.errorsFor("email")
    val firstEmail    = result.firstErrorFor("email")
    val byField       = result.errorsByField
    val count         = result.errorCount

    // Convenient formats
    val fieldMessages = result.toFieldMessageMap()   // Map<String, String>
    val detailed      = result.toDetailedList()      // List<ErrorDetail>
}
```

---

## Nested validation

```kotlin
val addressValidator = AtelierValidator<Address> {
    Address::city { notBlank() }
    Address::zipCode { matches(Regex("^\\d{5}$")) }
}

val userValidator = AtelierValidator<User> {
    User::name { notBlank() }
    User::address { nested(addressValidator) }
}
```

## Collection validation

```kotlin
val tagValidator = AtelierValidator<Tag> {
    Tag::name { notBlank(); minLength(2) }
}

val articleValidator = AtelierValidator<Article> {
    Article::tags {
        isNotEmpty()
        each(tagValidator) hint "All tags must be valid"
    }
}
```

---

## Ktor integration

### Server

```kotlin
install(AtelierValidatorServer) {
    register(userValidator)
}

post("/users") {
    val user = call.receiveAndValidate<User>() ?: return@post
    call.respond(HttpStatusCode.Created, user)
}
```

### Client

```kotlin
val client = HttpClient {
    install(AtelierValidatorClient) {
        register(userValidator)
    }
}

val user = client.getAndValidate<User>("https://api.example.com/users/1")
```

---

## Modules

| Module | Description |
|--------|-------------|
| `validator-core` | Core validation rules — strings, numbers, collections, booleans, arrays, maps |
| `validator-kotlinx-datetime` | Date and time validators based on `kotlinx-datetime` |
| `validator-ktor-server` | Ktor server plugin with automatic and manual validation |
| `validator-ktor-client` | Ktor client plugin for response validation |

---

## Roadmap

- ✅ Core validators — strings, numbers, collections, booleans, arrays, sets, maps
- ✅ kotlinx-datetime integration
- ✅ Ktor server integration
- ✅ Ktor client integration
- 🚧 Sample app
- 🚧 Documentation website

---

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

---

## License

Licensed under the [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0).
