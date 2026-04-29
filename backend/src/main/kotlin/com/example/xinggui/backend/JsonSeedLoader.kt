package com.example.xinggui.backend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path

data class SeedUser(
    val userId: String,
    val username: String? = null,
    val name: String,
    val displayName: String? = null,
    val role: String? = null,
    val roles: List<String>? = null,
    val email: String? = null,
    val childIds: List<String> = emptyList(),
    val avatarKey: String? = null
) {
    fun normalizedRoles(): List<String> {
        return (roles ?: listOfNotNull(role))
            .map { it.trim().uppercase() }
            .filter { BackendRole.parse(it) != null }
            .distinct()
            .ifEmpty { listOf(BackendRole.PARENT.name) }
    }
}

data class SeedChild(
    val childId: String,
    val name: String,
    val age: Int,
    val interventionDuration: String,
    val birthDate: String? = null,
    val interventionStartDate: String? = null,
    val avatarKey: String? = null,
    val guardianIds: List<String> = emptyList(),
    val assignedTeacherIds: List<String> = emptyList()
)

data class SeedWeeklyCheckIn(
    val itemId: String,
    val dimensionId: String = "cognition",
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class SeedGoalPlan(
    val childId: String,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyCheckIns: List<SeedWeeklyCheckIn> = emptyList()
)

data class SeedReportSummary(
    val childId: String,
    val overview: String,
    val overallEvaluation: String,
    val nextSuggestions: String,
    val aiAnalysis: String,
    val dimensionScores: Map<String, Int> = emptyMap(),
    val dimensionHighlights: List<String> = emptyList()
)

data class SeedResourceItem(
    val resourceId: String,
    val title: String,
    val category: String,
    val isPaid: Boolean,
    val summary: String,
    val recommendedReason: String,
    val assetPath: String? = null,
    val sourceUrl: String? = null
)

data class SeedBundle(
    val users: List<SeedUser>,
    val children: List<SeedChild>,
    val goals: List<SeedGoalPlan>,
    val reports: List<SeedReportSummary>,
    val resources: List<SeedResourceItem>
)

class JsonSeedLoader(
    private val gson: Gson,
    private val seedDir: Path
) {
    fun loadBundle(): SeedBundle {
        return SeedBundle(
            users = readList("users.json"),
            children = readList("children.json"),
            goals = readList("goals.json"),
            reports = readList("reports.json"),
            resources = readList("resources.json")
        )
    }

    private inline fun <reified T> readList(fileName: String): List<T> {
        val path = seedDir.resolve(fileName)
        if (!Files.exists(path)) {
            return emptyList()
        }
        val json = Files.newBufferedReader(path, Charsets.UTF_8).use { it.readText() }
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson<List<T>>(json, type).orEmpty()
    }
}
