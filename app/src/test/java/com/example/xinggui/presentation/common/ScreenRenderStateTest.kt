package com.example.xinggui.presentation.common

import com.example.xinggui.data.model.ReportDataSource
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.report.ReportContract
import com.example.xinggui.presentation.report.ReportUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenRenderStateTest {
    @Test
    fun reportViewDefaultRenderMapsAllStateBranches() {
        val view = RecordingReportView()
        val content = ReportUiState(
            childName = "晨晨",
            age = 7,
            interventionDuration = "8个月",
            role = UserRole.PARENT,
            dimensions = emptyList(),
            overview = "整体稳定",
            aiAnalysis = "继续保持",
            overallEvaluation = "良好",
            nextSuggestions = "保持规律练习",
            highlights = emptyList(),
            feedbackHint = "可分享给教师",
            dataSource = ReportDataSource.REMOTE_API,
            fallbackUsed = false,
            history = emptyList()
        )

        view.render(ScreenRenderState.Loading)
        view.render(ScreenRenderState.Content(content))
        view.render(ScreenRenderState.Empty("暂无报告"))
        view.render(ScreenRenderState.Error("网络异常"))

        assertEquals(listOf("loading", "content:晨晨", "empty", "error:网络异常"), view.events)
    }

    private class RecordingReportView : ReportContract.View {
        val events = mutableListOf<String>()

        override fun showLoading() {
            events += "loading"
        }

        override fun showContent(state: ReportUiState) {
            events += "content:${state.childName}"
        }

        override fun showEmpty() {
            events += "empty"
        }

        override fun showError(message: String) {
            events += "error:$message"
        }
    }
}

