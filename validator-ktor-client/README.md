# 🎨 Atelier Validator - Ktor Client

Type-safe validation for Ktor HTTP client responses.

## Installation

```kotlin
dependencies {
    implementation("dev.megatilus.atelier:validator-ktor-client:${version}")
}
```

## Quick Start

### 1. Define your validators

```kotlin
import dev.megatilus.atelier.*
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int
)

val userValidator = atelierValidator<User> {
    User::id { notBlank() }
    User::name { notBlank(); minLength(2); maxLength(50) }
    User::email { notBlank(); email() }
    User::age { min(0); max(150) }
}
```

### 2. Configure your HTTP client

```kotlin
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    
    install(AtelierValidatorClient) {
        // Register validators
        register(userValidator)
        
        // Optional: Configure accepted status codes
        acceptStatusCodeRange(200..299)  // Default: 2xx only
        
        // Optional: Enable automatic validation
        useAutomaticValidation = true  // Default: true
    }
}
```

### 3. Make validated requests

```kotlin
// Automatic validation (throws exception on failure)
val user: User = client.get("https://api.example.com/users/1").body()

// Or use shortcuts
val user = client.getAndValidate<User>("https://api.example.com/users/1")

// Manual validation with error handling
try {
    val user = client.get("https://api.example.com/users/1")
        .bodyAndValidate<User>()
    println("Valid user: ${user.name}")
} catch (e: AtelierClientValidationException) {
    println("Validation failed:")
    e.validationResult.errors.forEach { error ->
        println("  ${error.fieldName}: ${error.message}")
    }
} catch (e: AtelierClientStatusException) {
    println("HTTP error: ${e.statusCode.value}")
}
```

## Features

### ✅ Automatic Validation

When enabled, all responses are automatically validated:

```kotlin
install(AtelierValidatorClient) {
    register(userValidator)
    useAutomaticValidation = true
}

// Validation happens automatically
val user: User = client.get("/users/1").body()
```

### 🔍 Manual Validation

Disable automatic validation for fine-grained control:

```kotlin
install(AtelierValidatorClient) {
    register(userValidator)
    useAutomaticValidation = false
}

// Explicit validation
val response = client.get("/users/1")
val user = response.bodyAndValidate<User>()
```

### 🎯 Status Code Validation

Configure which HTTP status codes are acceptable:

```kotlin
install(AtelierValidatorClient) {
    register(userValidator)
    
    // Accept only specific codes
    acceptStatusCodes(
        HttpStatusCode.OK,
        HttpStatusCode.Created
    )
    
    // Or accept a range
    acceptStatusCodeRange(200..299)
}
```

### 🛡️ Error Handling

#### Exception-based (default)

```kotlin
try {
    val user = client.getAndValidate<User>("/users/1")
    // Process valid user
} catch (e: AtelierClientValidationException) {
    // Handle validation errors
    println("Invalid response data:")
    e.validationResult.errors.forEach {
        println("  ${it.fieldName}: ${it.message}")
    }
} catch (e: AtelierClientStatusException) {
    // Handle HTTP status errors
    println("HTTP ${e.statusCode.value}: ${e.statusCode.description}")
}
```

#### Callback-based

```kotlin
install(AtelierValidatorClient) {
    register(userValidator)
    
    throwOnValidationError = false
    
    onValidationError = { failure ->
        logger.error("Validation failed: ${failure.errors}")
        metrics.increment("validation.errors")
    }
    
    onStatusCodeError = { statusCode, body ->
        logger.error("HTTP error $statusCode: $body")
    }
}
```

#### Null-based

```kotlin
val user = client.get("/users/1").bodyAndValidateOrNull<User>()

if (user != null) {
    println("Valid user: ${user.name}")
} else {
    println("Validation or HTTP error occurred")
}
```

### 🔄 Custom Error Handling per Request

```kotlin
val user = client.get("/users/1").bodyAndValidate<User> { failure ->
    if (failure.hasErrorFor("email")) {
        println("Email validation failed")
        // Custom handling for email errors
    } else {
        println("General validation error")
    }
}
```

## API Reference

### HTTP Shortcuts

Combine request and validation in one call:

```kotlin
// GET
val user = client.getAndValidate<User>("/users/1")

// POST
val createdUser = client.postAndValidate<User>("/users") {
    setBody(newUser)
}

// PUT
val updatedUser = client.putAndValidate<User>("/users/1") {
    setBody(user)
}

// PATCH
val patchedUser = client.patchAndValidate<User>("/users/1") {
    setBody(partialUpdate)
}

// DELETE
val response = client.deleteAndValidate<DeleteResponse>("/users/1")
```

### Response Extensions

```kotlin
val response = client.get("/users/1")

// Validate and get body (throws on error)
val user = response.bodyAndValidate<User>()

// Validate with custom error handler (returns null on error)
val user = response.bodyAndValidate<User> { failure ->
    println("Validation failed: ${failure.errors}")
}

// Get body or null (no exceptions)
val user = response.bodyAndValidateOrNull<User>()

// Check if valid without throwing
if (response.isValid<User>()) {
    val user = response.body<User>()
    // Process user
}

// Manual status code validation
response.validateStatusCode()
```

### Batch Validation

Validate multiple objects at once:

```kotlin
@Serializable
data class BatchResponse(val users: List<User>)

val response = client.get("/users/batch").body<BatchResponse>()
val (valid, invalid) = client.validateBatch(response.users)

println("Valid: ${valid.size}, Invalid: ${invalid.size}")

invalid.forEach { (user, failure) ->
    println("User ${user.id} errors:")
    failure.errors.forEach { error ->
        println("  ${error.fieldName}: ${error.message}")
    }
}
```

## Advanced Usage

### Custom Validators

```kotlin
fun <T : Any> FieldValidatorBuilder<T, String>.uuid(
    message: String? = null
): FieldValidatorBuilder<T, String> = 
    constraintForExtension(
        hint = message ?: "Invalid UUID format",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { 
            it.matches(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
        }
    )

val userValidator = atelierValidator<User> {
    User::id { uuid() }
    User::email { email() }
}
```

### Multiple Validators

```kotlin
val client = HttpClient {
    install(AtelierValidatorClient) {
        register(userValidator)
        register(productValidator)
        register(orderValidator)
        
        // Each type is validated automatically
    }
}
```

### Conditional Validation

```kotlin
val response = client.get("/users/1")

if (response.status == HttpStatusCode.OK) {
    if (response.isValid<User>()) {
        val user = response.body<User>()
        // Guaranteed valid
    } else {
        // Invalid but status was OK
        println("Response data is invalid")
    }
}
```

### Integration with Other Plugins

```kotlin
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    
    install(Logging) {
        level = LogLevel.INFO
    }
    
    install(AtelierValidatorClient) {
        register(userValidator)
        
        onValidationError = { failure ->
            // Log validation errors
            logger.error("Validation failed: ${failure.errorCount} errors")
        }
    }
    
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
    }
}
```

## Error Types

### AtelierClientValidationException

Thrown when response body fails validation:

```kotlin
try {
    val user = client.getAndValidate<User>("/users/1")
} catch (e: AtelierClientValidationException) {
    println("URL: ${e.url}")
    println("Status: ${e.statusCode}")
    println("Errors: ${e.validationResult.errorCount}")
    
    e.validationResult.errors.forEach { error ->
        println("  ${error.fieldName}: ${error.message}")
    }
}
```

### AtelierClientStatusException

Thrown when HTTP status code is not acceptable:

```kotlin
try {
    val user = client.getAndValidate<User>("/users/1")
} catch (e: AtelierClientStatusException) {
    println("Status: ${e.statusCode.value}")
    println("URL: ${e.url}")
    
    if (e.isClientError) {
        println("Client error (4xx)")
    } else if (e.isServerError) {
        println("Server error (5xx)")
    }
    
    val body = e.getResponseBodyOrDefault("No body available")
    println("Response: $body")
}
```

## Best Practices

### 1. Register all validators at startup

```kotlin
val client = HttpClient {
    install(AtelierValidatorClient) {
        // Register all validators once
        register(userValidator)
        register(productValidator)
        register(orderValidator)
    }
}
```

### 2. Use automatic validation by default

```kotlin
install(AtelierValidatorClient) {
    register(userValidator)
    useAutomaticValidation = true  // Default
}

// Simple and safe
val user: User = client.get("/users/1").body()
```

### 3. Handle errors appropriately

```kotlin
try {
    val user = client.getAndValidate<User>("/users/1")
    processUser(user)
} catch (e: AtelierClientValidationException) {
    // Log and handle validation errors
    logger.error("Invalid response", e)
    // Maybe retry or use fallback data
} catch (e: AtelierClientStatusException) {
    // Log and handle HTTP errors
    logger.error("HTTP error", e)
    // Maybe retry or show error to user
}
```

### 4. Use shortcuts for common operations

```kotlin
// Instead of:
val user = client.get("/users/1").bodyAndValidate<User>()

// Use:
val user = client.getAndValidate<User>("/users/1")
```

### 5. Validate early in your pipeline

```kotlin
suspend fun fetchUser(id: String): User {
    // Validation happens here, throwing exceptions early
    return client.getAndValidate<User>("/users/$id")
}

// Caller knows the user is valid
val user = fetchUser("123")
```

## Migration from Manual Validation

### Before (manual validation)

```kotlin
val response = client.get("/users/1")
val user = response.body<User>()

// Manual checks
if (user.email.isBlank() || !isValidEmail(user.email)) {
    throw IllegalArgumentException("Invalid email")
}
if (user.age < 0 || user.age > 150) {
    throw IllegalArgumentException("Invalid age")
}
```

### After (with Atelier)

```kotlin
val userValidator = atelierValidator<User> {
    User::email { notBlank(); email() }
    User::age { min(0); max(150) }
}

val client = HttpClient {
    install(AtelierValidatorClient) {
        register(userValidator)
    }
}

// Automatic validation
val user = client.getAndValidate<User>("/users/1")
// user is guaranteed to be valid
```

## Contributing

Contributions are welcome! Please see the main [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

Apache 2.0 - See [LICENSE](../LICENSE) for details.
