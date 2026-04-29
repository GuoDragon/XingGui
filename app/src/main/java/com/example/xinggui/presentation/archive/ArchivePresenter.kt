package com.example.xinggui.presentation.archive

import com.example.xinggui.data.model.GrowthDimension
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.model.displayAge
import com.example.xinggui.data.model.displayInterventionDuration
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.presentation.common.ScreenRenderState

class ArchivePresenter(
    private val repository: AppRepository
) : ArchiveContract.Presenter {

    private var view: ArchiveContract.View? = null
    private var currentChildId: String? = null
    private var currentRole: UserRole? = null

    override fun attachView(view: ArchiveContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override suspend fun loadData(childId: String, role: UserRole) {
        currentChildId = childId
        currentRole = role
        view?.render(ScreenRenderState.Loading)
        runCatching {
            val child = repository.getChildById(childId)
            val goal = repository.getGoalPlan(childId)
            val report = repository.getReportSummary(childId)
            if (child == null || goal == null || report == null) {
                null
            } else {
                val dimensions = GrowthDimension.entries.map { dimension ->
                    val dimensionItems = goal.weeklyCheckIns.filter { it.dimensionId == dimension.id }
                    val earned = dimensionItems.filter { it.completed }.sumOf { it.rewardStars }
                    ArchiveDimensionUiModel(
                        id = dimension.id,
                        title = dimension.displayName,
                        score = earned
                    )
                }

                val weeklyItems = goal.weeklyCheckIns.map {
                    ArchiveWeeklyUiModel(
                        id = it.itemId,
                        dimensionId = it.dimensionId,
                        title = it.title,
                        completed = it.completed,
                        rewardStars = it.rewardStars
                    )
                }

                val earnedStars = weeklyItems.filter { it.completed }.sumOf { it.rewardStars }
                val totalStars = weeklyItems.sumOf { it.rewardStars }
                val weeklyCheckInCountsByDimension = repository.getWeeklyCheckInCounts(childId)
                val roleHint = when (role) {
                    UserRole.PARENT -> "请根据孩子本周的真实表现进行记录，多给予鼓励与奖励星星。"
                    UserRole.TEACHER -> "本周反馈意见：建议引导孩子多次尝试，并督促家长完成家庭打卡。"
                }
                ArchiveUiState(
                    childName = child.name,
                    childAge = child.displayAge(),
                    childInterventionDuration = child.displayInterventionDuration(),
                    childBirthDate = child.birthDate,
                    childAvatarKey = child.avatarKey,
                    role = role,
                    semesterGoal = goal.semesterGoal,
                    monthlyGoal = goal.monthlyGoal,
                    dimensions = dimensions,
                    weeklyItems = weeklyItems,
                    earnedStars = earnedStars,
                    totalStars = totalStars,
                    roleHint = roleHint,
                    weeklyCheckInCountsByDimension = weeklyCheckInCountsByDimension
                )
            }
        }.onSuccess { state ->
            if (state == null) {
                view?.render(ScreenRenderState.Empty("暂无档案展示数据"))
            } else {
                view?.render(ScreenRenderState.Content(state))
            }
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "加载失败"))
        }
    }

    override suspend fun performCheckIn(itemId: String, note: String, stars: Int, isCompleted: Boolean) {
        val childId = currentChildId ?: return
        runCatching {
            repository.submitArchiveCheckIn(
                childId = childId,
                itemId = itemId,
                note = note,
                stars = stars,
                completed = isCompleted
            )
        }.onSuccess { result ->
            if (result.success) {
                view?.showCheckInFeedback(true, result.earnedStars)
                currentRole?.let { role -> loadData(childId, role) }
            } else {
                view?.showCheckInFeedback(false, 0)
            }
        }.onFailure { error ->
            view?.showError(error.message ?: "打卡失败")
        }
    }
}
