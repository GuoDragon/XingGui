package com.example.xinggui.presentation.main

import com.example.xinggui.data.model.CheckInProcessResult
import com.example.xinggui.data.model.CaptchaChallenge
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
        assertEquals("Teacher Lin", initialState?.currentUserName)
        assertEquals("teacher001", initialState?.username)
        assertEquals("teacher001", initialState?.currentUserId)
        assertEquals("teacher001@xinggui.local", initialState?.currentUserEmail)
        assertEquals("user_sun", initialState?.currentUserAvatarKey)
        assertEquals(UserRole.TEACHER, initialState?.currentRole)
        assertEquals(listOf(UserRole.TEACHER), initialState?.availableRoles)
        assertEquals("child001", initialState?.currentChild?.childId)

        presenter.onChildSelected("child002")
        assertEquals("child002", view.lastState?.currentChild?.childId)
        assertEquals(2, view.lastState?.availableChildren?.size)
    }

    @Test
    fun updatingAccountProfileRefreshesShellState() = runBlocking {
        val repository = FakeRepository()
        val presenter = MainPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        val saved = presenter.onAccountProfileSaved(
            displayName = "New Nickname",
            email = "new@example.com",
            avatarKey = "user_leaf"
        )

        assertEquals(true, saved)
        assertEquals("New Nickname", view.lastState?.currentUserName)
        assertEquals("new@example.com", view.lastState?.currentUserEmail)
        assertEquals("user_leaf", view.lastState?.currentUserAvatarKey)
        assertEquals("\u8d26\u53f7\u8d44\u6599\u5df2\u66f4\u65b0", view.lastMessage)
    }

    @Test
    fun updatingChildProfileRefreshesShellState() = runBlocking {
        val repository = FakeRepository()
        val presenter = MainPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        val saved = presenter.onChildProfileSaved(
            childId = "child001",
            name = "Morning",
            birthDate = "2018-09-01",
            interventionStartDate = "2025-08-26",
            avatarKey = "child_mint"
        )

        assertEquals(true, saved)
        assertEquals("Morning", view.lastState?.currentChild?.name)
        assertEquals("2018-09-01", view.lastState?.currentChild?.birthDate)
        assertEquals("2025-08-26", view.lastState?.currentChild?.interventionStartDate)
        assertEquals("child_mint", view.lastState?.currentChild?.avatarKey)
        assertEquals("\u513f\u7ae5\u8d44\u6599\u5df2\u66f4\u65b0", view.lastMessage)
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
        var lastMessage: String? = null
        var navigatedToLogin: Boolean = false

        override fun showShell(state: MainShellUiState) {
            lastState = state
        }

        override fun showError(message: String) {
            lastError = message
        }

        override fun showMessage(message: String) {
            lastMessage = message
        }

        override fun navigateToLogin() {
            navigatedToLogin = true
        }
    }

    private class FakeRepository : AppRepository {
        private val children = mutableListOf(
            ChildProfile("child001", "Morning", 7, "8 months", assignedTeacherIds = listOf("teacher001")),
            ChildProfile("child002", "Dodo", 6, "5 months", assignedTeacherIds = listOf("teacher001"))
        )
        private var session = SessionState(
            authToken = "token",
            currentUserId = "teacher001",
            username = "teacher001",
            displayName = "Teacher Lin",
            email = "teacher001@xinggui.local",
            avatarKey = "user_sun",
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

        override suspend fun logoutAllDevices(): SessionState {
            session = SessionState()
            return session
        }

        override suspend fun requestRegistrationCaptcha(): CaptchaChallenge {
            return CaptchaChallenge("captcha", "1 + 1 = ?", 0L)
        }

        override suspend fun register(
            username: String,
            name: String,
            email: String,
            password: String,
            roles: List<UserRole>,
            captchaId: String?,
            captchaAnswer: String?
        ): SessionState = session

        override suspend fun updateRole(role: UserRole): SessionState {
            session = session.copy(activeRole = role)
            return session
        }

        override suspend fun updateSelectedChild(childId: String): SessionState {
            session = session.copy(selectedChildId = childId)
            return session
        }

        override suspend fun updateCurrentUserProfile(
            displayName: String,
            email: String?,
            avatarKey: String?
        ): SessionState {
            session = session.copy(displayName = displayName, email = email, avatarKey = avatarKey)
            return session
        }

        override suspend fun updateChildProfile(
            childId: String,
            name: String,
            birthDate: String?,
            interventionStartDate: String?,
            avatarKey: String?
        ): ChildProfile {
            val index = children.indexOfFirst { it.childId == childId }
            check(index >= 0) { "child not found" }
            val updated = children[index].copy(
                name = name,
                birthDate = birthDate,
                interventionStartDate = interventionStartDate,
                avatarKey = avatarKey
            )
            children[index] = updated
            return updated
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
