package com.example.xinggui.data.model

data class CheckInProcessResult(
    val success: Boolean,
    val earnedStars: Int = 0,
    val updatedReport: ReportSummary? = null,
    val message: String? = null
)
