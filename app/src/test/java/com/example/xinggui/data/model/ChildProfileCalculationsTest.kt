package com.example.xinggui.data.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChildProfileCalculationsTest {
    @Test
    fun ageUsesBirthDateWhenPresent() {
        val today = LocalDate.of(2026, 4, 26)

        assertEquals(8, calculateAgeYears("2018-04-26", today))
        assertEquals(6, calculateAgeYears("2019-05-10", today))
    }

    @Test
    fun invalidBirthDateReturnsNullForCompatibilityFallback() {
        assertNull(calculateAgeYears("not-a-date", LocalDate.of(2026, 4, 26)))
    }

    @Test
    fun interventionDurationUsesStartDate() {
        val today = LocalDate.of(2026, 4, 26)

        assertEquals("8个月", calculateInterventionDuration("2025-08-26", today))
        assertEquals("1年2个月", calculateInterventionDuration("2025-02-26", today))
        assertEquals("不足1个月", calculateInterventionDuration("2026-04-10", today))
    }

    @Test
    fun childProfileFallsBackToLegacyAgeAndDurationWhenDatesAreMissing() {
        val child = ChildProfile(
            childId = "child001",
            name = "晨晨",
            age = 7,
            interventionDuration = "8个月"
        )

        assertEquals(7, child.displayAge(LocalDate.of(2026, 4, 26)))
        assertEquals("8个月", child.displayInterventionDuration(LocalDate.of(2026, 4, 26)))
    }
}
