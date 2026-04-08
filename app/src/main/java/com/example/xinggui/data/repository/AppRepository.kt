package com.example.xinggui.data.repository

import com.example.xinggui.data.model.AppUser
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.GoalPlan
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.ResourceItem
import com.example.xinggui.data.model.SessionState
import com.example.xinggui.data.model.UserRole

interface AppRepository {
    fun getSessionState(): SessionState
    fun updateRole(role: UserRole): SessionState
    fun updateSelectedChild(childId: String): SessionState
    fun getCurrentUser(): AppUser?
    fun getUsers(): List<AppUser>
    fun getChildById(childId: String): ChildProfile?
    fun getChildrenForUser(userId: String, role: UserRole): List<ChildProfile>
    fun getGoalPlan(childId: String): GoalPlan?
    fun getReportSummary(childId: String): ReportSummary?
    fun getResources(): List<ResourceItem>
}
