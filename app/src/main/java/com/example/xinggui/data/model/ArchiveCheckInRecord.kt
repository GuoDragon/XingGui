package com.example.xinggui.data.model

data class ArchiveCheckInRecord(
    val recordId: String,
    val childId: String,
    val itemId: String,
    val dimensionId: String,
    val title: String,
    val note: String,
    val completed: Boolean,
    val rewardStars: Int,
    val timestamp: Long
)
