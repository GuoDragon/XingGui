package com.example.xinggui.backend

import io.ktor.http.HttpStatusCode
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong

enum class BackendRole {
    PARENT,
    TEACHER,
    ADMIN;

    companion object {
        fun parse(value: String?): BackendRole? {
            return entries.firstOrNull { it.name == value?.trim()?.uppercase() }
        }
    }
}

data class ApiError(
    val message: String,
    val requestId: String? = null
)

class ApiException(
    val status: HttpStatusCode,
    override val message: String
) : RuntimeException(message)

data class RegisterRequest(
    val username: String,
    val name: String,
    val email: String? = null,
    val password: String,
    val roles: List<String> = emptyList(),
    val captchaId: String? = null,
    val captchaAnswer: String? = null,
    val deviceId: String? = null
)

data class LoginRequest(
    val account: String,
    val password: String,
    val deviceId: String? = null
)

data class ActiveRoleRequest(
    val role: String
)

data class SelectedChildRequest(
    val childId: String
)

data class UserProfileUpdateRequest(
    val displayName: String,
    val email: String? = null,
    val avatarKey: String? = null
)

data class ChildProfileUpdateRequest(
    val name: String,
    val birthDate: String? = null,
    val interventionStartDate: String? = null,
    val avatarKey: String? = null
)

data class RoleMutationRequest(
    val role: String
)

data class ArchiveCheckInRequest(
    val childId: String,
    val itemId: String,
    val note: String,
    val stars: Int,
    val completed: Boolean
)

data class IepWeeklyGoalInput(
    val dimensionId: String,
    val title: String,
    val rewardStars: Int
)

data class UploadedIepFile(
    val originalFileName: String,
    val contentType: String?,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadedIepFile) return false
        return originalFileName == other.originalFileName &&
            contentType == other.contentType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = originalFileName.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class IepUploadRequest(
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyGoals: List<IepWeeklyGoalInput> = emptyList(),
    val notes: String? = null,
    val file: UploadedIepFile
)

data class PublicUser(
    val userId: String,
    val username: String,
    val name: String,
    val displayName: String? = null,
    val email: String? = null,
    val avatarKey: String? = null,
    val roles: List<String> = emptyList(),
    val childIds: List<String> = emptyList()
)

data class PublicChildProfile(
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

data class PublicWeeklyCheckIn(
    val itemId: String,
    val dimensionId: String = "cognition",
    val title: String,
    val completed: Boolean,
    val rewardStars: Int
)

data class PublicGoalPlan(
    val childId: String,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyCheckIns: List<PublicWeeklyCheckIn> = emptyList()
)

data class PublicReportSummary(
    val childId: String,
    val overview: String,
    val overallEvaluation: String,
    val nextSuggestions: String,
    val aiAnalysis: String,
    val dimensionScores: Map<String, Int> = emptyMap(),
    val dimensionHighlights: List<String> = emptyList()
)

data class PublicReportHistoryEntry(
    val entryId: String,
    val childId: String,
    val sourceItemId: String,
    val sourceDimensionId: String,
    val note: String,
    val generatedAt: Long,
    val dimensionScores: Map<String, Int>,
    val overview: String,
    val aiAnalysis: String,
    val overallEvaluation: String,
    val nextSuggestions: String
)

data class PublicCheckInResult(
    val success: Boolean,
    val earnedStars: Int = 0,
    val updatedReport: PublicReportSummary? = null,
    val message: String? = null
)

data class PublicResourceItem(
    val resourceId: String,
    val title: String,
    val category: String,
    val isPaid: Boolean,
    val summary: String,
    val recommendedReason: String,
    val assetPath: String? = null,
    val sourceUrl: String? = null
)

data class PublicResourceRuntimeState(
    val unlockedResourceIds: Set<String> = emptySet(),
    val searchHistory: List<String> = emptyList()
)

data class PublicIepDocument(
    val documentId: String,
    val childId: String,
    val uploadedBy: String,
    val originalFileName: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val semesterGoal: String,
    val monthlyGoal: String,
    val weeklyGoals: List<IepWeeklyGoalInput> = emptyList(),
    val notes: String? = null,
    val uploadedAt: Long,
    val safetyStatus: String = "PASSED_BY_RULES",
    val contentSafetyStatus: String = "PASSED_BY_RULES"
)

data class PublicIepUploadResult(
    val document: PublicIepDocument,
    val goalPlan: PublicGoalPlan,
    val documentId: String = document.documentId,
    val fileSizeBytes: Long = document.fileSizeBytes,
    val contentType: String = document.contentType,
    val safetyStatus: String = document.safetyStatus,
    val uploadedAt: Long = document.uploadedAt
)

data class SessionResponse(
    val token: String,
    val user: PublicUser,
    val availableRoles: List<String>,
    val activeRole: String? = null,
    val selectedChildId: String? = null,
    val mobileEntryAllowed: Boolean,
    val message: String? = null
)

data class RolesResponse(
    val roles: List<String>,
    val activeRole: String? = null,
    val selectedChildId: String? = null
)

data class CaptchaResponse(
    val captchaId: String,
    val question: String,
    val expiresAt: Long
)

data class HealthResponse(
    val status: String,
    val app: String,
    val database: String,
    val uploadDirWritable: Boolean,
    val seedAccounts: Map<String, String>,
    val mode: String,
    val time: Long
)

data class PoolMetrics(
    val activeConnections: Int,
    val idleConnections: Int,
    val totalConnections: Int,
    val threadsAwaitingConnection: Int
)

data class BasicMetricsResponse(
    val uptimeMillis: Long,
    val totalRequests: Long,
    val averageLatencyMs: Double,
    val p95LatencyMs: Double,
    val loginFailureCount: Long,
    val uploadCount: Long,
    val rateLimitHitCount: Long,
    val auditLogCount: Long,
    val pendingEventCount: Long,
    val pool: PoolMetrics?
)

object BasicMetrics {
    private const val MAX_RECENT = 512
    private val startedAt = System.currentTimeMillis()
    private val totalRequests = AtomicLong()
    private val totalLatencyNanos = AtomicLong()
    private val loginFailureCount = AtomicLong()
    private val uploadCount = AtomicLong()
    private val rateLimitHitCount = AtomicLong()
    private val recentLatencies = ArrayDeque<Long>()

    @Synchronized
    fun recordRequest(latencyNanos: Long) {
        totalRequests.incrementAndGet()
        totalLatencyNanos.addAndGet(latencyNanos.coerceAtLeast(0L))
        recentLatencies.addLast(latencyNanos.coerceAtLeast(0L))
        while (recentLatencies.size > MAX_RECENT) {
            recentLatencies.removeFirst()
        }
    }

    fun recordUpload() {
        uploadCount.incrementAndGet()
    }

    fun recordLoginFailure() {
        loginFailureCount.incrementAndGet()
    }

    fun recordRateLimitHit() {
        rateLimitHitCount.incrementAndGet()
    }

    @Synchronized
    fun snapshot(
        pool: PoolMetrics?,
        auditLogCount: Long,
        pendingEventCount: Long
    ): BasicMetricsResponse {
        val total = totalRequests.get()
        val average = if (total == 0L) 0.0 else nanosToMillis(totalLatencyNanos.get().toDouble() / total)
        val sorted = recentLatencies.toList().sorted()
        val p95 = if (sorted.isEmpty()) {
            0.0
        } else {
            val index = ((sorted.size - 1) * 0.95).toInt().coerceIn(0, sorted.lastIndex)
            nanosToMillis(sorted[index].toDouble())
        }
        return BasicMetricsResponse(
            uptimeMillis = System.currentTimeMillis() - startedAt,
            totalRequests = total,
            averageLatencyMs = roundMillis(average),
            p95LatencyMs = roundMillis(p95),
            loginFailureCount = loginFailureCount.get(),
            uploadCount = uploadCount.get(),
            rateLimitHitCount = rateLimitHitCount.get(),
            auditLogCount = auditLogCount,
            pendingEventCount = pendingEventCount,
            pool = pool
        )
    }

    private fun nanosToMillis(nanos: Double): Double = nanos / 1_000_000.0

    private fun roundMillis(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }
}
