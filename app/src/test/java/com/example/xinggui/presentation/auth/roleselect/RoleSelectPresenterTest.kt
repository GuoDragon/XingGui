package com.example.xinggui.presentation.auth.roleselect

import com.example.xinggui.data.model.AppUser
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleSelectPresenterTest {
    @Test
    fun loadInitialSelectionUsesRepositoryRole() {
        val repository = FakeRepository(SessionState("parent001", UserRole.PARENT, "child001"))
        val presenter = RoleSelectPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.loadInitialSelection()

        assertEquals(UserRole.PARENT, view.selectedRole)
    }

    @Test
    fun continueUpdatesRoleAndNavigates() {
        val repository = FakeRepository(SessionState("parent001", UserRole.PARENT, "child001"))
        val presenter = RoleSelectPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.onContinue(UserRole.TEACHER)

        assertEquals(UserRole.TEACHER, repository.session.role)
        assertTrue(view.navigated)
    }

    private class RecordingView : RoleSelectContract.View {
        var selectedRole: UserRole? = null
        var navigated: Boolean = false

        override fun showSelectedRole(role: UserRole) {
            selectedRole = role
        }

        override fun showError(message: String) = Unit

        override fun navigateToMain() {
            navigated = true
        }
    }

    private class FakeRepository(
        var session: SessionState
    ) : AppRepository {
        override fun getSessionState(): SessionState = session

        override fun updateRole(role: UserRole): SessionState {
            session = session.copy(role = role)
            return session
        }

        override fun updateSelectedChild(childId: String): SessionState = session.copy(selectedChildId = childId)

        override fun getCurrentUser(): AppUser? = null

        override fun getUsers(): List<AppUser> = emptyList()

        override fun getChildById(childId: String): ChildProfile? = null

        override fun getChildrenForUser(userId: String, role: UserRole): List<ChildProfile> = emptyList()

        override fun getGoalPlan(childId: String): GoalPlan? = null

        override fun getReportSummary(childId: String): ReportSummary? = null

        override fun getResources(): List<ResourceItem> = emptyList()
    }
}
