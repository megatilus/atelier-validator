/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalTime::class)

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.Rule
import dev.megatilus.atelier.ValidationRule
import dev.megatilus.atelier.results.ValidationErrorCode
import kotlinx.datetime.*
import kotlin.jvm.JvmName
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Validates ISO-8601 date format (YYYY-MM-DD).
 *
 * Example:
 * ```kotlin
 * User::birthDate {
 *     isValidIsoLocalDate() hint "Invalid date format"
 * }
 *
 * validator.validate(User(birthDate = "2000-01-15")) // Success
 * validator.validate(User(birthDate = "2000/01/15")) // Failure
 * ```
 */
public fun ValidationRule<String?>.isValidIsoLocalDate(): Rule = constrainIfNotNull(
    message = "Must be a valid ISO date (YYYY-MM-DD)",
    code = ValidationErrorCode.INVALID_FORMAT,
    predicate = { parseIsoDate(it) != null }
)

/**
 * Validates ISO-8601 datetime format (YYYY-MM-DDTHH:MM:SS).
 *
 * Example:
 * ```kotlin
 * Event::startTime {
 *     isValidIsoLocalDateTime() hint "Invalid datetime format"
 * }
 *
 * validator.validate(Event(startTime = "2025-03-14T10:30:00")) // Success
 * validator.validate(Event(startTime = "2025-03-14 10:30:00")) // Failure
 * ```
 */
public fun ValidationRule<String?>.isValidIsoLocalDateTime(): Rule = constrainIfNotNull(
    message = "Must be a valid ISO datetime (YYYY-MM-DDTHH:MM:SS)",
    code = ValidationErrorCode.INVALID_FORMAT,
    predicate = { parseIsoDateTime(it) != null }
)

/**
 * Validates ISO-8601 instant format with timezone (YYYY-MM-DDTHH:MM:SSZ).
 *
 * Example:
 * ```kotlin
 * Event::timestamp {
 *     isValidIsoInstant() hint "Invalid instant format"
 * }
 *
 * validator.validate(Event(timestamp = "2025-03-14T10:30:00Z")) // Success
 * validator.validate(Event(timestamp = "2025-03-14T10:30:00")) // Failure
 * ```
 */
public fun ValidationRule<String?>.isValidIsoInstant(): Rule = constrainIfNotNull(
    message = "Must be a valid ISO instant (YYYY-MM-DDTHH:MM:SSZ)",
    code = ValidationErrorCode.INVALID_FORMAT,
    predicate = { parseIsoInstant(it) != null }
)

/**
 * Validates that a date is before the specified date.
 *
 * Example:
 * ```kotlin
 * Event::endDate {
 *     isBefore(LocalDate(2026, 1, 1)) hint "Must end before 2026"
 * }
 *
 * validator.validate(Event(endDate = LocalDate(2025, 12, 31))) // Success
 * validator.validate(Event(endDate = LocalDate(2026, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.isBefore(date: LocalDate): Rule = constrainIfNotNull(
    message = "Must be before $date",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it < date }
)

/**
 * Validates that a date is after the specified date.
 *
 * Example:
 * ```kotlin
 * Event::startDate {
 *     isAfter(LocalDate(2025, 1, 1)) hint "Must start after 2025"
 * }
 *
 * validator.validate(Event(startDate = LocalDate(2025, 3, 14))) // Success
 * validator.validate(Event(startDate = LocalDate(2024, 12, 31))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.isAfter(date: LocalDate): Rule = constrainIfNotNull(
    message = "Must be after $date",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it > date }
)

/**
 * Validates that a date is within a range.
 *
 * Example:
 * ```kotlin
 * Event::date {
 *     isBetween(
 *         LocalDate(2025, 1, 1),
 *         LocalDate(2025, 12, 31)
 *     ) hint "Must be in 2025"
 * }
 *
 * validator.validate(Event(date = LocalDate(2025, 6, 15))) // Success
 * validator.validate(Event(date = LocalDate(2026, 1, 1))) // Failure
 * ```
 */
@JvmName("isBetweenLocalDate")
public fun ValidationRule<LocalDate?>.isBetween(start: LocalDate, end: LocalDate): Rule = constrainIfNotNull(
    message = "Must be between $start and $end",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it in start..end }
)

/**
 * Validates that a date is in the past.
 *
 * Example:
 * ```kotlin
 * User::birthDate {
 *     isPast() hint "Birth date must be in the past"
 * }
 *
 * validator.validate(User(birthDate = LocalDate(2000, 1, 1))) // Success
 * validator.validate(User(birthDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)) // Failure
 * ```
 */
@OptIn(ExperimentalTime::class)
@JvmName("isPastLocalDate")
public fun ValidationRule<LocalDate?>.isPast(): Rule = constrainIfNotNull(
    message = "Must be in the past",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        it < today
    }
)

/**
 * Validates that a date is in the future.
 *
 * Example:
 * ```kotlin
 * Event::date {
 *     isFuture() hint "Event must be in the future"
 * }
 *
 * validator.validate(Event(date = LocalDate(2030, 1, 1))) // Success
 * validator.validate(Event(date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)) // Failure
 * ```
 */
@JvmName("isFutureLocalDate")
public fun ValidationRule<LocalDate?>.isFuture(): Rule = constrainIfNotNull(
    message = "Must be in the future",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        it > today
    }
)

/**
 * Validates that a date is today.
 *
 * Example:
 * ```kotlin
 * Checkin::date {
 *     isToday() hint "Check-in must be today"
 * }
 *
 * validator.validate(Checkin(date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)) // Success
 * validator.validate(Checkin(date = LocalDate(2025, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.isToday(): Rule = constrainIfNotNull(
    message = "Must be today",
    code = ValidationErrorCode.INVALID_VALUE,
    predicate = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        it == today
    }
)

/**
 * Validates that a date is today or in the past.
 *
 * Example:
 * ```kotlin
 * Task::completedDate {
 *     isPastOrToday() hint "Completion date cannot be in the future"
 * }
 *
 * validator.validate(Task(completedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)) // Success
 * validator.validate(Task(completedDate = LocalDate(2030, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.isPastOrToday(): Rule = constrainIfNotNull(
    message = "Must be today or in the past",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        it <= today
    }
)

/**
 * Validates that a date is today or in the future.
 *
 * Example:
 * ```kotlin
 * Event::startDate {
 *     isFutureOrToday() hint "Event cannot start in the past"
 * }
 *
 * validator.validate(Event(startDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)) // Success
 * validator.validate(Event(startDate = LocalDate(2020, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.isFutureOrToday(): Rule = constrainIfNotNull(
    message = "Must be today or in the future",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        it >= today
    }
)

/**
 * Validates minimum age based on birthdate.
 *
 * Example:
 * ```kotlin
 * User::birthDate {
 *     ageAtLeast(18) hint "Must be at least 18 years old"
 * }
 *
 * validator.validate(User(birthDate = LocalDate(2000, 1, 1))) // Success (if current year >= 2018)
 * validator.validate(User(birthDate = LocalDate(2010, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.ageAtLeast(minAge: Int): Rule = constrainIfNotNull(
    message = "Must be at least $minAge years old",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { calculateAge(it) >= minAge }
)

/**
 * Validates maximum age based on birthdate.
 *
 * Example:
 * ```kotlin
 * User::birthDate {
 *     ageAtMost(120) hint "Age cannot exceed 120 years"
 * }
 *
 * validator.validate(User(birthDate = LocalDate(2000, 1, 1))) // Success
 * validator.validate(User(birthDate = LocalDate(1800, 1, 1))) // Failure
 * ```
 */
public fun ValidationRule<LocalDate?>.ageAtMost(maxAge: Int): Rule = constrainIfNotNull(
    message = "Must be at most $maxAge years old",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { calculateAge(it) <= maxAge }
)

/**
 * Validates age range based on birthdate.
 *
 * Example:
 * ```kotlin
 * User::birthDate {
 *     ageBetween(18, 65) hint "Must be between 18 and 65 years old"
 * }
 *
 * validator.validate(User(birthDate = LocalDate(2000, 1, 1))) // Success (if age in range)
 * validator.validate(User(birthDate = LocalDate(2010, 1, 1))) // Failure (too young)
 * ```
 */
public fun ValidationRule<LocalDate?>.ageBetween(minAge: Int, maxAge: Int): Rule = constrainIfNotNull(
    message = "Must be between $minAge and $maxAge years old",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { calculateAge(it) in minAge..maxAge }
)

/**
 * Validates that a datetime is before the specified datetime.
 *
 * Example:
 * ```kotlin
 * Event::endTime {
 *     isBefore(LocalDateTime(2026, 1, 1, 0, 0)) hint "Must end before 2026"
 * }
 *
 * validator.validate(Event(endTime = LocalDateTime(2025, 12, 31, 23, 59))) // Success
 * validator.validate(Event(endTime = LocalDateTime(2026, 1, 1, 0, 0))) // Failure
 * ```
 */
public fun ValidationRule<LocalDateTime?>.isBefore(dateTime: LocalDateTime): Rule = constrainIfNotNull(
    message = "Must be before $dateTime",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it < dateTime }
)

/**
 * Validates that a datetime is after the specified datetime.
 *
 * Example:
 * ```kotlin
 * Event::startTime {
 *     isAfter(LocalDateTime(2025, 1, 1, 0, 0)) hint "Must start after 2025"
 * }
 *
 * validator.validate(Event(startTime = LocalDateTime(2025, 3, 14, 10, 30))) // Success
 * validator.validate(Event(startTime = LocalDateTime(2024, 12, 31, 23, 59))) // Failure
 * ```
 */
public fun ValidationRule<LocalDateTime?>.isAfter(dateTime: LocalDateTime): Rule = constrainIfNotNull(
    message = "Must be after $dateTime",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it > dateTime }
)

/**
 * Validates that a datetime is in the past.
 *
 * Example:
 * ```kotlin
 * Log::timestamp {
 *     isPast() hint "Log timestamp must be in the past"
 * }
 *
 * validator.validate(Log(timestamp = LocalDateTime(2020, 1, 1, 0, 0))) // Success
 * validator.validate(Log(timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))) // Failure
 * ```
 */
@JvmName("isPastLocalDateTime")
public fun ValidationRule<LocalDateTime?>.isPast(): Rule = constrainIfNotNull(
    message = "Must be in the past",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        it < now
    }
)

/**
 * Validates that a datetime is within a range.
 *
 * Example:
 * ```kotlin
 * Event::scheduledAt {
 *     isBetween(
 *         LocalDateTime(2025, 1, 1, 0, 0),
 *         LocalDateTime(2025, 12, 31, 23, 59)
 *     ) hint "Must be in 2025"
 * }
 *
 * validator.validate(Event(scheduledAt = LocalDateTime(2025, 6, 15, 10, 30))) // Success
 * validator.validate(Event(scheduledAt = LocalDateTime(2026, 1, 1, 0, 0))) // Failure
 * ```
 */
@JvmName("isBetweenLocalDateTime")
public fun ValidationRule<LocalDateTime?>.isBetween(
    start: LocalDateTime,
    end: LocalDateTime
): Rule = constrainIfNotNull(
    message = "Must be between $start and $end",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it in start..end }
)

/**
 * Validates that a datetime is in the future.
 *
 * Example:
 * ```kotlin
 * Event::scheduledTime {
 *     isFuture() hint "Event must be scheduled in the future"
 * }
 *
 * validator.validate(Event(scheduledTime = LocalDateTime(2030, 1, 1, 10, 0))) // Success
 * validator.validate(Event(scheduledTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))) // Failure
 * ```
 */
@JvmName("isFutureLocalDateTime")
public fun ValidationRule<LocalDateTime?>.isFuture(): Rule = constrainIfNotNull(
    message = "Must be in the future",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        it > now
    }
)

/**
 * Validates that an instant is before the specified instant.
 *
 * Example:
 * ```kotlin
 * Event::endTimestamp {
 *     isBefore(Instant.parse("2026-01-01T00:00:00Z")) hint "Must end before 2026"
 * }
 *
 * validator.validate(Event(endTimestamp = Instant.parse("2025-12-31T23:59:59Z"))) // Success
 * validator.validate(Event(endTimestamp = Instant.parse("2026-01-01T00:00:00Z"))) // Failure
 * ```
 */
public fun ValidationRule<Instant?>.isBefore(instant: Instant): Rule = constrainIfNotNull(
    message = "Must be before $instant",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it < instant }
)

/**
 * Validates that an instant is after the specified instant.
 *
 * Example:
 * ```kotlin
 * Event::startTimestamp {
 *     isAfter(Instant.parse("2025-01-01T00:00:00Z")) hint "Must start after 2025"
 * }
 *
 * validator.validate(Event(startTimestamp = Instant.parse("2025-03-14T10:30:00Z"))) // Success
 * validator.validate(Event(startTimestamp = Instant.parse("2024-12-31T23:59:59Z"))) // Failure
 * ```
 */
public fun ValidationRule<Instant?>.isAfter(instant: Instant): Rule = constrainIfNotNull(
    message = "Must be after $instant",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it > instant }
)

/**
 * Validates that an instant is in the past.
 *
 * Example:
 * ```kotlin
 * Log::timestamp {
 *     isPast() hint "Log timestamp must be in the past"
 * }
 *
 * validator.validate(Log(timestamp = Instant.parse("2020-01-01T00:00:00Z"))) // Success
 * validator.validate(Log(timestamp = Clock.System.now())) // Failure
 * ```
 */
@JvmName("isPastInstant")
public fun ValidationRule<Instant?>.isPast(): Rule = constrainIfNotNull(
    message = "Must be in the past",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it < Clock.System.now() }
)

/**
 * Validates that an instant is within a range.
 *
 * Example:
 * ```kotlin
 * Event::occurredAt {
 *     isBetween(
 *         Instant.parse("2025-01-01T00:00:00Z"),
 *         Instant.parse("2025-12-31T23:59:59Z")
 *     ) hint "Must be in 2025"
 * }
 *
 * validator.validate(Event(occurredAt = Instant.parse("2025-06-15T10:30:00Z"))) // Success
 * validator.validate(Event(occurredAt = Instant.parse("2026-01-01T00:00:00Z"))) // Failure
 * ```
 */@JvmName("isBetweenInstant")
public fun ValidationRule<Instant?>.isBetween(start: Instant, end: Instant): Rule = constrainIfNotNull(
    message = "Must be between $start and $end",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it in start..end }
)

/**
 * Validates that an instant is in the future.
 *
 * Example:
 * ```kotlin
 * Event::scheduledTimestamp {
 *     isFuture() hint "Event must be scheduled in the future"
 * }
 *
 * validator.validate(Event(scheduledTimestamp = Instant.parse("2030-01-01T00:00:00Z"))) // Success
 * validator.validate(Event(scheduledTimestamp = Clock.System.now())) // Failure
 * ```
 */
@JvmName("isFutureInstant")
public fun ValidationRule<Instant?>.isFuture(): Rule = constrainIfNotNull(
    message = "Must be in the future",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it > Clock.System.now() }
)

/**
 * Validates that an instant is within the next duration.
 *
 * Example:
 * ```kotlin
 * Reminder::dueAt {
 *     isWithinNext(Duration.parse("7d")) hint "Reminder must be due within 7 days"
 * }
 *
 * validator.validate(Reminder(dueAt = Clock.System.now() + Duration.parse("3d"))) // Success
 * validator.validate(Reminder(dueAt = Clock.System.now() + Duration.parse("10d"))) // Failure
 * ```
 */
public fun ValidationRule<Instant?>.isWithinNext(duration: Duration): Rule = constrainIfNotNull(
    message = "Must be within the next $duration",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val now = Clock.System.now()
        it in now..(now + duration)
    }
)

/**
 * Validates that an instant is within the past duration.
 *
 * Example:
 * ```kotlin
 * Event::occurredAt {
 *     isWithinPast(Duration.parse("30d")) hint "Event must have occurred within the past 30 days"
 * }
 *
 * validator.validate(Event(occurredAt = Clock.System.now() - Duration.parse("7d"))) // Success
 * validator.validate(Event(occurredAt = Clock.System.now() - Duration.parse("60d"))) // Failure
 * ```
 */
public fun ValidationRule<Instant?>.isWithinPast(duration: Duration): Rule = constrainIfNotNull(
    message = "Must be within the past $duration",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = {
        val now = Clock.System.now()
        it in (now - duration)..now
    }
)

/**
 * Validates that a duration is positive.
 *
 * Example:
 * ```kotlin
 * Task::estimatedDuration {
 *     isPositive() hint "Duration must be positive"
 * }
 *
 * validator.validate(Task(estimatedDuration = Duration.parse("2h"))) // Success
 * validator.validate(Task(estimatedDuration = Duration.parse("-1h"))) // Failure
 * ```
 */
public fun ValidationRule<Duration?>.isPositive(): Rule = constrainIfNotNull(
    message = "Duration must be positive",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.isPositive() }
)

/**
 * Validates that a duration is negative.
 *
 * Example:
 * ```kotlin
 * Adjustment::offset {
 *     isNegative() hint "Offset must be negative"
 * }
 *
 * validator.validate(Adjustment(offset = Duration.parse("-30m"))) // Success
 * validator.validate(Adjustment(offset = Duration.parse("30m"))) // Failure
 * ```
 */
public fun ValidationRule<Duration?>.isNegative(): Rule = constrainIfNotNull(
    message = "Duration must be negative",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it.isNegative() }
)

/**
 * Validates minimum duration.
 *
 * Example:
 * ```kotlin
 * Meeting::duration {
 *     isAtLeast(Duration.parse("30m")) hint "Meeting must be at least 30 minutes"
 * }
 *
 * validator.validate(Meeting(duration = Duration.parse("1h"))) // Success
 * validator.validate(Meeting(duration = Duration.parse("15m"))) // Failure
 * ```
 */
public fun ValidationRule<Duration?>.isAtLeast(minimum: Duration): Rule = constrainIfNotNull(
    message = "Duration must be at least $minimum",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it >= minimum }
)

/**
 * Validates maximum duration.
 *
 * Example:
 * ```kotlin
 * Break::duration {
 *     isAtMost(Duration.parse("1h")) hint "Break cannot exceed 1 hour"
 * }
 *
 * validator.validate(Break(duration = Duration.parse("30m"))) // Success
 * validator.validate(Break(duration = Duration.parse("2h"))) // Failure
 * ```
 */
public fun ValidationRule<Duration?>.isAtMost(maximum: Duration): Rule = constrainIfNotNull(
    message = "Duration must be at most $maximum",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it <= maximum }
)

/**
 * Validates duration range.
 *
 * Example:
 * ```kotlin
 * Session::duration {
 *     isBetween(Duration.parse("30m"), Duration.parse("2h")) hint "Session must be 30min-2h"
 * }
 *
 * validator.validate(Session(duration = Duration.parse("1h"))) // Success
 * validator.validate(Session(duration = Duration.parse("3h"))) // Failure
 * ```
 */
@JvmName("isBetweenDuration")
public fun ValidationRule<Duration?>.isBetween(min: Duration, max: Duration): Rule = constrainIfNotNull(
    message = "Duration must be between $min and $max",
    code = ValidationErrorCode.OUT_OF_RANGE,
    predicate = { it in min..max }
)

private fun calculateAge(
    birthDate: LocalDate,
    referenceDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
): Int {
    val years = referenceDate.year - birthDate.year
    val monthDiff = referenceDate.month.number - birthDate.month.number
    val dayDiff = referenceDate.day - birthDate.day

    return when {
        monthDiff > 0 -> years
        monthDiff < 0 -> years - 1
        dayDiff >= 0 -> years
        else -> years - 1
    }
}

private fun parseIsoDate(dateString: String): LocalDate? {
    return try {
        if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            LocalDate.parse(dateString)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun parseIsoDateTime(dateTimeString: String): LocalDateTime? {
    return try {
        if (dateTimeString.matches(
                Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?$")
            )
        ) {
            LocalDateTime.parse(dateTimeString)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun parseIsoInstant(instantString: String): Instant? {
    return try {
        if (instantString.matches(
                Regex(
                    "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?(?:Z|[+-]\\d{2}:\\d{2})$"
                )
            )
        ) {
            Instant.parse(instantString)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
