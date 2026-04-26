package com.example.xinggui.presentation.main

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
import com.example.xinggui.data.model.ResourceRuntimeState
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MainPresenterTest {
    @Test
    fun selectingChildReloadsShellState() = runBlocking {
        val repository = FakeRepository()
        val presenter = MainPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.loadShell()
        val initialState = view.lastState
        assertEquals("林老师", initialState?.currentUserName)
        assertEquals("teacher001", initialState?.username)
        assertEquals("teacher001", initialState?.currentUserId)
        assertEquals(UserRole.TEACHER, initialState?.currentRole)
        assertEquals(listOf(UserRole.TEACHER), initialState?.availableRoles)
        assertEquals("child001", initialState?.currentChild?.childId)

        presenter.onChildSelected("child002")
        assertEquals("child002", view.lastState?.currentChild?.childId)
        assertEquals(2, view.lastState?.availableChildren?.size)
    }

    @Test
    fun logoutClearsSessionAndNavigatesToLogin() = runBlocking {
        val repository = FakeRepository()
        val presenter = MainPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.onLogoutClicked()

        assertEquals(false, repository.getSessionState().isAuthenticated)
        assertEquals(true, view.navigatedToLogin)
    }

    private class RecordingView : MainContract.View {
        var lastState: MainShellUiState? = null
        var lastError: String? = null
        var navigatedToLogin: Boolean = false

        override fun showShell(state: MainShellUiState) {
            lastState = state
        }

        override fun showError(message: String) {
            lastError = message
        }

        override fun navigateToLogin() {
            navigatedToLogin = true
        }
    }

    private class FakeRepository : AppRepository {
        private val children = listOf(
            ChildProfile("child001", "晨晨", 7, "8个月", assignedTeacherIds = listOf("teacher001")),
            ChildProfile("child002", "朵朵", 6, "5个月", assignedTeacherIds = listOf("teacher001"))
        )
        private var session = SessionState(
            authToken = "token",
            currentUserId = "teacher001",
            username = "teacher001",
            displayName = "林老师",
            availableRoles = listOf(UserRole.TEACHER),
            activeRole = UserRole.TEACHER,
            selectedChildId = "child001",
            isAuthenticated = true
        )

        override fun getSessionState(): SessionState = session

        override suspend fun restoreSession(): SessionState = session

        override suspend fun login(account: String, password: String): SessionState = session

        override suspend fun logout(): SessionState {
            session = SessionState()
            return session
        }

        override suspend fun register(
            username: String,
            name: String,
            email: String,
            password: String,
            roles: List<UserRole>
        ): SessionState = session

        override suspend fun updateRole(role: UserRole): SessionState {
            session = session.copy(activeRole = role)
            return session
        }

        override suspend fun updateSelectedChild(childId: String): SessionState {
            session = session.copy(selectedChildId = childId)
            return session
        }

        override suspend fun getChildById(childId: String): ChildProfile? = children.firstOrNull { it.childId == childId }

        override suspend fun getChildrenForActiveRole(): List<ChildProfile> = children

        override suspend fun getGoalPlan(childId: String): GoalPlan? = null

        override suspend fun getLatestIepDocument(childId: String): IepDocument? = null

        override suspend fun uploadIepDocument(
            childId: String,
            fileName: String,
            mimeType: String?,
            fileBytes: ByteArray,
            semesterGoal: String,
            monthlyGoal: String,
            weeklyGoals: List<IepWeeklyGoalInput>,
            notes: String?
        ): IepUploadResult = error("Not implemented")

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
            return ReportLoadResult(
                report = null,
                source = ReportDataSource.REMOTE_API,
                fallbackUsed = false
            )
        }

        override suspend fun getReportHistory(childId: String): List<ReportHistoryEntry> = emptyList()

        override suspend fun getResources(): List<ResourceItem> = emptyList()

        override suspend fun getResourceRuntimeState(): ResourceRuntimeState = ResourceRuntimeState()

        override suspend fun saveResourceRuntimeState(state: ResourceRuntimeState) = Unit
    }
}
