package com.example.xinggui.data.repository

import com.example.xinggui.data.model.CheckInProcessResult
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.IepDocument
import com.example.xinggui.data.model.IepUploadResult
import com.example.xinggui.data.model.IepWeeklyGoalInput
import com.example.xinggui.data.model.ReportHistoryEntry
import com.example.xinggui.data.model.ReportLoadResult
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceRuntimeState
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole

interface AppRepository {
    fun getSessionState(): SessionState
    suspend fun restoreSession(): SessionState
    suspend fun login(account: String, password: String): SessionState
    suspend fun logout(): SessionState
    suspend fun register(
        username: String,
        name: String,
        email: String,
        password: String,
        roles: List<UserRole>
    ): SessionState
    suspend fun updateRole(role: UserRole): SessionState
    suspend fun updateSelectedChild(childId: String): SessionState
    suspend fun getChildById(childId: String): ChildProfile?
    suspend fun getChildrenForActiveRole(): List<ChildProfile>
    suspend fun getGoalPlan(childId: String): GoalPlan?
    suspend fun getLatestIepDocument(childId: String): IepDocument?
    suspend fun uploadIepDocument(
        childId: String,
        fileName: String,
        mimeType: String?,
        fileBytes: ByteArray,
        semesterGoal: String,
        monthlyGoal: String,
        weeklyGoals: List<IepWeeklyGoalInput>,
        notes: String? = null
    ): IepUploadResult
    suspend fun submitArchiveCheckIn(
        childId: String,
        itemId: String,
        note: String,
        stars: Int,
        completed: Boolean
    ): CheckInProcessResult
    suspend fun getWeeklyCheckInCounts(childId: String): Map<String, Int>
    suspend fun getReportSummary(childId: String): ReportSummary?
    suspend fun fetchReport(childId: String): ReportLoadResult
    suspend fun getReportHistory(childId: String): List<ReportHistoryEntry>
    suspend fun getResources(): List<ResourceItem>
    suspend fun getResourceRuntimeState(): ResourceRuntimeState
    suspend fun saveResourceRuntimeState(state: ResourceRuntimeState)
}
