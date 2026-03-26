/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.validator.results

/**
 * Represents the result of a validation operation.
 */
public sealed class ValidationResult {
    public data object Success : ValidationResult()

    public data class Failure(val errors: List<ValidationError>) : ValidationResult() {
        public constructor(error: ValidationError) : this(listOf(error))

        public val errorCount: Int
            get() = errors.size

        public val errorsByField: Map<String, List<ValidationError>>
            get() = errors.groupBy { it.fieldName }

        public fun errorsFor(fieldName: String): List<ValidationError> =
            errors.filter { it.fieldName == fieldName }

        public fun firstErrorFor(fieldName: String): ValidationError? =
            errors.firstOrNull { it.fieldName == fieldName }

        /**
         * Returns errors as a simple map: field -> message.
         *
         * Perfect for REST APIs.
         *
         * Example:
         * ```json
         * {
         *   "email": "Must be a valid email",
         *   "password": "Must be at least 8 characters"
         * }
         * ```
         */
        public fun toFieldMessageMap(): Map<String, String> {
            return errors.associate { it.fieldName to it.message }
        }

        /**
         * Returns errors as a map: field -> list of messages.
         *
         * Useful when multiple errors per field.
         *
         * Example:
         * ```json
         * {
         *   "password": [
         *     "Must be at least 8 characters",
         *     "Must contain at least one uppercase letter"
         *   ]
         * }
         * ```
         */
        public fun toFieldMessagesMap(): Map<String, List<String>> {
            return errors
                .groupBy { it.fieldName }
                .mapValues { (_, errors) -> errors.map { it.message } }
        }

        /**
         * Returns errors as a detailed list with code and message.
         *
         * Perfect for detailed API responses or debugging.
         *
         * Example:
         * ```json
         * [
         *   {
         *     "field": "email",
         *     "code": "invalid_email",
         *     "message": "Must be a valid email"
         *   },
         *   {
         *     "field": "password",
         *     "code": "too_short",
         *     "message": "Must be at least 8 characters"
         *   }
         * ]
         * ```
         */
        public fun toDetailedList(): List<ErrorDetail> {
            return errors.map { error ->
                ErrorDetail(
                    field = error.fieldName,
                    code = error.codeString,
                    message = error.message,
                    actualValue = error.actualValue
                )
            }
        }

        /**
         * Returns errors grouped by field with details.
         *
         * Example:
         * ```json
         * {
         *   "password": [
         *     { "code": "too_short", "message": "Must be at least 8 characters" },
         *     { "code": "weak_password", "message": "Must contain uppercase" }
         *   ]
         * }
         * ```
         */
        public fun toGroupedDetails(): Map<String, List<ErrorDetail>> {
            return errors
                .groupBy { it.fieldName }
                .mapValues { (_, errors) ->
                    errors.map { error ->
                        ErrorDetail(
                            field = error.fieldName,
                            code = error.codeString,
                            message = error.message,
                            actualValue = error.actualValue
                        )
                    }
                }
        }

        /**
         * Returns a flat list of error messages.
         *
         * Example:
         * ```json
         * [
         *   "email: Must be a valid email",
         *   "password: Must be at least 8 characters"
         * ]
         * ```
         */
        public fun toMessageList(): List<String> {
            return errors.map { it.toSimpleString() }
        }
    }

    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is Failure

    /**
     * Returns errors if failure, empty list if success.
     */
    public fun errorsOrEmpty(): List<ValidationError> {
        return when (this) {
            is Success -> emptyList()
            is Failure -> errors
        }
    }

    /**
     * Returns error count (0 if success).
     */
    public fun errorCount(): Int {
        return when (this) {
            is Success -> 0
            is Failure -> errorCount
        }
    }
}

/**
 * Detailed error representation for API responses.
 */
public data class ErrorDetail(
    val field: String,
    val code: String,
    val message: String,
    val actualValue: String? = null
)
