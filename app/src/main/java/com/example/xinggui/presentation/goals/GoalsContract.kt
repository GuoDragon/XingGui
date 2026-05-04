package com.example.xinggui.presentation.goals

import com.example.xinggui.data.model.IepDocument
import com.example.xinggui.data.model.IepWeeklyGoalInput
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ScreenRenderState

data class GoalTaskUiModel(
    val dimensionId: String,
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class GoalsUiState(
    val childName: String,
    // AI辅助生成：Doubao-Seed-2.0-Code, 2026-05-02
    val childAge: Int,
    val childInterventionDuration: String,
    val childBirthDate: String? = null,
    val childAvatarKey: String? = null,
    val role: UserRole,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyTasks: List<GoalTaskUiModel>,
    val uploadHint: String,
    val aiHint: String,
    val latestIepDocument: IepDocument? = null
)

interface GoalsContract {
    interface View {
        fun render(state: ScreenRenderState<GoalsUiState>) {
            when (state) {
                ScreenRenderState.Loading -> showLoading()
                is ScreenRenderState.Content -> showContent(state.data)
                is ScreenRenderState.Empty -> showEmpty()
                is ScreenRenderState.Error -> showError(state.message)
            }
        }

        fun showLoading() = Unit
        fun showContent(state: GoalsUiState) = Unit
        fun showEmpty() = Unit
        fun showError(message: String) = Unit
        fun showIepUploading(isUploading: Boolean)
        fun showIepUploadSuccess(document: IepDocument)
        fun showIepUploadError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun loadData(childId: String, role: UserRole)
        suspend fun submitIepDocument(
            childId: String,
            role: UserRole,
            fileName: String,
            mimeType: String?,
            fileBytes: ByteArray,
            semesterGoal: String,
            monthlyGoal: String,
            weeklyGoals: List<IepWeeklyGoalInput>,
            notes: String? = null
        )
    }
}
