package com.example.xinggui.presentation.report

import com.example.xinggui.data.model.GrowthDimension
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.model.displayAge
import com.example.xinggui.data.model.displayInterventionDuration
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.presentation.common.ScreenRenderState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override suspend fun loadData(childId: String, role: UserRole) {
        view?.render(ScreenRenderState.Loading)
        runCatching {
            val child = repository.getChildById(childId)
            val loadResult = repository.fetchReport(childId)
            val report = loadResult.report
            if (child == null || report == null) {
                null
            } else {
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
                val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
                val history = repository.getReportHistory(childId).map {
                    ReportHistoryUiModel(
                        id = it.entryId,
                        generatedAtLabel = dateFormat.format(Date(it.generatedAt)),
                        note = it.note.ifBlank { "未填写备注" },
                        overview = it.overview
                    )
                }
                ReportUiState(
                    childName = child.name,
                    age = child.displayAge(),
                    interventionDuration = child.displayInterventionDuration(),
                    childBirthDate = child.birthDate,
                    childAvatarKey = child.avatarKey,
                    role = role,
                    dimensions = dimensions,
                    overview = report.overview,
                    aiAnalysis = report.aiAnalysis,
                    overallEvaluation = report.overallEvaluation,
                    nextSuggestions = report.nextSuggestions,
                    highlights = report.dimensionHighlights,
                    feedbackHint = feedbackHint,
                    dataSource = loadResult.source,
                    fallbackUsed = loadResult.fallbackUsed,
                    history = history
                )
            }
        }.onSuccess { state ->
            if (state == null) {
                view?.render(ScreenRenderState.Empty("暂无报告展示数据"))
            } else {
                view?.render(ScreenRenderState.Content(state))
            }
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "加载失败"))
        }
    }
}
