package com.example.xinggui.presentation.goals

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

class GoalsPresenter(
    private val repository: AppRepository
) : GoalsContract.Presenter {
    private var view: GoalsContract.View? = null

    override fun attachView(view: GoalsContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadData(childId: String, role: UserRole) {
        view?.showLoading()
        val child = repository.getChildById(childId)
        val goal = repository.getGoalPlan(childId)
        if (child == null || goal == null) {
            view?.showEmpty()
            return
        }
        view?.showContent(
            GoalsUiState(
                childName = child.name,
                role = role,
                semesterGoal = goal.semesterGoal,
                monthlyGoal = goal.monthlyGoal,
                weeklyTasks = goal.weeklyCheckIns.map {
                    GoalTaskUiModel(it.title, it.completed, it.rewardStars)
                },
                uploadHint = "个别化教育计划上传入口目前为占位，后续会接入文件选择和结构化录入。",
                aiHint = "智能分析区域当前为静态示例文案，用于首版脚手架展示。"
            )
        )
    }
}
