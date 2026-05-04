package com.example.xinggui.presentation.archive

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ScreenRenderState

data class ArchiveDimensionUiModel(
    val id: String,
    val title: String,
    val score: Int
)

data class ArchiveWeeklyUiModel(
    val id: String,
    val dimensionId: String,
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class ArchiveUiState(
    val childName: String,
    val childAge: Int,
    val childInterventionDuration: String,
    val childBirthDate: String? = null,
    val childAvatarKey: String? = null,
    val role: UserRole,
    val semesterGoal: String,
    val monthlyGoal: String,
    val dimensions: List<ArchiveDimensionUiModel>,
    val weeklyItems: List<ArchiveWeeklyUiModel>,
    val earnedStars: Int,
    val totalStars: Int,
    val roleHint: String,
    val weeklyCheckInCountsByDimension: Map<String, Int>
)

interface ArchiveContract {
    interface View {
        fun render(state: ScreenRenderState<ArchiveUiState>) {
            when (state) {
                ScreenRenderState.Loading -> showLoading()
                is ScreenRenderState.Content -> showContent(state.data)
                is ScreenRenderState.Empty -> showEmpty()
                is ScreenRenderState.Error -> showError(state.message)
            }
        }

        fun showLoading() = Unit
        fun showContent(state: ArchiveUiState) = Unit
        fun showEmpty() = Unit
        fun showError(message: String) = Unit
        fun showCheckInFeedback(success: Boolean, stars: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun loadData(childId: String, role: UserRole)
        suspend fun performCheckIn(itemId: String, note: String, stars: Int, isCompleted: Boolean)
    }
}
