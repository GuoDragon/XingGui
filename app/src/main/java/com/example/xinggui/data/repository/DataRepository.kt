package com.example.xinggui.data.repository

import android.content.Context
import com.example.xinggui.common.constants.AppConstants
import com.example.xinggui.data.model.AppConfig
import com.example.xinggui.data.model.AppUser
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

object DataRepository : AppRepository {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private lateinit var appContext: Context
    private var initialized = false
    private lateinit var appConfig: AppConfig
    private var users: List<AppUser> = emptyList()
    private var children: List<ChildProfile> = emptyList()
    private var goals: List<GoalPlan> = emptyList()
    private var reports: List<ReportSummary> = emptyList()
    private var resources: List<ResourceItem> = emptyList()
    private lateinit var sessionState: SessionState

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        appConfig = readAssetObject(AppConstants.APP_CONFIG_FILE, AppConfig::class.java)
        users = readAssetList(AppConstants.USERS_FILE)
        children = readAssetList(AppConstants.CHILDREN_FILE)
        goals = readAssetList(AppConstants.GOALS_FILE)
        reports = readAssetList(AppConstants.REPORTS_FILE)
        resources = readAssetList(AppConstants.RESOURCES_FILE)
        sessionState = loadOrCreateSessionState()
        initialized = true
    }

    override fun getSessionState(): SessionState = sessionState

    override fun updateRole(role: UserRole): SessionState {
        val defaultUserId = when (role) {
            UserRole.PARENT -> appConfig.defaultParentUserId
            UserRole.TEACHER -> appConfig.defaultTeacherUserId
        }
        val user = users.firstOrNull { it.userId == defaultUserId && it.role == role }
            ?: users.firstOrNull { it.role == role }
            ?: throw IllegalStateException("No user found for role $role")
        val selectedChildId = getChildrenForUser(user.userId, role).firstOrNull()?.childId
            ?: sessionState.selectedChildId
        sessionState = SessionState(
            currentUserId = user.userId,
            role = role,
            selectedChildId = selectedChildId
        )
        persistSessionState()
        return sessionState
    }

    override fun updateSelectedChild(childId: String): SessionState {
        val accessibleChildIds = getChildrenForUser(sessionState.currentUserId, sessionState.role)
            .map { it.childId }
        if (childId in accessibleChildIds) {
            sessionState = sessionState.copy(selectedChildId = childId)
            persistSessionState()
        }
        return sessionState
    }

    override fun getCurrentUser(): AppUser? {
        return users.firstOrNull { it.userId == sessionState.currentUserId }
    }

    override fun getUsers(): List<AppUser> = users

    override fun getChildById(childId: String): ChildProfile? {
        return children.firstOrNull { it.childId == childId }
    }

    override fun getChildrenForUser(userId: String, role: UserRole): List<ChildProfile> {
        return when (role) {
            UserRole.PARENT -> children.filter { userId in it.guardianIds }
            UserRole.TEACHER -> children.filter { userId in it.assignedTeacherIds }
        }
    }

    override fun getGoalPlan(childId: String): GoalPlan? {
        return goals.firstOrNull { it.childId == childId }
    }

    override fun getReportSummary(childId: String): ReportSummary? {
        return reports.firstOrNull { it.childId == childId }
    }

    override fun getResources(): List<ResourceItem> = resources

    private fun loadOrCreateSessionState(): SessionState {
        val file = sessionFile()
        if (file.exists()) {
            return gson.fromJson(file.readText(Charsets.UTF_8), SessionState::class.java)
        }
        val initialUser = users.firstOrNull { it.userId == appConfig.defaultParentUserId }
            ?: users.firstOrNull()
            ?: throw IllegalStateException("No users found in assets")
        val initialRole = initialUser.role
        val initialChildId = getChildrenForUser(initialUser.userId, initialRole).firstOrNull()?.childId
            ?: children.firstOrNull()?.childId
            ?: throw IllegalStateException("No children found in assets")
        return SessionState(
            currentUserId = initialUser.userId,
            role = initialRole,
            selectedChildId = initialChildId
        ).also {
            sessionState = it
            persistSessionState()
        }
    }

    private fun persistSessionState() {
        val file = sessionFile()
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(sessionState), Charsets.UTF_8)
    }

    private inline fun <reified T> readAssetList(path: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        val json = appContext.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return gson.fromJson(json, type)
    }

    private fun <T> readAssetObject(path: String, clazz: Class<T>): T {
        val json = appContext.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return gson.fromJson(json, clazz)
    }

    private fun sessionFile(): File {
        return File(appContext.filesDir, AppConstants.SESSION_STATE_FILE)
    }
}
