package com.example.xinggui.presentation.goals

import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.IepDocument
import com.example.xinggui.data.model.IepWeeklyGoalInput
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.presentation.common.ScreenRenderState

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

    override suspend fun loadData(childId: String, role: UserRole) {
        view?.render(ScreenRenderState.Loading)
        runCatching {
            buildUiState(childId = childId, role = role)
        }.onSuccess { state ->
            if (state == null) {
                view?.render(ScreenRenderState.Empty("暂无目标数据"))
            } else {
                view?.render(ScreenRenderState.Content(state))
            }
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "加载失败"))
        }
    }

    override suspend fun submitIepDocument(
        childId: String,
        role: UserRole,
        fileName: String,
        mimeType: String?,
        fileBytes: ByteArray,
        semesterGoal: String,
        monthlyGoal: String,
        weeklyGoals: List<IepWeeklyGoalInput>,
        notes: String?
    ) {
        view?.showIepUploading(true)
        runCatching {
            validateIepInput(fileName, fileBytes, semesterGoal, monthlyGoal, weeklyGoals)
            val result = repository.uploadIepDocument(
                childId = childId,
                fileName = fileName,
                mimeType = mimeType,
                fileBytes = fileBytes,
                semesterGoal = semesterGoal,
                monthlyGoal = monthlyGoal,
                weeklyGoals = weeklyGoals,
                notes = notes
            )
            buildUiState(
                childId = childId,
                role = role,
                goalPlan = result.goalPlan,
                latestIepDocument = result.document
            ) to result.document
        }.onSuccess { (state, document) ->
            if (state == null) {
                view?.render(ScreenRenderState.Empty("暂无目标数据"))
            } else {
                view?.render(ScreenRenderState.Content(state))
                view?.showIepUploadSuccess(document)
            }
        }.onFailure { error ->
            view?.showIepUploadError(error.message ?: "IEP 上传失败")
        }
        view?.showIepUploading(false)
    }

    private suspend fun buildUiState(
        childId: String,
        role: UserRole,
        goalPlan: GoalPlan? = null,
        latestIepDocument: IepDocument? = null
    ): GoalsUiState? {
        val child = repository.getChildById(childId)
        val goal = goalPlan ?: repository.getGoalPlan(childId)
        if (child == null || goal == null) {
            return null
        }
        val iepDocument = latestIepDocument ?: repository.getLatestIepDocument(childId)
        return GoalsUiState(
            childName = child.name,
            role = role,
            semesterGoal = goal.semesterGoal,
            monthlyGoal = goal.monthlyGoal,
            weeklyTasks = goal.weeklyCheckIns.map {
                GoalTaskUiModel(
                    dimensionId = it.dimensionId,
                    title = it.title,
                    completed = it.completed,
                    rewardStars = it.rewardStars
                )
            },
            uploadHint = if (iepDocument == null) {
                "请选择 IEP 文件，并同步录入长期目标、月目标和微目标。"
            } else {
                "最近上传：${iepDocument.originalFileName}，结构化目标已同步到星目标。"
            },
            aiHint = "智能分析区域当前为阶段性说明文案，后续将结合评估结果自动生成。",
            latestIepDocument = iepDocument
        )
    }

    private fun validateIepInput(
        fileName: String,
        fileBytes: ByteArray,
        semesterGoal: String,
        monthlyGoal: String,
        weeklyGoals: List<IepWeeklyGoalInput>
    ) {
        require(fileName.isNotBlank()) { "请选择 IEP 文件" }
        require(fileBytes.isNotEmpty()) { "IEP 文件不能为空" }
        require(fileBytes.size <= 10 * 1024 * 1024) { "IEP 文件不能超过 10MB" }
        require(semesterGoal.isNotBlank()) { "请填写长期目标" }
        require(monthlyGoal.isNotBlank()) { "请填写月目标" }
        require(weeklyGoals.isNotEmpty()) { "请至少填写一个微目标" }
        weeklyGoals.forEachIndexed { index, goal ->
            require(goal.dimensionId.isNotBlank()) { "第 ${index + 1} 个微目标缺少维度" }
            require(goal.title.isNotBlank()) { "第 ${index + 1} 个微目标缺少标题" }
            require(goal.rewardStars in 1..5) { "第 ${index + 1} 个微目标奖励星数需在 1-5 之间" }
        }
    }
}
