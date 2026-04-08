package com.example.xinggui.presentation.report

import com.example.xinggui.data.model.UserRole

data class DimensionScoreUiModel(
    val title: String,
    val score: Int
)

data class ReportUiState(
    val childName: String,
    val age: Int,
    val interventionDuration: String,
    val role: UserRole,
    val dimensions: List<DimensionScoreUiModel>,
    val overview: String,
    val aiAnalysis: String,
    val overallEvaluation: String,
    val nextSuggestions: String,
    val highlights: List<String>,
    val feedbackHint: String
)

interface ReportContract {
    interface View {
        fun showLoading()
        fun showContent(state: ReportUiState)
        fun showEmpty()
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData(childId: String, role: UserRole)
    }
}
