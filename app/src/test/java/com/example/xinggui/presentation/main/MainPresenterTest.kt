package com.example.xinggui.presentation.main

import com.example.xinggui.data.model.AppUser
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class MainPresenterTest {
    @Test
    fun selectingChildReloadsShellState() {
        val repository = FakeRepository()
        val presenter = MainPresenter(repository)
        val view = RecordingView()

        presenter.attachView(view)
        presenter.loadShell()
        assertEquals("child001", view.lastState?.currentChild?.childId)

        presenter.onChildSelected("child002")
        assertEquals("child002", view.lastState?.currentChild?.childId)
        assertEquals(2, view.lastState?.availableChildren?.size)
    }

    private class RecordingView : MainContract.View {
        var lastState: MainShellUiState? = null
        var lastError: String? = null

        override fun showShell(state: MainShellUiState) {
            lastState = state
        }

        override fun showError(message: String) {
            lastError = message
        }
    }

    private class FakeRepository : AppRepository {
        private val teacher = AppUser(
            userId = "teacher001",
            name = "???",
            role = UserRole.TEACHER,
            childIds = listOf("child001", "child002")
        )
        private val children = listOf(
            ChildProfile("child001", "??", 7, "8??", assignedTeacherIds = listOf("teacher001")),
            ChildProfile("child002", "??", 6, "5??", assignedTeacherIds = listOf("teacher001"))
        )
        private var session = SessionState("teacher001", UserRole.TEACHER, "child001")

        override fun getSessionState(): SessionState = session

        override fun updateRole(role: UserRole): SessionState = session.copy(role = role)

        override fun updateSelectedChild(childId: String): SessionState {
            session = session.copy(selectedChildId = childId)
            return session
        }

        override fun getCurrentUser(): AppUser = teacher

        override fun getUsers(): List<AppUser> = listOf(teacher)

        override fun getChildById(childId: String): ChildProfile? = children.firstOrNull { it.childId == childId }

        override fun getChildrenForUser(userId: String, role: UserRole): List<ChildProfile> = children

        override fun getGoalPlan(childId: String): GoalPlan? = null

        override fun getReportSummary(childId: String): ReportSummary? = null

        override fun getResources(): List<ResourceItem> = emptyList()
    }
}
