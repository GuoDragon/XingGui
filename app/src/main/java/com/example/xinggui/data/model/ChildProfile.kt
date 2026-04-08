package com.example.xinggui.data.model

data class ChildProfile(
    val childId: String,
    val name: String,
    val age: Int,
    val interventionDuration: String,
    val guardianIds: List<String> = emptyList(),
    val assignedTeacherIds: List<String> = emptyList()
)
