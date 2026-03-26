/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalTime::class)

package dev.megatilus.atelier.rules

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.results.ValidationResult
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class KotlinxDatetimeRulesTest {

    data class EventEntity(
        val isoDate: String? = null,
        val date: LocalDate? = null,
        val datetime: LocalDateTime? = null,
        val instant: Instant? = null,
        val duration: Duration? = null,
        val birthdate: LocalDate? = null
    )

    @Test
    fun `isValidIsoLocalDate should succeed for valid ISO date`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoLocalDate()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19")) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(isoDate = "2000-01-01")) is ValidationResult.Success)
    }

    @Test
    fun `isValidIsoLocalDate should fail for invalid ISO date`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoLocalDate()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-13-01")) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(isoDate = "invalid")) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(isoDate = "2025/03/19")) is ValidationResult.Failure)
    }

    @Test
    fun `isValidIsoLocalDateTime should succeed for valid ISO datetime`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoLocalDateTime()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19T10:30:00")) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19T10:30:00.123")) is ValidationResult.Success)
    }

    @Test
    fun `isValidIsoLocalDateTime should fail for invalid ISO datetime`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoLocalDateTime()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19")) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(isoDate = "invalid")) is ValidationResult.Failure)
    }

    @Test
    fun `isValidIsoInstant should succeed for valid ISO instant`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoInstant()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19T10:30:00Z")) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19T10:30:00+01:00")) is ValidationResult.Success)
    }

    @Test
    fun `isValidIsoInstant should fail for invalid ISO instant`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::isoDate {
                isValidIsoInstant()
            }
        }

        assertTrue(validator.validate(EventEntity(isoDate = "2025-03-19T10:30:00")) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(isoDate = "invalid")) is ValidationResult.Failure)
    }

    @Test
    fun `isBefore should succeed when date is before specified date`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isBefore(LocalDate(2025, 12, 31))
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 3, 19))) is ValidationResult.Success)
    }

    @Test
    fun `isBefore should fail when date is after or equal`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isBefore(LocalDate(2025, 3, 19))
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 3, 19))) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 12, 31))) is ValidationResult.Failure)
    }

    @Test
    fun `isAfter should succeed when date is after specified date`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isAfter(LocalDate(2020, 1, 1))
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 3, 19))) is ValidationResult.Success)
    }

    @Test
    fun `isAfter should fail when date is before or equal`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isAfter(LocalDate(2025, 3, 19))
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 3, 19))) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2020, 1, 1))) is ValidationResult.Failure)
    }

    @Test
    fun `isBetween should succeed when date is in range`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isBetween(
                    start = LocalDate(2025, 1, 1),
                    end = LocalDate(2025, 12, 31)
                )
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 3, 19))) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 1, 1))) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2025, 12, 31))) is ValidationResult.Success)
    }

    @Test
    fun `isBetween should fail when date is out of range`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isBetween(
                    start = LocalDate(2025, 1, 1),
                    end = LocalDate(2025, 12, 31)
                )
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2024, 12, 31))) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2026, 1, 1))) is ValidationResult.Failure)
    }

    @Test
    fun `isPast should succeed when date is in the past`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isPast()
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2020, 1, 1))) is ValidationResult.Success)
    }

    @Test
    fun `isFuture should succeed when date is in the future`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isFuture()
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2030, 1, 1))) is ValidationResult.Success)
    }

    @Test
    fun `isToday should succeed when date is today`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isToday()
            }
        }

        assertTrue(validator.validate(EventEntity(date = today)) is ValidationResult.Success)
    }

    @Test
    fun `isPastOrToday should succeed for past and today`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isPastOrToday()
            }
        }

        assertTrue(validator.validate(EventEntity(date = today)) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2020, 1, 1))) is ValidationResult.Success)
    }

    @Test
    fun `isFutureOrToday should succeed for future and today`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isFutureOrToday()
            }
        }

        assertTrue(validator.validate(EventEntity(date = today)) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(date = LocalDate(2030, 1, 1))) is ValidationResult.Success)
    }

    @Test
    fun `isAtLeast should succeed when age is sufficient`() {
        val eighteenYearsAgo = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(18, DateTimeUnit.YEAR)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::birthdate {
                ageAtLeast(18)
            }
        }

        assertTrue(validator.validate(EventEntity(birthdate = eighteenYearsAgo)) is ValidationResult.Success)
    }

    @Test
    fun `isAtLeast should fail when age is insufficient`() {
        val fifteenYearsAgo = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(15, DateTimeUnit.YEAR)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::birthdate {
                ageAtLeast(18)
            }
        }

        assertTrue(validator.validate(EventEntity(birthdate = fifteenYearsAgo)) is ValidationResult.Failure)
    }

    @Test
    fun `isAtMost should succeed when age is within limit`() {
        val thirtyYearsAgo = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(30, DateTimeUnit.YEAR)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::birthdate {
                ageAtMost(65)
            }
        }

        assertTrue(validator.validate(EventEntity(birthdate = thirtyYearsAgo)) is ValidationResult.Success)
    }

    @Test
    fun `ageBetween should succeed when age is in range`() {
        val twentyFiveYearsAgo = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(25, DateTimeUnit.YEAR)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::birthdate {
                ageBetween(18, 65)
            }
        }

        assertTrue(validator.validate(EventEntity(birthdate = twentyFiveYearsAgo)) is ValidationResult.Success)
    }

    @Test
    fun `ageBetween should fail when age is out of range`() {
        val seventyYearsAgo = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .minus(70, DateTimeUnit.YEAR)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::birthdate {
                ageBetween(18, 65)
            }
        }

        assertTrue(validator.validate(EventEntity(birthdate = seventyYearsAgo)) is ValidationResult.Failure)
    }

    @Test
    fun `LocalDateTime isBefore should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::datetime {
                isBefore(LocalDateTime(2025, 12, 31, 23, 59))
            }
        }

        assertTrue(
            validator.validate(
                EventEntity(datetime = LocalDateTime(2025, 3, 19, 10, 30))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `LocalDateTime isAfter should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::datetime {
                isAfter(LocalDateTime(2020, 1, 1, 0, 0))
            }
        }

        assertTrue(
            validator.validate(
                EventEntity(datetime = LocalDateTime(2025, 3, 19, 10, 30))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `LocalDateTime isBetween should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::datetime {
                isBetween(
                    start = LocalDateTime(2025, 1, 1, 0, 0),
                    end = LocalDateTime(2025, 12, 31, 23, 59)
                )
            }
        }

        assertTrue(
            validator.validate(
                EventEntity(datetime = LocalDateTime(2025, 3, 19, 10, 30))
            ) is ValidationResult.Success
        )
    }

    @Test
    fun `Instant isBefore should succeed`() {
        val now = Clock.System.now()
        val future = now.plus(1.days)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::instant {
                isBefore(future)
            }
        }

        assertTrue(validator.validate(EventEntity(instant = now)) is ValidationResult.Success)
    }

    @Test
    fun `Instant isAfter should succeed`() {
        val now = Clock.System.now()
        val past = now.minus(1.days)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::instant {
                isAfter(past)
            }
        }

        assertTrue(validator.validate(EventEntity(instant = now)) is ValidationResult.Success)
    }

    @Test
    fun `Instant isBetween should succeed`() {
        val now = Clock.System.now()
        val past = now.minus(1.days)
        val future = now.plus(1.days)

        val validator = AtelierValidator<EventEntity> {
            EventEntity::instant {
                isBetween(start = past, end = future)
            }
        }

        assertTrue(validator.validate(EventEntity(instant = now)) is ValidationResult.Success)
    }

    @Test
    fun `Duration isAtLeast should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isAtLeast(1.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 2.hours)) is ValidationResult.Success)
        assertTrue(validator.validate(EventEntity(duration = 90.minutes)) is ValidationResult.Success)
    }

    @Test
    fun `Duration isAtLeast should fail when too short`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isAtLeast(1.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 30.minutes)) is ValidationResult.Failure)
    }

    @Test
    fun `Duration isAtMost should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isAtMost(2.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 1.hours)) is ValidationResult.Success)
    }

    @Test
    fun `Duration isAtMost should fail when too long`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isAtMost(2.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 3.hours)) is ValidationResult.Failure)
    }

    @Test
    fun `Duration isBetween should succeed`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isBetween(min = 30.minutes, max = 2.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 1.hours)) is ValidationResult.Success)
    }

    @Test
    fun `Duration isBetween should fail when out of range`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::duration {
                isBetween(min = 30.minutes, max = 2.hours)
            }
        }

        assertTrue(validator.validate(EventEntity(duration = 15.minutes)) is ValidationResult.Failure)
        assertTrue(validator.validate(EventEntity(duration = 3.hours)) is ValidationResult.Failure)
    }

    @Test
    fun `datetime rules should skip validation when value is null`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isFuture()
                isAfter(LocalDate(2020, 1, 1))
            }
        }

        assertTrue(validator.validate(EventEntity(date = null)) is ValidationResult.Success)
    }

    @Test
    fun `multiple datetime rules should all be validated`() {
        val validator = AtelierValidator<EventEntity> {
            EventEntity::date {
                isFuture()
                isBefore(LocalDate(2030, 1, 1))
            }
        }

        assertTrue(validator.validate(EventEntity(date = LocalDate(2027, 1, 1))) is ValidationResult.Success)
    }
}
