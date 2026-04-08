package com.example.xinggui.presentation.archive

import com.example.xinggui.data.model.GrowthDimension
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

class ArchivePresenter(
    private val repository: AppRepository
) : ArchiveContract.Presenter {
    private var view: ArchiveContract.View? = null

    override fun attachView(view: ArchiveContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadData(childId: String, role: UserRole) {
        view?.showLoading()
        val child = repository.getChildById(childId)
        val goal = repository.getGoalPlan(childId)
        val report = repository.getReportSummary(childId)
        if (child == null || goal == null || report == null) {
            view?.showEmpty()
            return
        }
        val dimensions = GrowthDimension.entries.map { dimension ->
            ArchiveDimensionUiModel(
                title = dimension.displayName,
                score = report.dimensionScores[dimension.id] ?: 0
            )
        }
        val weeklyItems = goal.weeklyCheckIns.map {
            ArchiveWeeklyUiModel(
                title = it.title,
                completed = it.completed,
                rewardStars = it.rewardStars
            )
        }
        val earnedStars = weeklyItems.filter { it.completed }.sumOf { it.rewardStars }
        val totalStars = weeklyItems.sumOf { it.rewardStars }
        val roleHint = when (role) {
            UserRole.PARENT -> "家长端更偏向记录、追踪与跟进。"
            UserRole.TEACHER -> "教师端更偏向复盘、指导与评估。"
        }
        view?.showContent(
            ArchiveUiState(
                childName = child.name,
                role = role,
                semesterGoal = goal.semesterGoal,
                monthlyGoal = goal.monthlyGoal,
                dimensions = dimensions,
                weeklyItems = weeklyItems,
                earnedStars = earnedStars,
                totalStars = totalStars,
                roleHint = roleHint
            )
        )
    }
}
