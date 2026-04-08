package com.example.xinggui.presentation.goals

import com.example.xinggui.data.model.UserRole

data class GoalTaskUiModel(
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class GoalsUiState(
    val childName: String,
    val role: UserRole,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyTasks: List<GoalTaskUiModel>,
    val uploadHint: String,
    val aiHint: String
)

interface GoalsContract {
    interface View {
        fun showLoading()
        fun showContent(state: GoalsUiState)
        fun showEmpty()
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData(childId: String, role: UserRole)
    }
}
