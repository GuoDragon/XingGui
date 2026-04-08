package com.example.xinggui.data.model

data class GoalPlan(
    val childId: String,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyCheckIns: List<WeeklyCheckIn> = emptyList()
)
