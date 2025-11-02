/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalTime::class)

package dev.megatilus.atelier.validator

import dev.megatilus.atelier.builders.FieldValidatorBuilder
import dev.megatilus.atelier.builders.constraintForExtension
import dev.megatilus.atelier.results.ValidatorCode
import kotlinx.datetime.*
import kotlin.jvm.JvmName
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal fun calculateAge(
    birthDate: LocalDate,
    referenceDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
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

internal fun parseIsoDate(dateString: String): LocalDate? {
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

internal fun parseIsoDateTime(dateTimeString: String): LocalDateTime? {
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

/** Validates and parses ISO-8601 instant strings with timezone. Returns null if invalid. */
internal fun parseIsoInstant(instantString: String): Instant? {
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

public fun <T : Any> FieldValidatorBuilder<T, String>.isValidIsoDate(
    message: String? = null
): FieldValidatorBuilder<T, String> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO date (YYYY-MM-DD)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.isBlank() || parseIsoDate(it) != null }
    )

@JvmName("isValidIsoDateNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.isValidIsoDate(
    message: String? = null
): FieldValidatorBuilder<T, String?> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO date (YYYY-MM-DD)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.isBlank() || parseIsoDate(it) != null }
    )

public fun <T : Any> FieldValidatorBuilder<T, String>.isValidIsoDateTime(
    message: String? = null
): FieldValidatorBuilder<T, String> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO datetime (YYYY-MM-DDTHH:MM:SS)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.isBlank() || parseIsoDateTime(it) != null }
    )

@JvmName("isValidIsoDateTimeNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.isValidIsoDateTime(
    message: String? = null
): FieldValidatorBuilder<T, String?> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO datetime (YYYY-MM-DDTHH:MM:SS)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.isBlank() || parseIsoDateTime(it) != null }
    )

public fun <T : Any> FieldValidatorBuilder<T, String>.isValidIsoInstant(
    message: String? = null
): FieldValidatorBuilder<T, String> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO instant (YYYY-MM-DDTHH:MM:SSZ)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it.isBlank() || parseIsoInstant(it) != null }
    )

@JvmName("isValidIsoInstantNullable")
public fun <T : Any> FieldValidatorBuilder<T, String?>.isValidIsoInstant(
    message: String? = null
): FieldValidatorBuilder<T, String?> =
    constraintForExtension(
        hint = message ?: "Must be a valid ISO instant (YYYY-MM-DDTHH:MM:SSZ)",
        code = ValidatorCode.INVALID_FORMAT,
        predicate = { it == null || it.isBlank() || parseIsoInstant(it) != null }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isBefore(
    date: LocalDate,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be before $date",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < date }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isAfter(
    date: LocalDate,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be after $date",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > date }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isBetween(
    start: LocalDate,
    end: LocalDate,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be between $start and $end",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it in start..end }
    )

@JvmName("isBetweenNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isBetween(
    start: LocalDate,
    end: LocalDate,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be between $start and $end",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it in start..end }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isPastNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isFutureLocalDate")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isFutureLocalDateNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be today",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isTodayNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be today",
        code = ValidatorCode.INVALID_VALUE,
        predicate = { it == null || it == Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isPastOrToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be today or in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it <= Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isPastOrTodayNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isPastOrToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be today or in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it <= Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.isFutureOrToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be today or in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it >= Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

@JvmName("isFutureOrTodayNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.isFutureOrToday(
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be today or in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it >= Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.ageAtLeast(
    minAge: Int,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be at least $minAge years old",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { calculateAge(it) >= minAge }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.ageAtMost(
    maxAge: Int,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be at most $maxAge years old",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { calculateAge(it) <= maxAge }
    )

@JvmName("ageAtMostNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.ageAtMost(
    maxAge: Int,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be at most $maxAge years old",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || calculateAge(it) <= maxAge }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDate>.ageBetween(
    minAge: Int,
    maxAge: Int,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate> =
    constraintForExtension(
        hint = message ?: "Must be between $minAge and $maxAge years old",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { calculateAge(it) in minAge..maxAge }
    )

@JvmName("ageBetweenNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDate?>.ageBetween(
    minAge: Int,
    maxAge: Int,
    message: String? = null
): FieldValidatorBuilder<T, LocalDate?> =
    constraintForExtension(
        hint = message ?: "Must be between $minAge and $maxAge years old",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || calculateAge(it) in minAge..maxAge }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime>.isBefore(
    dateTime: LocalDateTime,
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime> =
    constraintForExtension(
        hint = message ?: "Must be before $dateTime",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < dateTime }
    )

public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime>.isAfter(
    dateTime: LocalDateTime,
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime> =
    constraintForExtension(
        hint = message ?: "Must be after $dateTime",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > dateTime }
    )

@JvmName("isBeforeLocalDateTimeNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime?>.isBefore(
    dateTime: LocalDateTime,
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime?> =
    constraintForExtension(
        hint = message ?: "Must be before $dateTime",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < dateTime }
    )

@JvmName("isAfterLocalDateTimeNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime?>.isAfter(
    dateTime: LocalDateTime,
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime?> =
    constraintForExtension(
        hint = message ?: "Must be after $dateTime",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > dateTime }
    )

@JvmName("isPastLocalDateTime")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < now }
    )
}

@JvmName("isPastLocalDateTimeNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime?>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime?> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < now }
    )
}

/** Validates that the datetime is in the future. */
@JvmName("isFutureLocalDateTime")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > now }
    )
}

@JvmName("isFutureLocalDateTimeNullable")
public fun <T : Any> FieldValidatorBuilder<T, LocalDateTime?>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, LocalDateTime?> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > now }
    )
}

public fun <T : Any> FieldValidatorBuilder<T, Instant>.isBefore(
    instant: Instant,
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be before $instant",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < instant }
    )

public fun <T : Any> FieldValidatorBuilder<T, Instant>.isAfter(
    instant: Instant,
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be after $instant",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > instant }
    )

@JvmName("isBeforeInstantNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isBefore(
    instant: Instant,
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be before $instant",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < instant }
    )

@JvmName("isAfterInstantNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isAfter(
    instant: Instant,
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be after $instant",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > instant }
    )

/** Validates that the instant is in the past. */
@JvmName("isPastInstant")
public fun <T : Any> FieldValidatorBuilder<T, Instant>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it < Clock.System.now() }
    )

@JvmName("isPastInstantNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isPast(
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be in the past",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it < Clock.System.now() }
    )

@JvmName("isFutureInstant")
public fun <T : Any> FieldValidatorBuilder<T, Instant>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it > Clock.System.now() }
    )

@JvmName("isFutureInstantNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isFuture(
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be in the future",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it > Clock.System.now() }
    )

public fun <T : Any> FieldValidatorBuilder<T, Instant>.isWithinNext(
    duration: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be within the next $duration",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = {
            val now = Clock.System.now()
            it in now..(now + duration)
        }
    )

@JvmName("isWithinNextNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isWithinNext(
    duration: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be within the next $duration",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = {
            if (it == null) return@constraintForExtension true
            val now = Clock.System.now()
            it in now..(now + duration)
        }
    )

public fun <T : Any> FieldValidatorBuilder<T, Instant>.isWithinPast(
    duration: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Instant> =
    constraintForExtension(
        hint = message ?: "Must be within the past $duration",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = {
            val now = Clock.System.now()
            it in (now - duration)..now
        }
    )

@JvmName("isWithinPastNullable")
public fun <T : Any> FieldValidatorBuilder<T, Instant?>.isWithinPast(
    duration: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Instant?> =
    constraintForExtension(
        hint = message ?: "Must be within the past $duration",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = {
            if (it == null) return@constraintForExtension true
            val now = Clock.System.now()
            it in (now - duration)..now
        }
    )

public fun <T : Any> FieldValidatorBuilder<T, Duration>.isPositive(
    message: String? = null
): FieldValidatorBuilder<T, Duration> =
    constraintForExtension(
        hint = message ?: "Duration must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.isPositive() }
    )

@JvmName("isPositiveNullable")
public fun <T : Any> FieldValidatorBuilder<T, Duration?>.isPositive(
    message: String? = null
): FieldValidatorBuilder<T, Duration?> =
    constraintForExtension(
        hint = message ?: "Duration must be positive",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it.isPositive() }
    )

public fun <T : Any> FieldValidatorBuilder<T, Duration>.isNegative(
    message: String? = null
): FieldValidatorBuilder<T, Duration> =
    constraintForExtension(
        hint = message ?: "Duration must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it.isNegative() }
    )

@JvmName("isNegativeNullable")
public fun <T : Any> FieldValidatorBuilder<T, Duration?>.isNegative(
    message: String? = null
): FieldValidatorBuilder<T, Duration?> =
    constraintForExtension(
        hint = message ?: "Duration must be negative",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it.isNegative() }
    )

public fun <T : Any> FieldValidatorBuilder<T, Duration>.isAtLeast(
    minimum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration> =
    constraintForExtension(
        hint = message ?: "Duration must be at least $minimum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it >= minimum }
    )

@JvmName("isAtLeastNullable")
public fun <T : Any> FieldValidatorBuilder<T, Duration?>.isAtLeast(
    minimum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration?> =
    constraintForExtension(
        hint = message ?: "Duration must be at least $minimum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it >= minimum }
    )

public fun <T : Any> FieldValidatorBuilder<T, Duration>.isAtMost(
    maximum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration> =
    constraintForExtension(
        hint = message ?: "Duration must be at most $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it <= maximum }
    )

@JvmName("isAtMostNullable")
public fun <T : Any> FieldValidatorBuilder<T, Duration?>.isAtMost(
    maximum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration?> =
    constraintForExtension(
        hint = message ?: "Duration must be at most $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it <= maximum }
    )

public fun <T : Any> FieldValidatorBuilder<T, Duration>.isBetween(
    minimum: Duration,
    maximum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration> =
    constraintForExtension(
        hint = message ?: "Duration must be between $minimum and $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it in minimum..maximum }
    )

@JvmName("isBetweenDurationNullable")
public fun <T : Any> FieldValidatorBuilder<T, Duration?>.isBetween(
    minimum: Duration,
    maximum: Duration,
    message: String? = null
): FieldValidatorBuilder<T, Duration?> =
    constraintForExtension(
        hint = message ?: "Duration must be between $minimum and $maximum",
        code = ValidatorCode.OUT_OF_RANGE,
        predicate = { it == null || it in minimum..maximum }
    )
