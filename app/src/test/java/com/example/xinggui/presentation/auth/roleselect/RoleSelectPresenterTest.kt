package com.example.xinggui.presentation.auth.roleselect

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
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleSelectPresenterTest {
    @Test
    fun loadInitialSelectionUsesRepositoryRole() {
        val repository = FakeRepository(
            SessionState(
                authToken = "token",
                currentUserId = "parent001",
                availableRoles = listOf(UserRole.PARENT, UserRole.TEACHER),
                activeRole = UserRole.PARENT,
                isAuthenticated = true
            )
        )
        val presenter = RoleSelectPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.loadInitialSelection()

        assertEquals(UserRole.PARENT, view.selectedRole)
        assertEquals(2, view.availableRoles.size)
    }

    @Test
    fun continueUpdatesRoleAndNavigates() = runBlocking {
        val repository = FakeRepository(
            SessionState(
                authToken = "token",
                currentUserId = "parent001",
                availableRoles = listOf(UserRole.PARENT, UserRole.TEACHER),
                activeRole = UserRole.PARENT,
                isAuthenticated = true
            )
        )
        val presenter = RoleSelectPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.onContinue(UserRole.TEACHER)

        assertEquals(UserRole.TEACHER, repository.session.activeRole)
        assertTrue(view.navigated)
    }

    private class RecordingView : RoleSelectContract.View {
        var availableRoles: List<UserRole> = emptyList()
        var selectedRole: UserRole? = null
        var navigated: Boolean = false

        override fun showRoleOptions(roles: List<UserRole>, selectedRole: UserRole?) {
            availableRoles = roles
            this.selectedRole = selectedRole
        }

        override fun showError(message: String) = Unit

        override fun showSubmitting(submitting: Boolean) = Unit

        override fun navigateToMain() {
            navigated = true
        }
    }

    private class FakeRepository(
        var session: SessionState
    ) : AppRepository {
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
        ): ChildProfile = ChildProfile(
            childId = childId,
            name = name,
            age = 0,
            interventionDuration = "",
            birthDate = birthDate,
            interventionStartDate = interventionStartDate,
            avatarKey = avatarKey
        )

        override suspend fun getChildById(childId: String): ChildProfile? = null

        override suspend fun getChildrenForActiveRole(): List<ChildProfile> = emptyList()

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
