package com.example.xinggui.presentation.common

import java.time.LocalDate
import java.util.Locale

data class ProfileDisplayInfo(
    val nameLine: String,
    val birthDateLine: String,
    val ageLine: String,
    val interventionDurationLine: String
)

fun buildProfileDisplayInfo(
    childName: String,
    age: Int,
    interventionDuration: String
): ProfileDisplayInfo {
    val birthDate = buildStableMockBirthDate(childName, age, interventionDuration)
    return ProfileDisplayInfo(
        nameLine = "姓名：$childName",
        birthDateLine = "出生日期：$birthDate",
        ageLine = "年龄：${age} 岁",
        interventionDurationLine = "干预时长：$interventionDuration"
    )
}

private fun buildStableMockBirthDate(
    childName: String,
    age: Int,
    interventionDuration: String
): String {
    val key = "$childName|$age|$interventionDuration"
    val hash = stablePositiveHash(key)

    val currentYear = LocalDate.now().year
    val safeAge = age.coerceIn(1, 18)
    val baseYear = (currentYear - safeAge).coerceIn(2000, currentYear)
    val yearOffset = if (hash % 4 == 0) -1 else 0
    val year = (baseYear + yearOffset).coerceIn(2000, currentYear)
    val month = hash % 12 + 1

    return String.format(Locale.CHINA, "%04d.%02d", year, month)
}

private fun stablePositiveHash(text: String): Int {
    var value = 17
    text.forEach { ch ->
        value = value * 31 + ch.code
    }
    return value and Int.MAX_VALUE
}
