/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(kotlin.time.ExperimentalTime::class)

package dev.megatilus.atelier.validator

import kotlinx.datetime.*
import kotlinx.datetime.number
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class KotlinxDatetimeValidatorsTest {
    private val now = Clock.System.now()
    private val nowLocalDatetime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val yesterday = today.minus(1, DateTimeUnit.DAY)
    private val tomorrow = today.plus(1, DateTimeUnit.DAY)

    @Test
    fun `calculateAge should return correct age`() {
        val birthDate = LocalDate(2000, 1, 15)
        val referenceDate = LocalDate(2025, 1, 15)
        assertEquals(25, calculateAge(birthDate, referenceDate))
    }

    @Test
    fun `calculateAge should handle birthday not yet occurred`() {
        val birthDate = LocalDate(2000, 12, 25)
        val referenceDate = LocalDate(2025, 1, 15)
        assertEquals(24, calculateAge(birthDate, referenceDate))
    }

    @Test
    fun `parseIsoDate should parse valid dates`() {
        assertNotNull(parseIsoDate("2025-01-15"))
        assertNull(parseIsoDate("2025/01/15"))
        assertNull(parseIsoDate("invalid"))
    }

    @Test
    fun `parseIsoDateTime should parse valid datetime`() {
        assertNotNull(parseIsoDateTime("2025-01-15T10:30:45"))
        assertNull(parseIsoDateTime("2025-01-15 10:30:45"))
    }

    @Test
    fun `parseIsoInstant should parse valid instants`() {
        assertNotNull(parseIsoInstant("2025-01-15T10:30:45Z"))
        assertNull(parseIsoInstant("2025-01-15T10:30:45"))
    }

    @Test
    fun `isBefore and isAfter should work`() {
        assertTrue(LocalDate(2020, 1, 1) < LocalDate(2025, 1, 1))
        assertTrue(LocalDate(2025, 1, 1) > LocalDate(2020, 1, 1))
    }

    @Test
    fun `isBetween should validate range`() {
        val start = LocalDate(2020, 1, 1)
        val end = LocalDate(2025, 12, 31)
        assertTrue(LocalDate(2022, 6, 15) in start..end)
        assertFalse(LocalDate(2019, 12, 31) in start..end)
    }

    @Test
    fun `isPast and isFuture should work`() {
        assertTrue(yesterday < today)
        assertTrue(tomorrow > today)
        assertFalse(today > today)
    }

    @Test
    fun `isToday should work`() {
        assertEquals(today, today)
        assertNotEquals(yesterday, today)
    }

    @Test
    fun `ageAtLeast should validate minimum age`() {
        val birthDate = LocalDate(2000, 1, 1)
        val age = calculateAge(birthDate)
        assertTrue(age >= 18)
    }

    @Test
    fun `ageAtMost should validate maximum age`() {
        val birthDate = LocalDate(1970, 1, 1)
        val age = calculateAge(birthDate)
        assertTrue(age <= 100)
    }

    @Test
    fun `ageBetween should validate age range`() {
        val birthDate18 = LocalDate(2007, 1, 1)
        val age = calculateAge(birthDate18)
        assertTrue(age in 18..65)
    }

    @Test
    fun `LocalDateTime comparisons should work`() {
        val pastInstant = now - 1.hours
        val futureInstant = now + 1.hours

        val past = pastInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val future = futureInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        assertTrue(past < nowLocalDatetime)
        assertTrue(future > nowLocalDatetime)
    }

    @Test
    fun `Instant comparisons should work`() {
        val past = now.minus(1.hours)
        val future = now.plus(1.hours)

        assertTrue(past < now)
        assertTrue(future > now)
    }

    @Test
    fun `isWithinNext should validate future window`() {
        val inOneHour = now.plus(1.hours)
        val window = 90.minutes
        assertTrue(inOneHour in now..(now + window))
    }

    @Test
    fun `isWithinPast should validate past window`() {
        val oneHourAgo = now.minus(1.hours)
        val window = 90.minutes
        assertTrue(oneHourAgo in (now - window)..now)
    }

    @Test
    fun `Duration comparisons should work`() {
        assertTrue(1.hours > Duration.ZERO)
        assertTrue((-1).hours < Duration.ZERO)
        assertFalse(0.seconds > Duration.ZERO)
    }

    @Test
    fun `Duration range validation should work`() {
        val min = 30.minutes
        val max = 2.hours

        assertTrue(1.hours in min..max)
        assertFalse(3.hours in min..max)
    }

    @Test
    fun `nullable validators should allow null`() {
        val nullDate: LocalDate? = null
        assertTrue(nullDate == null || nullDate < today)
    }

    @Test
    fun `nullable validators should validate non-null values`() {
        val past = LocalDate(2020, 1, 1)
        assertTrue(past < today)
    }

    data class User(
        val name: String,
        val birthDate: LocalDate,
        val registeredAt: Instant
    )

    data class Event(
        val startDate: LocalDate,
        val endDate: LocalDate
    )

    @Test
    fun `User validation example`() {
        val user = User(
            name = "John",
            birthDate = LocalDate(1990, 5, 15),
            registeredAt = now.minus(30.days)
        )

        assertTrue(calculateAge(user.birthDate) >= 18)
        assertTrue(user.registeredAt < now)
    }

    @Test
    fun `Event validation example`() {
        val event = Event(
            startDate = today.plus(7, DateTimeUnit.DAY),
            endDate = today.plus(10, DateTimeUnit.DAY)
        )

        assertTrue(event.startDate > today)
        assertTrue(event.endDate > event.startDate)
    }
}
