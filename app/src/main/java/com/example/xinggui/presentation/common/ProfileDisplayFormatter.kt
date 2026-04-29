package com.example.xinggui.presentation.common

import com.example.xinggui.data.model.formatProfileDateForDisplay

data class ProfileDisplayInfo(
    val nameLine: String,
    val birthDateLine: String,
    val ageLine: String,
    val interventionDurationLine: String
)

fun buildProfileDisplayInfo(
    childName: String,
    age: Int,
    interventionDuration: String,
    birthDate: String? = null
): ProfileDisplayInfo {
    val birthDateLabel = formatProfileDateForDisplay(birthDate) ?: "未填写"
    return ProfileDisplayInfo(
        nameLine = "姓名：$childName",
        birthDateLine = "出生日期：$birthDateLabel",
        ageLine = "年龄：${age} 岁",
        interventionDurationLine = "干预时长：$interventionDuration"
    )
}
