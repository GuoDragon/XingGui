package com.example.xinggui.data.model

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val IsoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val DisplayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.CHINA)

fun ChildProfile.displayAge(today: LocalDate = LocalDate.now()): Int {
    return calculateAgeYears(birthDate, today) ?: age
}

fun ChildProfile.displayInterventionDuration(today: LocalDate = LocalDate.now()): String {
    return calculateInterventionDuration(interventionStartDate, today) ?: interventionDuration
}

fun calculateAgeYears(birthDate: String?, today: LocalDate = LocalDate.now()): Int? {
    val birth = parseProfileDate(birthDate) ?: return null
    if (birth.isAfter(today)) {
        return null
    }
    return Period.between(birth, today).years.coerceAtLeast(0)
}

fun calculateInterventionDuration(
    interventionStartDate: String?,
    today: LocalDate = LocalDate.now()
): String? {
    val startDate = parseProfileDate(interventionStartDate) ?: return null
    if (startDate.isAfter(today)) {
        return "未开始"
    }
    val period = Period.between(startDate, today)
    val years = period.years
    val months = period.months
    return when {
        years > 0 && months > 0 -> "${years}年${months}个月"
        years > 0 -> "${years}年"
        months > 0 -> "${months}个月"
        else -> "不足1个月"
    }
}

fun formatProfileDateForDisplay(value: String?): String? {
    val date = parseProfileDate(value) ?: return value?.takeIf { it.isNotBlank() }
    return DisplayDateFormatter.format(date)
}

fun normalizeProfileDate(value: String): String {
    return parseProfileDate(value)?.format(IsoDateFormatter).orEmpty()
}

fun parseProfileDate(value: String?): LocalDate? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isBlank()) {
        return null
    }
    return try {
        LocalDate.parse(trimmed, IsoDateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}
