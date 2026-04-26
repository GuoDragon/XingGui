package com.example.xinggui.data.model

data class IepWeeklyGoalInput(
    val dimensionId: String,
    val title: String,
    val rewardStars: Int
)

data class IepDocument(
    val documentId: String,
    val childId: String,
    val uploadedBy: String,
    val originalFileName: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyGoals: List<IepWeeklyGoalInput> = emptyList(),
    val notes: String? = null,
    val uploadedAt: Long
)

data class IepUploadResult(
    val document: IepDocument,
    val goalPlan: GoalPlan
)
