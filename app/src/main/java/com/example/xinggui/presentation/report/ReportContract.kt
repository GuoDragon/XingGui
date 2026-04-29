package com.example.xinggui.presentation.report

import com.example.xinggui.data.model.ReportDataSource
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ScreenRenderState

data class DimensionScoreUiModel(
    val title: String,
    val score: Int
)

data class ReportHistoryUiModel(
    val id: String,
    val generatedAtLabel: String,
    val note: String,
    val overview: String
)

data class ReportUiState(
    val childName: String,
    val age: Int,
    val interventionDuration: String,
    val childBirthDate: String? = null,
    val childAvatarKey: String? = null,
    val role: UserRole,
    val dimensions: List<DimensionScoreUiModel>,
    val overview: String,
    val aiAnalysis: String,
    val overallEvaluation: String,
    val nextSuggestions: String,
    val highlights: List<String>,
    val feedbackHint: String,
    val dataSource: ReportDataSource,
    val fallbackUsed: Boolean,
    val history: List<ReportHistoryUiModel>
)

interface ReportContract {
    interface View {
        fun render(state: ScreenRenderState<ReportUiState>) {
            when (state) {
                ScreenRenderState.Loading -> showLoading()
                is ScreenRenderState.Content -> showContent(state.data)
                is ScreenRenderState.Empty -> showEmpty()
                is ScreenRenderState.Error -> showError(state.message)
            }
        }

        fun showLoading() = Unit
        fun showContent(state: ReportUiState) = Unit
        fun showEmpty() = Unit
        fun showError(message: String) = Unit
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun loadData(childId: String, role: UserRole)
    }
}
