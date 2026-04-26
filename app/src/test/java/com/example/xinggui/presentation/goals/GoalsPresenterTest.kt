package com.example.xinggui.presentation.goals

import com.example.xinggui.data.model.CheckInProcessResult
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.IepDocument
import com.example.xinggui.data.model.IepUploadResult
import com.example.xinggui.data.model.IepWeeklyGoalInput
import com.example.xinggui.data.model.ReportDataSource
import com.example.xinggui.data.model.ReportHistoryEntry
import com.example.xinggui.data.model.ReportLoadResult
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.ResourceRuntimeState
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.model.WeeklyCheckIn
import com.example.xinggui.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalsPresenterTest {
    @Test
    fun loadDataIncludesLatestIepDocument() = runBlocking {
        val repository = FakeRepository()
        val presenter = GoalsPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.loadData("child001", UserRole.PARENT)

        assertEquals("initial.pdf", view.lastState?.latestIepDocument?.originalFileName)
        assertEquals("最近上传：initial.pdf，结构化目标已同步到星目标。", view.lastState?.uploadHint)
    }

    @Test
    fun uploadSuccessRefreshesGoals() = runBlocking {
        val repository = FakeRepository()
        val presenter = GoalsPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.submitIepDocument(
            childId = "child001",
            role = UserRole.PARENT,
            fileName = "new-iep.pdf",
            mimeType = "application/pdf",
            fileBytes = "%PDF".toByteArray(),
            semesterGoal = "new semester",
            monthlyGoal = "new month",
            weeklyGoals = listOf(IepWeeklyGoalInput("communication", "new weekly", 4))
        )

        assertEquals("new semester", view.lastState?.semesterGoal)
        assertEquals("new weekly", view.lastState?.weeklyTasks?.single()?.title)
        assertEquals("new-iep.pdf", view.successDocument?.originalFileName)
        assertFalse(view.uploading)
    }

    @Test
    fun uploadFailureShowsError() = runBlocking {
        val repository = FakeRepository(uploadShouldFail = true)
        val presenter = GoalsPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.submitIepDocument(
            childId = "child001",
            role = UserRole.PARENT,
            fileName = "new-iep.pdf",
            mimeType = "application/pdf",
            fileBytes = "%PDF".toByteArray(),
            semesterGoal = "new semester",
            monthlyGoal = "new month",
            weeklyGoals = listOf(IepWeeklyGoalInput("communication", "new weekly", 4))
        )

        assertEquals("upload failed", view.uploadError)
        assertFalse(view.uploading)
        assertTrue(view.successDocument == null)
    }

    private class RecordingView : GoalsContract.View {
        var lastState: GoalsUiState? = null
        var uploadError: String? = null
        var successDocument: IepDocument? = null
        var uploading: Boolean = false

        override fun showLoading() = Unit

        override fun showContent(state: GoalsUiState) {
            lastState = state
        }

        override fun showEmpty() = Unit

        override fun showError(message: String) = Unit

        override fun showIepUploading(isUploading: Boolean) {
            uploading = isUploading
        }

        override fun showIepUploadSuccess(document: IepDocument) {
            successDocument = document
        }

        override fun showIepUploadError(message: String) {
            uploadError = message
        }
    }

    private class FakeRepository(
        private val uploadShouldFail: Boolean = false
    ) : AppRepository {
        private val child = ChildProfile("child001", "晨晨", 7, "8个月")
        private var goalPlan = GoalPlan(
            childId = "child001",
            semesterGoal = "initial semester",
            monthlyGoal = "initial month",
            weeklyCheckIns = listOf(WeeklyCheckIn("w1", "cognition", "initial weekly", false, 3))
        )
        private var iepDocument: IepDocument? = IepDocument(
            documentId = "iep001",
            childId = "child001",
            uploadedBy = "parent001",
            originalFileName = "initial.pdf",
            contentType = "application/pdf",
            fileSizeBytes = 128,
            semesterGoal = "initial semester",
            monthlyGoal = "initial month",
            weeklyGoals = listOf(IepWeeklyGoalInput("cognition", "initial weekly", 3)),
            uploadedAt = 1L
        )

        override fun getSessionState(): SessionState = SessionState()

        override suspend fun restoreSession(): SessionState = SessionState()

        override suspend fun login(account: String, password: String): SessionState = SessionState()

        override suspend fun logout(): SessionState = SessionState()

        override suspend fun register(
            username: String,
            name: String,
            email: String,
            password: String,
            roles: List<UserRole>
        ): SessionState = SessionState()

        override suspend fun updateRole(role: UserRole): SessionState = SessionState(activeRole = role)

        override suspend fun updateSelectedChild(childId: String): SessionState = SessionState(selectedChildId = childId)

        override suspend fun getChildById(childId: String): ChildProfile? = child.takeIf { it.childId == childId }

        override suspend fun getChildrenForActiveRole(): List<ChildProfile> = listOf(child)

        override suspend fun getGoalPlan(childId: String): GoalPlan? = goalPlan.takeIf { it.childId == childId }

        override suspend fun getLatestIepDocument(childId: String): IepDocument? = iepDocument?.takeIf { it.childId == childId }

        override suspend fun uploadIepDocument(
            childId: String,
            fileName: String,
            mimeType: String?,
            fileBytes: ByteArray,
            semesterGoal: String,
            monthlyGoal: String,
            weeklyGoals: List<IepWeeklyGoalInput>,
            notes: String?
        ): IepUploadResult {
            if (uploadShouldFail) {
                error("upload failed")
            }
            goalPlan = GoalPlan(
                childId = childId,
                semesterGoal = semesterGoal,
                monthlyGoal = monthlyGoal,
                weeklyCheckIns = weeklyGoals.mapIndexed { index, item ->
                    WeeklyCheckIn("iep_$index", item.dimensionId, item.title, false, item.rewardStars)
                }
            )
            iepDocument = IepDocument(
                documentId = "iep002",
                childId = childId,
                uploadedBy = "parent001",
                originalFileName = fileName,
                contentType = mimeType ?: "application/pdf",
                fileSizeBytes = fileBytes.size.toLong(),
                semesterGoal = semesterGoal,
                monthlyGoal = monthlyGoal,
                weeklyGoals = weeklyGoals,
                notes = notes,
                uploadedAt = 2L
            )
            return IepUploadResult(document = iepDocument!!, goalPlan = goalPlan)
        }

        override suspend fun submitArchiveCheckIn(
            childId: String,
            itemId: String,
            note: String,
            stars: Int,
            completed: Boolean
        ): CheckInProcessResult = CheckInProcessResult(success = false)

        override suspend fun getWeeklyCheckInCounts(childId: String): Map<String, Int> = emptyMap()

        override suspend fun getReportSummary(childId: String): ReportSummary? = null

        override suspend fun fetchReport(childId: String): ReportLoadResult {
            return ReportLoadResult(null, ReportDataSource.REMOTE_API, fallbackUsed = false)
        }

        override suspend fun getReportHistory(childId: String): List<ReportHistoryEntry> = emptyList()

        override suspend fun getResources(): List<ResourceItem> = emptyList()

        override suspend fun getResourceRuntimeState(): ResourceRuntimeState = ResourceRuntimeState()

        override suspend fun saveResourceRuntimeState(state: ResourceRuntimeState) = Unit
    }
}
