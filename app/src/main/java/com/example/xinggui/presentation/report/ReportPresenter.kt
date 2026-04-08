package com.example.xinggui.presentation.report

import com.example.xinggui.data.model.GrowthDimension
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

class ReportPresenter(
    private val repository: AppRepository
) : ReportContract.Presenter {
    private var view: ReportContract.View? = null

    override fun attachView(view: ReportContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadData(childId: String, role: UserRole) {
        view?.showLoading()
        val child = repository.getChildById(childId)
        val report = repository.getReportSummary(childId)
        if (child == null || report == null) {
            view?.showEmpty()
            return
        }
        val dimensions = GrowthDimension.entries.map { dimension ->
            DimensionScoreUiModel(
                title = dimension.displayName,
                score = report.dimensionScores[dimension.id] ?: 0
            )
        }
        val feedbackHint = when (role) {
            UserRole.PARENT -> "可将雷达图快照与总结一键反馈给教师。"
            UserRole.TEACHER -> "可将雷达图快照与总结一键反馈给家长。"
        }
        view?.showContent(
            ReportUiState(
                childName = child.name,
                age = child.age,
                interventionDuration = child.interventionDuration,
                role = role,
                dimensions = dimensions,
                overview = report.overview,
                aiAnalysis = report.aiAnalysis,
                overallEvaluation = report.overallEvaluation,
                nextSuggestions = report.nextSuggestions,
                highlights = report.dimensionHighlights,
                feedbackHint = feedbackHint
            )
        )
    }
}
