package com.example.xinggui.data.repository

import android.content.Context
import com.example.xinggui.common.constants.AppConstants
import com.example.xinggui.data.model.AppConfig
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object DataRepository : AppRepository {
    private val gson: Gson = GsonBuilder().create()

    private lateinit var appContext: Context
    private lateinit var appConfig: AppConfig
    private var initialized = false
    private var sessionState: SessionState = SessionState()

    fun init(context: Context) {
        if (initialized) {
            return
        }
        appContext = context.applicationContext
        appConfig = readAssetObject(AppConstants.APP_CONFIG_FILE, AppConfig::class.java)
        sessionState = loadSessionState()
        initialized = true
    }

    override fun getSessionState(): SessionState = sessionState

    override suspend fun restoreSession(): SessionState {
        ensureInitialized()
        val token = sessionState.authToken ?: return sessionState
        return try {
            val payload = request<ApiSessionPayload>(
                method = "GET",
                path = "/auth/me",
                authToken = token
            )
            consumeSessionPayload(payload)
        } catch (error: IllegalStateException) {
            if (error.message?.contains("登录已失效") == true) {
                clearSession()
                sessionState
            } else {
                throw error
            }
        }
    }

    override suspend fun login(account: String, password: String): SessionState {
        ensureInitialized()
        val payload = request<ApiSessionPayload>(
            method = "POST",
            path = "/auth/login",
            body = LoginRequestBody(account = account, password = password),
            authToken = null
        )
        return consumeSessionPayload(payload)
    }

    override suspend fun logout(): SessionState {
        ensureInitialized()
        clearSession()
        return sessionState
    }

    override suspend fun register(
        username: String,
        name: String,
        email: String,
        password: String,
        roles: List<UserRole>
    ): SessionState {
        ensureInitialized()
        val payload = request<ApiSessionPayload>(
            method = "POST",
            path = "/auth/register",
            body = RegisterRequestBody(
                username = username,
                name = name,
                email = email.ifBlank { null },
                password = password,
                roles = roles.map { it.storageValue }
            ),
            authToken = null
        )
        return consumeSessionPayload(payload)
    }

    override suspend fun updateRole(role: UserRole): SessionState {
        ensureInitialized()
        val payload = request<ApiSessionPayload>(
            method = "POST",
            path = "/session/active-role",
            body = ActiveRoleRequestBody(role.storageValue),
            authToken = requireToken()
        )
        return consumeSessionPayload(payload)
    }

    override suspend fun updateSelectedChild(childId: String): SessionState {
        ensureInitialized()
        val payload = request<ApiSessionPayload>(
            method = "POST",
            path = "/session/selected-child",
            body = SelectedChildRequestBody(childId),
            authToken = requireToken()
        )
        return consumeSessionPayload(payload)
    }

    override suspend fun getChildById(childId: String): ChildProfile? {
        ensureInitialized()
        return requestNullable(
            method = "GET",
            path = "/children/$childId",
            authToken = requireToken()
        )
    }

    override suspend fun getChildrenForActiveRole(): List<ChildProfile> {
        ensureInitialized()
        return request(
            method = "GET",
            path = "/children",
            authToken = requireToken()
        )
    }

    override suspend fun getGoalPlan(childId: String): GoalPlan? {
        ensureInitialized()
        return requestNullable(
            method = "GET",
            path = "/goals/$childId",
            authToken = requireToken()
        )
    }

    override suspend fun getLatestIepDocument(childId: String): IepDocument? {
        ensureInitialized()
        return requestNullable(
            method = "GET",
            path = "/goals/$childId/iep",
            authToken = requireToken()
        )
    }

    override suspend fun uploadIepDocument(
        childId: String,
        fileName: String,
        mimeType: String?,
        fileBytes: ByteArray,
        semesterGoal: String,
        monthlyGoal: String,
        weeklyGoals: List<IepWeeklyGoalInput>,
        notes: String?
    ): IepUploadResult {
        ensureInitialized()
        return requestMultipart(
            path = "/goals/$childId/iep",
            fileName = fileName,
            mimeType = mimeType,
            fileBytes = fileBytes,
            fields = buildMap {
                put("semesterGoal", semesterGoal)
                put("monthlyGoal", monthlyGoal)
                put("weeklyGoalsJson", gson.toJson(weeklyGoals))
                notes?.takeIf { it.isNotBlank() }?.let { put("notes", it) }
            },
            responseType = object : TypeToken<IepUploadResult>() {}.type,
            authToken = requireToken()
        ) as IepUploadResult
    }

    override suspend fun submitArchiveCheckIn(
        childId: String,
        itemId: String,
        note: String,
        stars: Int,
        completed: Boolean
    ): CheckInProcessResult {
        ensureInitialized()
        return request(
            method = "POST",
            path = "/archive/checkin",
            body = ArchiveCheckInRequestBody(
                childId = childId,
                itemId = itemId,
                note = note,
                stars = stars,
                completed = completed
            ),
            authToken = requireToken()
        )
    }

    override suspend fun getWeeklyCheckInCounts(childId: String): Map<String, Int> {
        ensureInitialized()
        return request(
            method = "GET",
            path = "/archive/weekly-counts/$childId",
            authToken = requireToken()
        )
    }

    override suspend fun getReportSummary(childId: String): ReportSummary? {
        ensureInitialized()
        return requestNullable(
            method = "GET",
            path = "/reports/$childId",
            authToken = requireToken()
        )
    }

    override suspend fun fetchReport(childId: String): ReportLoadResult {
        val report = getReportSummary(childId)
        return ReportLoadResult(
            report = report,
            source = ReportDataSource.REMOTE_API,
            fallbackUsed = false
        )
    }

    override suspend fun getReportHistory(childId: String): List<ReportHistoryEntry> {
        ensureInitialized()
        return request(
            method = "GET",
            path = "/reports/$childId/history",
            authToken = requireToken()
        )
    }

    override suspend fun getResources(): List<ResourceItem> {
        ensureInitialized()
        return request(
            method = "GET",
            path = "/resources",
            authToken = requireToken()
        )
    }

    override suspend fun getResourceRuntimeState(): ResourceRuntimeState {
        ensureInitialized()
        return request(
            method = "GET",
            path = "/resources/runtime",
            authToken = requireToken()
        )
    }

    override suspend fun saveResourceRuntimeState(state: ResourceRuntimeState) {
        ensureInitialized()
        request<ResourceRuntimeState>(
            method = "PUT",
            path = "/resources/runtime",
            body = state,
            authToken = requireToken()
        )
    }

    private suspend inline fun <reified T> request(
        method: String,
        path: String,
        body: Any? = null,
        authToken: String?
    ): T {
        return request(method, path, object : TypeToken<T>() {}.type, body, authToken, allowNotFound = false) as T
    }

    private suspend inline fun <reified T> requestNullable(
        method: String,
        path: String,
        body: Any? = null,
        authToken: String?
    ): T? {
        return request(method, path, object : TypeToken<T>() {}.type, body, authToken, allowNotFound = true) as T?
    }

    private suspend fun request(
        method: String,
        path: String,
        responseType: Type,
        body: Any?,
        authToken: String?,
        allowNotFound: Boolean
    ): Any? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val connection = try {
            (URL(buildUrl(path)).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 10_000
                readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
                if (!authToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $authToken")
                }
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    outputStream.use { output ->
                        output.write(gson.toJson(body).toByteArray(Charsets.UTF_8))
                    }
                }
            }
        } catch (error: IOException) {
            throw IllegalStateException(connectivityMessage(), error)
        }

        try {
            val statusCode = connection.responseCode
            val responseText = readResponseText(connection, statusCode)
            when {
                statusCode in 200..299 -> {
                    if (responseType == Unit::class.java || responseText.isBlank()) {
                        Unit
                    } else {
                        gson.fromJson<Any>(responseText, responseType)
                    }
                }

                allowNotFound && statusCode == HttpURLConnection.HTTP_NOT_FOUND -> null

                statusCode == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    clearSession()
                    throw IllegalStateException(parseErrorMessage(responseText, "登录已失效，请重新登录"))
                }

                else -> throw IllegalStateException(parseErrorMessage(responseText, "请求失败（$statusCode）"))
            }
        } catch (error: IOException) {
            throw IllegalStateException(connectivityMessage(), error)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun requestMultipart(
        path: String,
        fileName: String,
        mimeType: String?,
        fileBytes: ByteArray,
        fields: Map<String, String>,
        responseType: Type,
        authToken: String
    ): Any? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val boundary = "XingGui-${UUID.randomUUID()}"
        val connection = try {
            (URL(buildUrl(path)).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $authToken")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                outputStream.use { output ->
                    fields.forEach { (name, value) ->
                        output.writeMultipartText(boundary, name, value)
                    }
                    output.writeMultipartFile(
                        boundary = boundary,
                        name = "file",
                        fileName = fileName,
                        mimeType = mimeType?.takeIf { it.isNotBlank() } ?: "application/octet-stream",
                        bytes = fileBytes
                    )
                    output.write("--$boundary--\r\n".toByteArray(Charsets.UTF_8))
                }
            }
        } catch (error: IOException) {
            throw IllegalStateException(connectivityMessage(), error)
        }

        try {
            val statusCode = connection.responseCode
            val responseText = readResponseText(connection, statusCode)
            when {
                statusCode in 200..299 -> {
                    if (responseText.isBlank()) {
                        Unit
                    } else {
                        gson.fromJson<Any>(responseText, responseType)
                    }
                }

                statusCode == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    clearSession()
                    throw IllegalStateException(parseErrorMessage(responseText, "登录已失效，请重新登录"))
                }

                else -> throw IllegalStateException(parseErrorMessage(responseText, "请求失败：$statusCode"))
            }
        } catch (error: IOException) {
            throw IllegalStateException(connectivityMessage(), error)
        } finally {
            connection.disconnect()
        }
    }

    private fun consumeSessionPayload(payload: ApiSessionPayload): SessionState {
        if (!payload.mobileEntryAllowed) {
            clearSession()
            throw IllegalStateException(payload.message ?: "该账号无移动端入口")
        }
        val mappedRoles = payload.availableRoles.mapNotNull(::mapRole)
        sessionState = SessionState(
            authToken = payload.token,
            currentUserId = payload.user.userId,
            username = payload.user.username,
            displayName = payload.user.name,
            availableRoles = mappedRoles,
            activeRole = mapRole(payload.activeRole),
            selectedChildId = payload.selectedChildId,
            isAuthenticated = true
        )
        persistSessionState()
        return sessionState
    }

    private fun mapRole(raw: String?): UserRole? {
        return UserRole.entries.firstOrNull { it.storageValue == raw }
    }

    private fun requireToken(): String {
        return sessionState.authToken ?: throw IllegalStateException("请先登录")
    }

    private fun buildUrl(path: String): String {
        return "${appConfig.backendBaseUrl.trimEnd('/')}$path"
    }

    private fun connectivityMessage(): String {
        return "无法连接本地服务，请确认后端已启动：${appConfig.backendBaseUrl}"
    }

    private fun readResponseText(connection: HttpURLConnection, statusCode: Int): String {
        val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
    }

    private fun parseErrorMessage(responseText: String, fallback: String): String {
        if (responseText.isBlank()) {
            return fallback
        }
        return runCatching {
            gson.fromJson(responseText, ApiErrorPayload::class.java).message
        }.getOrNull()?.ifBlank { fallback } ?: fallback
    }

    private fun loadSessionState(): SessionState {
        val file = sessionFile()
        if (!file.exists()) {
            return SessionState()
        }
        return runCatching {
            gson.fromJson(file.readText(Charsets.UTF_8), SessionState::class.java)
        }.getOrNull() ?: SessionState()
    }

    private fun persistSessionState() {
        val file = sessionFile()
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(sessionState), Charsets.UTF_8)
    }

    private fun clearSession() {
        sessionState = SessionState()
        persistSessionState()
    }

    private fun sessionFile(): File {
        return File(appContext.filesDir, AppConstants.SESSION_STATE_FILE)
    }

    private fun <T> readAssetObject(path: String, clazz: Class<T>): T {
        val json = appContext.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return gson.fromJson(json, clazz)
    }

    private fun ensureInitialized() {
        check(initialized) { "DataRepository must be initialized before use." }
    }
}

private data class ApiErrorPayload(
    val message: String
)

private data class ApiSessionPayload(
    val token: String,
    val user: ApiUserPayload,
    val availableRoles: List<String>,
    val activeRole: String? = null,
    val selectedChildId: String? = null,
    val mobileEntryAllowed: Boolean,
    val message: String? = null
)

private data class ApiUserPayload(
    val userId: String,
    val username: String,
    val name: String
)

private data class LoginRequestBody(
    val account: String,
    val password: String
)

private data class RegisterRequestBody(
    val username: String,
    val name: String,
    val email: String? = null,
    val password: String,
    val roles: List<String>
)

private data class ActiveRoleRequestBody(
    val role: String
)

private data class SelectedChildRequestBody(
    val childId: String
)

private data class ArchiveCheckInRequestBody(
    val childId: String,
    val itemId: String,
    val note: String,
    val stars: Int,
    val completed: Boolean
)

private fun OutputStream.writeMultipartText(boundary: String, name: String, value: String) {
    write("--$boundary\r\n".toByteArray(Charsets.UTF_8))
    write("Content-Disposition: form-data; name=\"${escapeMultipartHeader(name)}\"\r\n\r\n".toByteArray(Charsets.UTF_8))
    write(value.toByteArray(Charsets.UTF_8))
    write("\r\n".toByteArray(Charsets.UTF_8))
}

private fun OutputStream.writeMultipartFile(
    boundary: String,
    name: String,
    fileName: String,
    mimeType: String,
    bytes: ByteArray
) {
    write("--$boundary\r\n".toByteArray(Charsets.UTF_8))
    write(
        "Content-Disposition: form-data; name=\"${escapeMultipartHeader(name)}\"; filename=\"${escapeMultipartHeader(fileName)}\"\r\n"
            .toByteArray(Charsets.UTF_8)
    )
    write("Content-Type: $mimeType\r\n\r\n".toByteArray(Charsets.UTF_8))
    write(bytes)
    write("\r\n".toByteArray(Charsets.UTF_8))
}

private fun escapeMultipartHeader(value: String): String {
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "_").replace("\n", "_")
}
