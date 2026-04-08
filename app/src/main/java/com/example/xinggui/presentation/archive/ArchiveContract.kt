package com.example.xinggui.presentation.archive

import com.example.xinggui.data.model.UserRole

data class ArchiveDimensionUiModel(
    val title: String,
    val score: Int
)

data class ArchiveWeeklyUiModel(
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class ArchiveUiState(
    val childName: String,
    val role: UserRole,
    val semesterGoal: String,
    val monthlyGoal: String,
    val dimensions: List<ArchiveDimensionUiModel>,
    val weeklyItems: List<ArchiveWeeklyUiModel>,
    val earnedStars: Int,
    val totalStars: Int,
    val roleHint: String
)

interface ArchiveContract {
    interface View {
        fun showLoading()
        fun showContent(state: ArchiveUiState)
        fun showEmpty()
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData(childId: String, role: UserRole)
    }
}
