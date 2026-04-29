package com.example.xinggui.backend

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import javax.sql.DataSource

class XingGuiService(
    private val dataSource: DataSource,
    private val gson: Gson,
    private val passwordHasher: PasswordHasher,
    private val uploadDir: Path,
    private val runMode: String = "local-demo",
    private val adminPassword: String = "1"
) {
    private val mobileRoles = setOf(BackendRole.PARENT.name, BackendRole.TEACHER.name)
    private val sessionTtlMillis = 7L * 24 * 60 * 60 * 1000
    private val captchaTtlMillis = 5L * 60 * 1000
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val defaultSearchHistory = listOf("IEP", "情绪支持", "融合教育")
    private val maxIepUploadBytes = 20 * 1024 * 1024
    private val allowedIepMimeTypes = setOf(
        "application/pdf",
        "image/png",
        "image/jpeg",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    suspend fun health(): HealthResponse = withContext(Dispatchers.IO) {
        var seedAccounts = demoAccountNames().associateWith { "UNKNOWN" }
        val databaseStatus = runCatching {
            dataSource.connection.use { conn ->
                val valid = conn.isValid(2)
                if (valid) {
                    seedAccounts = demoAccountStatuses(conn)
                }
                valid
            }
        }.getOrDefault(false)
        val uploadWritable = runCatching {
            Files.createDirectories(uploadDir)
            Files.isDirectory(uploadDir) && Files.isWritable(uploadDir)
        }.getOrDefault(false)
        HealthResponse(
            status = if (databaseStatus && uploadWritable) "UP" else "DEGRADED",
            app = "xinggui-backend",
            database = if (databaseStatus) "UP" else "DOWN",
            uploadDirWritable = uploadWritable,
            seedAccounts = seedAccounts,
            mode = runMode,
            time = now()
        )
    }

    suspend fun metrics(): BasicMetricsResponse = db { conn ->
        val poolMetrics = (dataSource as? HikariDataSource)?.hikariPoolMXBean?.let { bean ->
            PoolMetrics(
                activeConnections = bean.activeConnections,
                idleConnections = bean.idleConnections,
                totalConnections = bean.totalConnections,
                threadsAwaitingConnection = bean.threadsAwaitingConnection
            )
        }
        BasicMetrics.snapshot(
            pool = poolMetrics,
            auditLogCount = countRows(conn, "audit_logs"),
            pendingEventCount = countRowsWhere(conn, "event_outbox", "processed_at IS NULL")
        )
    }

    suspend fun createCaptcha(clientIp: String?, deviceId: String?): CaptchaResponse = tx { conn ->
        val left = (10..49).random()
        val right = (1..19).random()
        val answer = (left + right).toString()
        val captchaId = uuid()
        val expiresAt = now() + captchaTtlMillis
        conn.prepareStatement(
            """
            INSERT INTO captchas(captcha_id, answer_hash, question, ip_address, device_id, expires_at, used, created_at)
            VALUES (?, ?, ?, ?, ?, ?, FALSE, ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, captchaId)
            stmt.setString(2, passwordHasher.hash(answer))
            stmt.setString(3, "$left + $right = ?")
            stmt.setNullableString(4, safeKey(clientIp))
            stmt.setNullableString(5, safeKey(deviceId))
            stmt.setLong(6, expiresAt)
            stmt.setLong(7, now())
            stmt.executeUpdate()
        }
        CaptchaResponse(captchaId = captchaId, question = "$left + $right = ?", expiresAt = expiresAt)
    }

    suspend fun seedIfNeeded(loader: JsonSeedLoader) {
        tx { conn ->
            val bundle = loader.loadBundle()
            ensureDemoSeedData(conn, bundle)

            if (countRows(conn, "resources") == 0L) {
                bundle.resources.forEach { insertResource(conn, it) }
            }
        }
    }

    private fun ensureDemoSeedData(conn: Connection, bundle: SeedBundle) {
        ensureDemoAdmin(conn)

        bundle.users.forEach { user ->
            val username = user.username ?: user.userId
            val existing = findUserByPublicId(conn, user.userId) ?: findUserByUsername(conn, username)
            val userDbId = existing?.dbId ?: insertUser(
                conn = conn,
                userId = user.userId,
                username = username,
                displayName = user.displayName ?: user.name,
                email = user.email,
                avatarKey = user.avatarKey,
                passwordHash = passwordHasher.hash(user.userId),
                createdAt = now()
            )
            user.normalizedRoles().forEach { role -> attachRole(conn, userDbId, role) }
            existing?.let { ensureDemoPassword(conn, it, expectedPassword = user.userId) }
        }

        val childIds = bundle.children.associate { child ->
            child.childId to (findChildDbIdByPublicId(conn, child.childId) ?: insertChild(conn, child))
        }
        bundle.children.forEach { child ->
            val childDbId = childIds[child.childId] ?: return@forEach
            child.guardianIds.forEach { userPublicId ->
                findUserByPublicId(conn, userPublicId)?.let { connectChildGuardian(conn, childDbId, it.dbId) }
            }
            child.assignedTeacherIds.forEach { userPublicId ->
                findUserByPublicId(conn, userPublicId)?.let { connectChildTeacher(conn, childDbId, it.dbId) }
            }
        }

        bundle.goals.forEach { goal ->
            val childDbId = childIds[goal.childId] ?: return@forEach
            if (!goalPlanExists(conn, childDbId)) {
                insertGoalPlan(conn, childDbId, goal)
            }
        }
        bundle.reports.forEach { report ->
            val childDbId = childIds[report.childId] ?: return@forEach
            if (!reportSummaryExists(conn, childDbId)) {
                insertReportSummary(conn, childDbId, report, now())
            }
        }
    }

    private fun ensureDemoAdmin(conn: Connection) {
        val existing = findUserByUsername(conn, "1") ?: findUserByPublicId(conn, "admin001")
        val adminDbId = existing?.dbId ?: insertUser(
            conn = conn,
            userId = "admin001",
            username = "1",
            displayName = "系统管理员",
            email = "admin@xinggui.local",
            avatarKey = "user_sun",
            passwordHash = passwordHasher.hash(adminPassword),
            createdAt = now()
        )
        attachRole(conn, adminDbId, BackendRole.ADMIN.name)
        existing?.let { ensureDemoPassword(conn, it, expectedPassword = adminPassword) }
    }

    private fun ensureDemoPassword(conn: Connection, user: UserRecord, expectedPassword: String) {
        val canVerifyExpected = passwordHasher.verify(expectedPassword, user.passwordHash)
        val shouldResetForLocalDemo = !canVerifyExpected && isLocalDemoMode()
        if (canVerifyExpected && !passwordHasher.needsRehash(user.passwordHash) && !shouldResetForLocalDemo) {
            return
        }
        if (!canVerifyExpected && !shouldResetForLocalDemo) {
            return
        }
        conn.prepareStatement(
            "UPDATE users SET password_hash = ?, failed_login_count = 0, locked_until = NULL WHERE id = ?"
        ).use { stmt ->
            stmt.setString(1, passwordHasher.hash(expectedPassword))
            stmt.setLong(2, user.dbId)
            stmt.executeUpdate()
        }
    }

    suspend fun register(request: RegisterRequest, clientIp: String? = null): SessionResponse = tx { conn ->
        val currentTime = now()
        val username = cleanText(request.username, 48, "用户名")
        val displayName = cleanText(request.name, 64, "显示名")
        val email = request.email?.trim()?.takeIf { it.isNotEmpty() }?.also(::validateEmail)
        val deviceId = safeKey(request.deviceId) ?: "unknown"
        validateUsername(username)
        validatePasswordStrength(request.password)
        enforceRegistrationRisk(conn, clientIp, deviceId, currentTime)
        verifyCaptchaIfPresent(conn, request, clientIp, deviceId, currentTime)

        if (findUserByUsername(conn, username) != null) {
            throw ApiException(HttpStatusCode.Conflict, "用户名已存在")
        }
        if (email != null && findUserByEmail(conn, email) != null) {
            throw ApiException(HttpStatusCode.Conflict, "邮箱已被占用")
        }

        val roles = request.roles.orEmpty().ifEmpty { listOf(BackendRole.PARENT.name) }
            .map(::normalizeMobileRole)
            .distinct()
        val userDbId = insertUser(
            conn = conn,
            userId = "user_${uuid().take(12)}",
            username = username,
            displayName = displayName,
            email = email,
            avatarKey = null,
            passwordHash = passwordHasher.hash(request.password),
            createdAt = currentTime
        )
        roles.forEach { role -> attachRole(conn, userDbId, role) }
        val token = createSession(conn, userDbId, roles.firstOrNull())
        audit(conn, userDbId, "REGISTER", username, clientIp, true, "roles=${roles.joinToString(",")}")
        enqueueEvent(conn, "USER_REGISTERED", mapOf("userId" to username, "roles" to roles))
        buildSessionResponse(conn, token)
    }

    suspend fun login(request: LoginRequest, clientIp: String? = null): SessionResponse = tx { conn ->
        val account = request.account.trim()
        if (account.isBlank() || request.password.isBlank()) {
            throw ApiException(HttpStatusCode.BadRequest, "请输入账号和密码")
        }
        val currentTime = now()
        val deviceId = safeKey(request.deviceId) ?: "unknown"
        enforceLoginRateLimit(conn, clientIp, deviceId, currentTime)
        val user = findUserByAccount(conn, account)
        if (user == null) {
            recordLoginFailure(conn, null, account, clientIp, deviceId, "unknown-account")
            throw ApiException(HttpStatusCode.Unauthorized, "账号或密码错误")
        }
        if (user.lockedUntil != null && user.lockedUntil > currentTime) {
            BasicMetrics.recordRateLimitHit()
            BasicMetrics.recordLoginFailure()
            audit(conn, user.dbId, "LOGIN_LOCKED", account, clientIp, false, "lockedUntil=${user.lockedUntil}")
            throw ApiException(HttpStatusCode.TooManyRequests, "登录失败次数过多，请稍后再试")
        }
        if (!passwordHasher.verify(request.password, user.passwordHash)) {
            val failedCount = user.failedLoginCount + 1
            val lockedUntil = if (failedCount >= 5) currentTime + 10 * 60 * 1000 else null
            conn.prepareStatement("UPDATE users SET failed_login_count = ?, locked_until = ? WHERE id = ?").use { stmt ->
                stmt.setInt(1, failedCount)
                if (lockedUntil == null) stmt.setNull(2, Types.BIGINT) else stmt.setLong(2, lockedUntil)
                stmt.setLong(3, user.dbId)
                stmt.executeUpdate()
            }
            recordLoginFailure(conn, user.dbId, account, clientIp, deviceId, "failedCount=$failedCount")
            throw ApiException(HttpStatusCode.Unauthorized, "账号或密码错误")
        }
        conn.prepareStatement("UPDATE users SET failed_login_count = 0, locked_until = NULL, password_hash = ? WHERE id = ?").use { stmt ->
            stmt.setString(
                1,
                if (passwordHasher.needsRehash(user.passwordHash)) passwordHasher.hash(request.password) else user.passwordHash
            )
            stmt.setLong(2, user.dbId)
            stmt.executeUpdate()
        }
        clearLoginRisk(conn, clientIp, deviceId)
        val roles = queryRoles(conn, user.dbId).filter { it in mobileRoles }
        val token = createSession(conn, user.dbId, roles.firstOrNull())
        audit(conn, user.dbId, "LOGIN_SUCCESS", account, clientIp, true, null)
        buildSessionResponse(conn, token)
    }

    suspend fun logout(token: String) {
        tx { conn ->
            val session = loadSession(conn, token)
            conn.prepareStatement("UPDATE sessions SET revoked_at = ? WHERE auth_token = ?").use { stmt ->
                stmt.setLong(1, now())
                stmt.setString(2, token)
                stmt.executeUpdate()
            }
            audit(conn, session.userDbId, "LOGOUT", session.username, null, true, null)
        }
    }

    suspend fun logoutAll(token: String) {
        tx { conn ->
            val session = loadSession(conn, token)
            val currentTime = now()
            conn.prepareStatement("UPDATE sessions SET revoked_at = ? WHERE user_id = ? AND revoked_at IS NULL").use { stmt ->
                stmt.setLong(1, currentTime)
                stmt.setLong(2, session.userDbId)
                stmt.executeUpdate()
            }
            audit(conn, session.userDbId, "LOGOUT_ALL", session.username, null, true, null)
        }
    }

    suspend fun me(token: String): SessionResponse = db { conn ->
        buildSessionResponse(conn, token)
    }

    suspend fun getSessionRoles(token: String): RolesResponse = db { conn ->
        val session = loadSession(conn, token)
        RolesResponse(
            roles = session.availableRoles,
            activeRole = session.activeRole,
            selectedChildId = session.selectedChildId
        )
    }

    suspend fun updateActiveRole(token: String, request: ActiveRoleRequest): SessionResponse = tx { conn ->
        val session = loadSession(conn, token)
        val role = normalizeMobileRole(request.role)
        if (role !in session.roles) {
            throw ApiException(HttpStatusCode.Forbidden, "账号未授予该角色")
        }
        val selectedChildDbId = resolveSelectedChildForRole(conn, session.userDbId, role, session.selectedChildDbId)
        conn.prepareStatement("UPDATE sessions SET active_role = ?, selected_child_id = ?, last_seen_at = ? WHERE auth_token = ?")
            .use { stmt ->
                stmt.setString(1, role)
                if (selectedChildDbId == null) stmt.setNull(2, Types.BIGINT) else stmt.setLong(2, selectedChildDbId)
                stmt.setLong(3, now())
                stmt.setString(4, token)
                stmt.executeUpdate()
            }
        audit(conn, session.userDbId, "ROLE_SWITCH", role, null, true, null)
        buildSessionResponse(conn, token)
    }

    suspend fun updateSelectedChild(token: String, request: SelectedChildRequest): SessionResponse = tx { conn ->
        val session = loadSession(conn, token)
        val activeRole = session.activeRole ?: throw ApiException(HttpStatusCode.BadRequest, "请先选择角色")
        val childDbId = findAccessibleChildDbId(conn, session.userDbId, activeRole, request.childId)
            ?: throw ApiException(HttpStatusCode.Forbidden, "无权访问该儿童档案")
        conn.prepareStatement("UPDATE sessions SET selected_child_id = ?, last_seen_at = ? WHERE auth_token = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setLong(2, now())
            stmt.setString(3, token)
            stmt.executeUpdate()
        }
        audit(conn, session.userDbId, "CHILD_SWITCH", request.childId, null, true, null)
        buildSessionResponse(conn, token)
    }

    suspend fun getCurrentUser(token: String): PublicUser = db { conn ->
        val session = loadSession(conn, token)
        loadPublicUser(conn, session.userDbId)
    }

    suspend fun updateCurrentUserProfile(token: String, request: UserProfileUpdateRequest): SessionResponse = tx { conn ->
        val session = loadSession(conn, token)
        val displayName = cleanText(request.displayName, 64, "显示名")
        val email = request.email?.trim()?.takeIf { it.isNotEmpty() }?.also(::validateEmail)
        val avatarKey = request.avatarKey?.trim()?.takeIf { it.isNotEmpty() }?.take(64)
        if (email != null) {
            val existing = findUserByEmail(conn, email)
            if (existing != null && existing.dbId != session.userDbId) {
                throw ApiException(HttpStatusCode.Conflict, "邮箱已被占用")
            }
        }
        conn.prepareStatement("UPDATE users SET display_name = ?, email = ?, avatar_key = ? WHERE id = ?").use { stmt ->
            stmt.setString(1, displayName)
            stmt.setNullableString(2, email)
            stmt.setNullableString(3, avatarKey)
            stmt.setLong(4, session.userDbId)
            stmt.executeUpdate()
        }
        audit(conn, session.userDbId, "PROFILE_UPDATE", session.username, null, true, null)
        buildSessionResponse(conn, token)
    }

    suspend fun getUsers(token: String): List<PublicUser> = db { conn ->
        val session = loadSession(conn, token)
        requireAdmin(session)
        conn.prepareStatement("SELECT id FROM users ORDER BY created_at, id").use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(loadPublicUser(conn, rs.getLong("id")))
                    }
                }
            }
        }
    }

    suspend fun getUserRoles(token: String, userId: String): RolesResponse = db { conn ->
        val session = loadSession(conn, token)
        requireAdmin(session)
        val user = findUserByPublicId(conn, userId)
            ?: throw ApiException(HttpStatusCode.NotFound, "用户不存在")
        RolesResponse(queryRoles(conn, user.dbId))
    }

    suspend fun assignRole(token: String, userId: String, request: RoleMutationRequest): RolesResponse = tx { conn ->
        val session = loadSession(conn, token)
        requireAdmin(session)
        val user = findUserByPublicId(conn, userId)
            ?: throw ApiException(HttpStatusCode.NotFound, "用户不存在")
        val role = BackendRole.parse(request.role)?.name
            ?: throw ApiException(HttpStatusCode.BadRequest, "角色不存在")
        attachRole(conn, user.dbId, role)
        audit(conn, session.userDbId, "ROLE_ASSIGN", "$userId:$role", null, true, null)
        RolesResponse(queryRoles(conn, user.dbId))
    }

    suspend fun removeRole(token: String, userId: String, roleName: String): RolesResponse = tx { conn ->
        val session = loadSession(conn, token)
        requireAdmin(session)
        val user = findUserByPublicId(conn, userId)
            ?: throw ApiException(HttpStatusCode.NotFound, "用户不存在")
        val role = BackendRole.parse(roleName)?.name
            ?: throw ApiException(HttpStatusCode.BadRequest, "角色不存在")
        conn.prepareStatement("DELETE FROM user_roles WHERE user_id = ? AND role_name = ?").use { stmt ->
            stmt.setLong(1, user.dbId)
            stmt.setString(2, role)
            stmt.executeUpdate()
        }
        audit(conn, session.userDbId, "ROLE_REMOVE", "$userId:$role", null, true, null)
        RolesResponse(queryRoles(conn, user.dbId))
    }

    suspend fun listChildren(token: String): List<PublicChildProfile> = db { conn ->
        val session = loadSession(conn, token)
        val activeRole = session.activeRole ?: throw ApiException(HttpStatusCode.BadRequest, "请先选择角色")
        listAccessibleChildren(conn, session.userDbId, activeRole)
    }

    suspend fun getChild(token: String, childId: String): PublicChildProfile = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        audit(conn, session.userDbId, "CHILD_READ", childId, null, true, null)
        loadChildByDbId(conn, childDbId)
    }

    suspend fun updateChildProfile(
        token: String,
        childId: String,
        request: ChildProfileUpdateRequest
    ): PublicChildProfile = tx { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        val name = cleanText(request.name, 64, "儿童姓名")
        val birthDate = request.birthDate?.takeIf { it.isNotBlank() }?.let { parseDate(it, "出生日期") }
        val interventionStartDate = request.interventionStartDate?.takeIf { it.isNotBlank() }
            ?.let { parseDate(it, "干预开始日期") }
        val age = birthDate?.let { calculateAge(it) } ?: loadChildByDbId(conn, childDbId).age
        val duration = interventionStartDate?.let { calculateInterventionDuration(it) }
            ?: loadChildByDbId(conn, childDbId).interventionDuration
        val avatarKey = request.avatarKey?.trim()?.takeIf { it.isNotEmpty() }?.take(64)
        conn.prepareStatement(
            """
            UPDATE children
            SET name = ?, age = ?, intervention_duration = ?, birth_date = ?, intervention_start_date = ?, avatar_key = ?
            WHERE id = ?
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, name)
            stmt.setInt(2, age)
            stmt.setString(3, duration)
            if (birthDate == null) stmt.setNull(4, Types.DATE) else stmt.setDate(4, Date.valueOf(birthDate))
            if (interventionStartDate == null) stmt.setNull(5, Types.DATE) else stmt.setDate(5, Date.valueOf(interventionStartDate))
            stmt.setNullableString(6, avatarKey)
            stmt.setLong(7, childDbId)
            stmt.executeUpdate()
        }
        audit(conn, session.userDbId, "CHILD_PROFILE_UPDATE", childId, null, true, null)
        loadChildByDbId(conn, childDbId)
    }

    suspend fun getGoalPlan(token: String, childId: String): PublicGoalPlan = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        loadGoalPlanByChildDbId(conn, childDbId)
            ?: throw ApiException(HttpStatusCode.NotFound, "目标计划不存在")
    }

    suspend fun getLatestIepDocument(token: String, childId: String): PublicIepDocument = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        loadLatestIepDocument(conn, childDbId)
            ?: throw ApiException(HttpStatusCode.NotFound, "暂未上传 IEP 文档")
    }

    suspend fun uploadIepDocument(
        token: String,
        childId: String,
        request: IepUploadRequest
    ): PublicIepUploadResult = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.autoCommit = false
            var storedPath: Path? = null
            var failureActorUserDbId: Long? = null
            try {
                val session = loadSession(conn, token)
                failureActorUserDbId = session.userDbId
                val childDbId = requireAccessibleChild(conn, session, childId)
                val normalized = normalizeIepUploadRequest(request)
                val documentId = "doc_${uuid().take(20)}"
                val file = normalizeIepFile(normalized.file)
                audit(
                    conn,
                    session.userDbId,
                    "IEP_UPLOAD_VALIDATED",
                    childId,
                    null,
                    true,
                    "name=${file.originalFileName.take(80)},size=${file.bytes.size},type=${file.contentType}"
                )
                val savedPath = saveIepFile(documentId, file.contentType ?: "application/octet-stream", file.bytes)
                storedPath = savedPath
                audit(conn, session.userDbId, "IEP_UPLOAD_STORED", childId, null, true, savedPath.fileName.toString())
                val storedFileName = savedPath.fileName.toString()
                val currentTime = now()
                val safetyStatus = "PASSED_BY_RULES"
                conn.prepareStatement(
                    """
                    INSERT INTO iep_documents(
                        document_id, child_id, uploaded_by, original_file_name, stored_file_name, stored_file_path,
                        content_type, file_size_bytes, semester_goal, monthly_goal, weekly_goals_json, notes,
                        safety_status, uploaded_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { stmt ->
                    stmt.setString(1, documentId)
                    stmt.setLong(2, childDbId)
                    stmt.setLong(3, session.userDbId)
                    stmt.setString(4, file.originalFileName)
                    stmt.setString(5, storedFileName)
                    stmt.setString(6, savedPath.toString())
                    stmt.setString(7, file.contentType)
                    stmt.setLong(8, file.bytes.size.toLong())
                    stmt.setString(9, normalized.semesterGoal)
                    stmt.setString(10, normalized.monthlyGoal)
                    stmt.setString(11, gson.toJson(normalized.weeklyGoals))
                    stmt.setNullableString(12, normalized.notes)
                    stmt.setString(13, safetyStatus)
                    stmt.setLong(14, currentTime)
                    stmt.setLong(15, currentTime)
                    stmt.executeUpdate()
                }
                upsertGoalPlanFromIep(
                    conn = conn,
                    childDbId = childDbId,
                    documentId = documentId,
                    semesterGoal = normalized.semesterGoal,
                    monthlyGoal = normalized.monthlyGoal,
                    weeklyGoals = normalized.weeklyGoals
                )
                audit(conn, session.userDbId, "IEP_UPLOAD", childId, null, true, "size=${file.bytes.size}")
                enqueueEvent(conn, "IEP_UPLOADED", mapOf("childId" to childId, "documentId" to documentId))
                conn.commit()
                BasicMetrics.recordUpload()
                val document = loadLatestIepDocument(conn, childDbId)
                    ?: throw ApiException(HttpStatusCode.InternalServerError, "IEP 文档保存后读取失败")
                PublicIepUploadResult(document = document, goalPlan = loadGoalPlanByChildDbId(conn, childDbId)!!)
            } catch (error: Throwable) {
                conn.rollback()
                storedPath?.let { Files.deleteIfExists(it) }
                auditIepUploadFailure(failureActorUserDbId, childId, error)
                throw error
            } finally {
                conn.autoCommit = true
            }
        }
    }

    suspend fun getWeeklyCounts(token: String, childId: String): Map<String, Int> = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        val (start, end) = currentWeekWindow()
        conn.prepareStatement(
            """
            SELECT dimension_id, COUNT(*) AS total
            FROM archive_checkin_records
            WHERE child_id = ? AND completed = TRUE AND timestamp >= ? AND timestamp < ?
            GROUP BY dimension_id
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setLong(2, start)
            stmt.setLong(3, end)
            stmt.executeQuery().use { rs ->
                buildMap {
                    while (rs.next()) {
                        put(rs.getString("dimension_id"), rs.getInt("total"))
                    }
                }
            }
        }
    }

    suspend fun getReportSummary(token: String, childId: String): PublicReportSummary = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        loadReportSummaryByChildDbId(conn, childDbId)
            ?: throw ApiException(HttpStatusCode.NotFound, "报告不存在")
    }

    suspend fun getReportHistory(token: String, childId: String): List<PublicReportHistoryEntry> = db { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, childId)
        audit(conn, session.userDbId, "REPORT_HISTORY_READ", childId, null, true, null)
        loadReportHistoryByChildDbId(conn, childDbId)
    }

    suspend fun submitArchiveCheckIn(
        token: String,
        request: ArchiveCheckInRequest
    ): PublicCheckInResult = tx { conn ->
        val session = loadSession(conn, token)
        val childDbId = requireAccessibleChild(conn, session, request.childId)
        val note = cleanOptionalText(request.note, 500)
        val checkIn = loadWeeklyCheckIn(conn, childDbId, request.itemId)
            ?: throw ApiException(HttpStatusCode.NotFound, "打卡项目不存在")
        val completedCheckIn = checkIn.copy(completed = request.completed, rewardStars = request.stars.coerceIn(0, 5))
        conn.prepareStatement("UPDATE weekly_checkins SET completed = ?, reward_stars = ? WHERE child_id = ? AND item_id = ?")
            .use { stmt ->
                stmt.setBoolean(1, request.completed)
                stmt.setInt(2, completedCheckIn.rewardStars)
                stmt.setLong(3, childDbId)
                stmt.setString(4, request.itemId)
                stmt.executeUpdate()
            }
        val currentTime = now()
        conn.prepareStatement(
            """
            INSERT INTO archive_checkin_records(
                record_id, child_id, item_id, dimension_id, title, note, completed, reward_stars, timestamp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, "rec_${uuid().take(20)}")
            stmt.setLong(2, childDbId)
            stmt.setString(3, completedCheckIn.itemId)
            stmt.setString(4, completedCheckIn.dimensionId)
            stmt.setString(5, completedCheckIn.title)
            stmt.setString(6, note)
            stmt.setBoolean(7, request.completed)
            stmt.setInt(8, completedCheckIn.rewardStars)
            stmt.setLong(9, currentTime)
            stmt.executeUpdate()
        }

        val baseReport = loadReportSummaryByChildDbId(conn, childDbId)
            ?: defaultReport(childPublicId(conn, childDbId))
        val updatedReport = BackendReportRuleEngine.generateUpdatedReport(baseReport, completedCheckIn, note)
        upsertReportSummary(conn, childDbId, updatedReport)
        insertReportHistory(conn, childDbId, completedCheckIn, note, updatedReport, currentTime)
        audit(conn, session.userDbId, "ARCHIVE_CHECKIN", request.childId, null, true, "item=${request.itemId}")
        enqueueEvent(conn, "CHECKIN_SUBMITTED", mapOf("childId" to request.childId, "itemId" to request.itemId))
        PublicCheckInResult(
            success = true,
            earnedStars = if (request.completed) completedCheckIn.rewardStars else 0,
            updatedReport = updatedReport,
            message = "打卡已保存，报告已自动更新"
        )
    }

    suspend fun getResources(
        token: String,
        limit: Int = 100,
        offset: Int = 0,
        query: String? = null
    ): List<PublicResourceItem> = db { conn ->
        loadSession(conn, token)
        val boundedLimit = limit.coerceIn(1, 200)
        val boundedOffset = offset.coerceAtLeast(0)
        val keyword = query?.trim()?.takeIf { it.isNotEmpty() }
        val sql = buildString {
            append(
                """
                SELECT resource_id, title, category, is_paid, summary, recommended_reason, asset_path, source_url
                FROM resources
                """.trimIndent()
            )
            if (keyword != null) {
                append(" WHERE title LIKE ? OR category LIKE ? OR summary LIKE ?")
            }
            append(" ORDER BY category, resource_id LIMIT ? OFFSET ?")
        }
        conn.prepareStatement(sql).use { stmt ->
            var index = 1
            if (keyword != null) {
                val like = "%$keyword%"
                stmt.setString(index++, like)
                stmt.setString(index++, like)
                stmt.setString(index++, like)
            }
            stmt.setInt(index++, boundedLimit)
            stmt.setInt(index, boundedOffset)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            PublicResourceItem(
                                resourceId = rs.getString("resource_id"),
                                title = rs.getString("title"),
                                category = rs.getString("category"),
                                isPaid = rs.getBoolean("is_paid"),
                                summary = rs.getString("summary"),
                                recommendedReason = rs.getString("recommended_reason"),
                                assetPath = rs.getNullableString("asset_path"),
                                sourceUrl = rs.getNullableString("source_url")
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun getResourceRuntimeState(token: String): PublicResourceRuntimeState = db { conn ->
        val session = loadSession(conn, token)
        loadOrCreateRuntimeState(conn, session.userDbId)
    }

    suspend fun saveResourceRuntimeState(
        token: String,
        state: PublicResourceRuntimeState
    ): PublicResourceRuntimeState = tx { conn ->
        val session = loadSession(conn, token)
        val normalized = PublicResourceRuntimeState(
            unlockedResourceIds = state.unlockedResourceIds.map { it.trim() }
                .filter { it.isNotEmpty() }
                .take(200)
                .toSet(),
            searchHistory = state.searchHistory.map { cleanOptionalText(it, 40) }
                .filter { it.isNotBlank() }
                .distinct()
                .take(20)
        )
        conn.prepareStatement(
            """
            INSERT INTO resource_runtime_state(user_id, unlocked_resource_ids_json, search_history_json, updated_at)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                unlocked_resource_ids_json = VALUES(unlocked_resource_ids_json),
                search_history_json = VALUES(search_history_json),
                updated_at = VALUES(updated_at)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, session.userDbId)
            stmt.setString(2, gson.toJson(normalized.unlockedResourceIds))
            stmt.setString(3, gson.toJson(normalized.searchHistory))
            stmt.setLong(4, now())
            stmt.executeUpdate()
        }
        normalized
    }

    private suspend fun <T> db(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn -> block(conn) }
    }

    private suspend fun <T> tx(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.autoCommit = false
            try {
                val result = block(conn)
                conn.commit()
                result
            } catch (error: Throwable) {
                conn.rollback()
                throw error
            } finally {
                conn.autoCommit = true
            }
        }
    }

    private fun requireAccessibleChild(conn: Connection, session: SessionContext, childId: String): Long {
        val role = session.activeRole ?: throw ApiException(HttpStatusCode.BadRequest, "请先选择角色")
        return findAccessibleChildDbId(conn, session.userDbId, role, childId)
            ?: throw ApiException(HttpStatusCode.Forbidden, "无权访问该儿童档案")
    }

    private fun buildSessionResponse(conn: Connection, token: String): SessionResponse {
        val session = loadSession(conn, token)
        val publicUser = loadPublicUser(conn, session.userDbId)
        val mobileEntryAllowed = session.availableRoles.isNotEmpty()
        return SessionResponse(
            token = token,
            user = publicUser,
            availableRoles = session.availableRoles,
            activeRole = session.activeRole,
            selectedChildId = session.selectedChildId,
            mobileEntryAllowed = mobileEntryAllowed,
            message = if (mobileEntryAllowed) null else "该账号无移动端入口"
        )
    }

    private fun loadSession(conn: Connection, token: String): SessionContext {
        if (token.isBlank()) {
            throw ApiException(HttpStatusCode.Unauthorized, "登录已失效，请重新登录")
        }
        conn.prepareStatement(
            """
            SELECT s.auth_token, s.user_id AS user_db_id, s.active_role, s.selected_child_id,
                   s.expires_at, s.revoked_at,
                   u.user_id, u.username, u.display_name, u.email, u.avatar_key
            FROM sessions s
            JOIN users u ON u.id = s.user_id
            WHERE s.auth_token = ?
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, token)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) {
                    throw ApiException(HttpStatusCode.Unauthorized, "登录已失效，请重新登录")
                }
                val currentTime = now()
                val revokedAt = rs.getNullableLong("revoked_at")
                if (revokedAt != null || rs.getLong("expires_at") <= currentTime) {
                    throw ApiException(HttpStatusCode.Unauthorized, "登录已失效，请重新登录")
                }
                val userDbId = rs.getLong("user_db_id")
                val roles = queryRoles(conn, userDbId)
                val availableRoles = roles.filter { it in mobileRoles }
                val activeRole = rs.getNullableString("active_role")?.takeIf { it in availableRoles }
                    ?: availableRoles.firstOrNull()
                val selectedChildDbId = resolveSelectedChildForRole(
                    conn,
                    userDbId,
                    activeRole,
                    rs.getNullableLong("selected_child_id")
                )
                val selectedChildId = selectedChildDbId?.let { childPublicId(conn, it) }
                conn.prepareStatement(
                    "UPDATE sessions SET active_role = ?, selected_child_id = ?, last_seen_at = ?, expires_at = ? WHERE auth_token = ?"
                ).use { update ->
                    update.setNullableString(1, activeRole)
                    if (selectedChildDbId == null) update.setNull(2, Types.BIGINT) else update.setLong(2, selectedChildDbId)
                    update.setLong(3, currentTime)
                    update.setLong(4, currentTime + sessionTtlMillis)
                    update.setString(5, token)
                    update.executeUpdate()
                }
                return SessionContext(
                    token = token,
                    userDbId = userDbId,
                    userId = rs.getString("user_id"),
                    username = rs.getString("username"),
                    name = rs.getString("display_name"),
                    email = rs.getNullableString("email"),
                    avatarKey = rs.getNullableString("avatar_key"),
                    roles = roles,
                    availableRoles = availableRoles,
                    activeRole = activeRole,
                    selectedChildDbId = selectedChildDbId,
                    selectedChildId = selectedChildId
                )
            }
        }
    }

    private fun createSession(conn: Connection, userDbId: Long, preferredRole: String?): String {
        val roles = queryRoles(conn, userDbId).filter { it in mobileRoles }
        val activeRole = preferredRole?.takeIf { it in roles } ?: roles.firstOrNull()
        val selectedChildDbId = resolveSelectedChildForRole(conn, userDbId, activeRole, null)
        val token = "xg_${uuid()}${uuid()}"
        val currentTime = now()
        conn.prepareStatement(
            """
            INSERT INTO sessions(auth_token, user_id, active_role, selected_child_id, created_at, expires_at, last_seen_at, revoked_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NULL)
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, token)
            stmt.setLong(2, userDbId)
            stmt.setNullableString(3, activeRole)
            if (selectedChildDbId == null) stmt.setNull(4, Types.BIGINT) else stmt.setLong(4, selectedChildDbId)
            stmt.setLong(5, currentTime)
            stmt.setLong(6, currentTime + sessionTtlMillis)
            stmt.setLong(7, currentTime)
            stmt.executeUpdate()
        }
        return token
    }

    private fun resolveSelectedChildForRole(
        conn: Connection,
        userDbId: Long,
        role: String?,
        currentSelectedChildDbId: Long?
    ): Long? {
        if (role == null) return null
        if (currentSelectedChildDbId != null) {
            val childId = childPublicId(conn, currentSelectedChildDbId)
            if (findAccessibleChildDbId(conn, userDbId, role, childId) != null) {
                return currentSelectedChildDbId
            }
        }
        val children = listAccessibleChildren(conn, userDbId, role)
        return children.firstOrNull()?.let { findChildDbIdByPublicId(conn, it.childId) }
    }

    private fun loadPublicUser(conn: Connection, userDbId: Long): PublicUser {
        conn.prepareStatement(
            "SELECT user_id, username, display_name, email, avatar_key FROM users WHERE id = ?"
        ).use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) throw ApiException(HttpStatusCode.NotFound, "用户不存在")
                val name = rs.getString("display_name")
                return PublicUser(
                    userId = rs.getString("user_id"),
                    username = rs.getString("username"),
                    name = name,
                    displayName = name,
                    email = rs.getNullableString("email"),
                    avatarKey = rs.getNullableString("avatar_key"),
                    roles = queryRoles(conn, userDbId),
                    childIds = queryUserChildIds(conn, userDbId)
                )
            }
        }
    }

    private fun listAccessibleChildren(
        conn: Connection,
        userDbId: Long,
        role: String
    ): List<PublicChildProfile> {
        val sql = when (role) {
            BackendRole.PARENT.name -> """
                SELECT c.id FROM children c
                JOIN child_guardians cg ON cg.child_id = c.id
                WHERE cg.user_id = ?
                ORDER BY c.id
            """.trimIndent()

            BackendRole.TEACHER.name -> """
                SELECT c.id FROM children c
                JOIN child_teachers ct ON ct.child_id = c.id
                WHERE ct.user_id = ?
                ORDER BY c.id
            """.trimIndent()

            BackendRole.ADMIN.name -> "SELECT id FROM children ORDER BY id"
            else -> throw ApiException(HttpStatusCode.BadRequest, "角色不存在")
        }
        conn.prepareStatement(sql).use { stmt ->
            if (role != BackendRole.ADMIN.name) {
                stmt.setLong(1, userDbId)
            }
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) {
                        add(loadChildByDbId(conn, rs.getLong("id")))
                    }
                }
            }
        }
    }

    private fun loadChildByDbId(conn: Connection, childDbId: Long): PublicChildProfile {
        conn.prepareStatement(
            """
            SELECT child_id, name, age, intervention_duration, birth_date, intervention_start_date, avatar_key
            FROM children
            WHERE id = ?
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) throw ApiException(HttpStatusCode.NotFound, "儿童档案不存在")
                return PublicChildProfile(
                    childId = rs.getString("child_id"),
                    name = rs.getString("name"),
                    age = rs.getInt("age"),
                    interventionDuration = rs.getString("intervention_duration"),
                    birthDate = rs.getDate("birth_date")?.toLocalDate()?.toString(),
                    interventionStartDate = rs.getDate("intervention_start_date")?.toLocalDate()?.toString(),
                    avatarKey = rs.getNullableString("avatar_key"),
                    guardianIds = queryGuardianIds(conn, childDbId),
                    assignedTeacherIds = queryTeacherIds(conn, childDbId)
                )
            }
        }
    }

    private fun loadGoalPlanByChildDbId(conn: Connection, childDbId: Long): PublicGoalPlan? {
        conn.prepareStatement("SELECT semester_goal, monthly_goal FROM goal_plans WHERE child_id = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return PublicGoalPlan(
                    childId = childPublicId(conn, childDbId),
                    semesterGoal = rs.getString("semester_goal"),
                    monthlyGoal = rs.getString("monthly_goal"),
                    weeklyCheckIns = loadWeeklyCheckIns(conn, childDbId)
                )
            }
        }
    }

    private fun loadWeeklyCheckIns(conn: Connection, childDbId: Long): List<PublicWeeklyCheckIn> {
        conn.prepareStatement(
            """
            SELECT item_id, dimension_id, title, completed, reward_stars
            FROM weekly_checkins
            WHERE child_id = ?
            ORDER BY id
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) {
                        add(
                            PublicWeeklyCheckIn(
                                itemId = rs.getString("item_id"),
                                dimensionId = rs.getString("dimension_id"),
                                title = rs.getString("title"),
                                completed = rs.getBoolean("completed"),
                                rewardStars = rs.getInt("reward_stars")
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadWeeklyCheckIn(conn: Connection, childDbId: Long, itemId: String): PublicWeeklyCheckIn? {
        conn.prepareStatement(
            """
            SELECT item_id, dimension_id, title, completed, reward_stars
            FROM weekly_checkins
            WHERE child_id = ? AND item_id = ?
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setString(2, itemId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return PublicWeeklyCheckIn(
                    itemId = rs.getString("item_id"),
                    dimensionId = rs.getString("dimension_id"),
                    title = rs.getString("title"),
                    completed = rs.getBoolean("completed"),
                    rewardStars = rs.getInt("reward_stars")
                )
            }
        }
    }

    private fun loadLatestIepDocument(conn: Connection, childDbId: Long): PublicIepDocument? {
        conn.prepareStatement(
            """
            SELECT d.document_id, d.original_file_name, d.content_type, d.file_size_bytes,
                   d.semester_goal, d.monthly_goal, d.weekly_goals_json, d.notes, d.uploaded_at,
                   d.safety_status, u.user_id AS uploaded_by
            FROM iep_documents d
            JOIN users u ON u.id = d.uploaded_by
            WHERE d.child_id = ?
            ORDER BY d.uploaded_at DESC, d.id DESC
            LIMIT 1
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                val safety = rs.getNullableString("safety_status") ?: "PASSED_BY_RULES"
                return PublicIepDocument(
                    documentId = rs.getString("document_id"),
                    childId = childPublicId(conn, childDbId),
                    uploadedBy = rs.getString("uploaded_by"),
                    originalFileName = rs.getString("original_file_name"),
                    contentType = rs.getString("content_type"),
                    fileSizeBytes = rs.getLong("file_size_bytes"),
                    semesterGoal = rs.getString("semester_goal"),
                    monthlyGoal = rs.getString("monthly_goal"),
                    weeklyGoals = readIepWeeklyGoals(rs.getString("weekly_goals_json")),
                    notes = rs.getNullableString("notes"),
                    uploadedAt = rs.getLong("uploaded_at"),
                    safetyStatus = safety,
                    contentSafetyStatus = safety
                )
            }
        }
    }

    private fun normalizeIepUploadRequest(request: IepUploadRequest): IepUploadRequest {
        val semesterGoal = cleanText(request.semesterGoal, 1000, "学期目标")
        val monthlyGoal = cleanText(request.monthlyGoal, 1000, "月目标")
        val weeklyGoals = request.weeklyGoals.take(20).mapIndexed(::normalizeIepWeeklyGoal)
        if (weeklyGoals.isEmpty()) {
            throw ApiException(HttpStatusCode.BadRequest, "请至少填写一个周目标")
        }
        return request.copy(
            semesterGoal = semesterGoal,
            monthlyGoal = monthlyGoal,
            weeklyGoals = weeklyGoals,
            notes = request.notes?.let { cleanOptionalText(it, 1000) }
        )
    }

    private fun normalizeIepWeeklyGoal(index: Int, goal: IepWeeklyGoalInput): IepWeeklyGoalInput {
        val dimension = cleanOptionalText(goal.dimensionId, 40).ifBlank { "cognition" }
        val title = cleanText(goal.title, 120, "第 ${index + 1} 个周目标")
        return IepWeeklyGoalInput(
            dimensionId = dimension,
            title = title,
            rewardStars = goal.rewardStars.coerceIn(0, 5)
        )
    }

    private fun normalizeIepFile(file: UploadedIepFile): UploadedIepFile {
        if (file.bytes.isEmpty()) {
            throw ApiException(HttpStatusCode.BadRequest, "上传文件为空")
        }
        if (file.bytes.size > maxIepUploadBytes) {
            throw ApiException(HttpStatusCode.PayloadTooLarge, "IEP 文档最大支持 20MB")
        }
        val originalName = sanitizeOriginalFileName(file.originalFileName)
        val contentType = resolveIepContentType(originalName, file.contentType, file.bytes)
        return UploadedIepFile(
            originalFileName = originalName,
            contentType = contentType,
            bytes = file.bytes
        )
    }

    private fun sanitizeOriginalFileName(fileName: String): String {
        val sanitized = fileName.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[\\r\\n\\t]"), "_")
            .trim()
            .take(180)
        if (sanitized.isBlank() || "." !in sanitized) {
            throw ApiException(HttpStatusCode.BadRequest, "文件名需包含有效扩展名")
        }
        return sanitized
    }

    private fun resolveIepContentType(fileName: String, suppliedContentType: String?, bytes: ByteArray): String {
        val supplied = suppliedContentType?.substringBefore(';')?.trim()?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() && it != "application/octet-stream" }
        val magic = inferIepMimeType(bytes)
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val extensionMime = when (extension) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> null
        }
        val resolved = magic ?: extensionMime ?: supplied
            ?: throw ApiException(HttpStatusCode.BadRequest, "仅支持 PDF、图片和 Word 文档")
        if (extensionMime != null && extensionMime != resolved) {
            throw ApiException(HttpStatusCode.BadRequest, "文件扩展名与内容不一致")
        }
        if (supplied != null && supplied !in allowedIepMimeTypes) {
            throw ApiException(HttpStatusCode.BadRequest, "文件 MIME 类型不在白名单内")
        }
        if (supplied != null && supplied != resolved && supplied != "application/octet-stream") {
            throw ApiException(HttpStatusCode.BadRequest, "文件 MIME 类型与内容不一致")
        }
        return resolved
    }

    private fun inferIepMimeType(bytes: ByteArray): String? {
        if (bytes.size >= 4 && bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
            return "application/pdf"
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        ) {
            return "image/png"
        }
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
            return "image/jpeg"
        }
        if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0xD0.toByte() && bytes[1] == 0xCF.toByte() && bytes[2] == 0x11.toByte() && bytes[3] == 0xE0.toByte()
        ) {
            return "application/msword"
        }
        return null
    }

    private fun saveIepFile(documentId: String, contentType: String, bytes: ByteArray): Path {
        Files.createDirectories(uploadDir)
        val extension = extensionForMimeType(contentType)
        val storedPath = uploadDir.resolve("$documentId.$extension").normalize()
        if (!storedPath.startsWith(uploadDir.normalize())) {
            throw ApiException(HttpStatusCode.BadRequest, "文件路径非法")
        }
        Files.write(storedPath, bytes)
        return storedPath
    }

    private fun extensionForMimeType(contentType: String): String {
        return when (contentType) {
            "application/pdf" -> "pdf"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            else -> "bin"
        }
    }

    private fun upsertGoalPlanFromIep(
        conn: Connection,
        childDbId: Long,
        documentId: String,
        semesterGoal: String,
        monthlyGoal: String,
        weeklyGoals: List<IepWeeklyGoalInput>
    ) {
        conn.prepareStatement(
            """
            INSERT INTO goal_plans(child_id, semester_goal, monthly_goal)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE semester_goal = VALUES(semester_goal), monthly_goal = VALUES(monthly_goal)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setString(2, semesterGoal)
            stmt.setString(3, monthlyGoal)
            stmt.executeUpdate()
        }
        conn.prepareStatement("DELETE FROM weekly_checkins WHERE child_id = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeUpdate()
        }
        weeklyGoals.forEachIndexed { index, goal ->
            conn.prepareStatement(
                """
                INSERT INTO weekly_checkins(item_id, child_id, dimension_id, title, completed, reward_stars)
                VALUES (?, ?, ?, ?, FALSE, ?)
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, "${documentId}_w${index + 1}")
                stmt.setLong(2, childDbId)
                stmt.setString(3, goal.dimensionId)
                stmt.setString(4, goal.title)
                stmt.setInt(5, goal.rewardStars)
                stmt.executeUpdate()
            }
        }
    }

    private fun loadReportSummaryByChildDbId(conn: Connection, childDbId: Long): PublicReportSummary? {
        conn.prepareStatement(
            """
            SELECT overview, overall_evaluation, next_suggestions, ai_analysis, dimension_scores_json, dimension_highlights_json
            FROM report_summaries
            WHERE child_id = ?
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) return null
                return PublicReportSummary(
                    childId = childPublicId(conn, childDbId),
                    overview = rs.getString("overview"),
                    overallEvaluation = rs.getString("overall_evaluation"),
                    nextSuggestions = rs.getString("next_suggestions"),
                    aiAnalysis = rs.getString("ai_analysis"),
                    dimensionScores = readMap(rs.getString("dimension_scores_json")),
                    dimensionHighlights = readStringList(rs.getString("dimension_highlights_json"))
                )
            }
        }
    }

    private fun loadReportHistoryByChildDbId(conn: Connection, childDbId: Long): List<PublicReportHistoryEntry> {
        conn.prepareStatement(
            """
            SELECT entry_id, source_item_id, source_dimension_id, note, generated_at, dimension_scores_json,
                   overview, ai_analysis, overall_evaluation, next_suggestions
            FROM report_history_entries
            WHERE child_id = ?
            ORDER BY generated_at DESC, id DESC
            LIMIT 100
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) {
                        add(
                            PublicReportHistoryEntry(
                                entryId = rs.getString("entry_id"),
                                childId = childPublicId(conn, childDbId),
                                sourceItemId = rs.getString("source_item_id"),
                                sourceDimensionId = rs.getString("source_dimension_id"),
                                note = rs.getString("note"),
                                generatedAt = rs.getLong("generated_at"),
                                dimensionScores = readMap(rs.getString("dimension_scores_json")),
                                overview = rs.getString("overview"),
                                aiAnalysis = rs.getString("ai_analysis"),
                                overallEvaluation = rs.getString("overall_evaluation"),
                                nextSuggestions = rs.getString("next_suggestions")
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadOrCreateRuntimeState(conn: Connection, userDbId: Long): PublicResourceRuntimeState {
        conn.prepareStatement(
            "SELECT unlocked_resource_ids_json, search_history_json FROM resource_runtime_state WHERE user_id = ?"
        ).use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return PublicResourceRuntimeState(
                        unlockedResourceIds = readStringList(rs.getString("unlocked_resource_ids_json")).toSet(),
                        searchHistory = readStringList(rs.getString("search_history_json"))
                    )
                }
            }
        }
        val state = PublicResourceRuntimeState(searchHistory = defaultSearchHistory)
        conn.prepareStatement(
            "INSERT INTO resource_runtime_state(user_id, unlocked_resource_ids_json, search_history_json, updated_at) VALUES (?, ?, ?, ?)"
        ).use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.setString(2, gson.toJson(state.unlockedResourceIds))
            stmt.setString(3, gson.toJson(state.searchHistory))
            stmt.setLong(4, now())
            stmt.executeUpdate()
        }
        return state
    }

    private fun queryRoles(conn: Connection, userDbId: Long): List<String> {
        conn.prepareStatement("SELECT role_name FROM user_roles WHERE user_id = ? ORDER BY role_name").use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) add(rs.getString("role_name"))
                }
            }
        }
    }

    private fun queryUserChildIds(conn: Connection, userDbId: Long): List<String> {
        conn.prepareStatement(
            """
            SELECT DISTINCT c.child_id
            FROM children c
            LEFT JOIN child_guardians cg ON cg.child_id = c.id
            LEFT JOIN child_teachers ct ON ct.child_id = c.id
            WHERE cg.user_id = ? OR ct.user_id = ?
            ORDER BY c.child_id
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.setLong(2, userDbId)
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) add(rs.getString("child_id"))
                }
            }
        }
    }

    private fun queryGuardianIds(conn: Connection, childDbId: Long): List<String> {
        return queryChildUserIds(conn, childDbId, "child_guardians")
    }

    private fun queryTeacherIds(conn: Connection, childDbId: Long): List<String> {
        return queryChildUserIds(conn, childDbId, "child_teachers")
    }

    private fun queryChildUserIds(conn: Connection, childDbId: Long, table: String): List<String> {
        conn.prepareStatement(
            """
            SELECT u.user_id
            FROM $table rel
            JOIN users u ON u.id = rel.user_id
            WHERE rel.child_id = ?
            ORDER BY u.user_id
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                return buildList {
                    while (rs.next()) add(rs.getString("user_id"))
                }
            }
        }
    }

    private fun findAccessibleChildDbId(
        conn: Connection,
        userDbId: Long,
        role: String,
        childId: String
    ): Long? {
        val sql = when (role) {
            BackendRole.PARENT.name -> """
                SELECT c.id FROM children c
                JOIN child_guardians cg ON cg.child_id = c.id
                WHERE c.child_id = ? AND cg.user_id = ?
            """.trimIndent()

            BackendRole.TEACHER.name -> """
                SELECT c.id FROM children c
                JOIN child_teachers ct ON ct.child_id = c.id
                WHERE c.child_id = ? AND ct.user_id = ?
            """.trimIndent()

            BackendRole.ADMIN.name -> "SELECT id FROM children WHERE child_id = ?"
            else -> return null
        }
        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, childId)
            if (role != BackendRole.ADMIN.name) stmt.setLong(2, userDbId)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getLong("id") else null
            }
        }
    }

    private fun findChildDbIdByPublicId(conn: Connection, childId: String): Long? {
        conn.prepareStatement("SELECT id FROM children WHERE child_id = ?").use { stmt ->
            stmt.setString(1, childId)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getLong("id") else null
            }
        }
    }

    private fun childPublicId(conn: Connection, childDbId: Long): String {
        conn.prepareStatement("SELECT child_id FROM children WHERE id = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) throw ApiException(HttpStatusCode.NotFound, "儿童档案不存在")
                return rs.getString("child_id")
            }
        }
    }

    private fun requireAdmin(session: SessionContext) {
        if (BackendRole.ADMIN.name !in session.roles) {
            throw ApiException(HttpStatusCode.Forbidden, "需要管理员权限")
        }
    }

    private fun normalizeMobileRole(role: String): String {
        val parsed = BackendRole.parse(role)?.name
            ?: throw ApiException(HttpStatusCode.BadRequest, "角色不存在")
        if (parsed !in mobileRoles) {
            throw ApiException(HttpStatusCode.BadRequest, "移动端仅允许家长或教师角色")
        }
        return parsed
    }

    private fun countRows(conn: Connection, table: String): Long {
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) AS total FROM $table").use { rs ->
                rs.next()
                return rs.getLong("total")
            }
        }
    }

    private fun countRowsWhere(conn: Connection, table: String, where: String): Long {
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) AS total FROM $table WHERE $where").use { rs ->
                rs.next()
                return rs.getLong("total")
            }
        }
    }

    private fun goalPlanExists(conn: Connection, childDbId: Long): Boolean {
        conn.prepareStatement("SELECT 1 FROM goal_plans WHERE child_id = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs -> return rs.next() }
        }
    }

    private fun reportSummaryExists(conn: Connection, childDbId: Long): Boolean {
        conn.prepareStatement("SELECT 1 FROM report_summaries WHERE child_id = ?").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.executeQuery().use { rs -> return rs.next() }
        }
    }

    private fun insertUser(
        conn: Connection,
        userId: String,
        username: String,
        displayName: String,
        email: String?,
        avatarKey: String?,
        passwordHash: String,
        createdAt: Long
    ): Long {
        conn.prepareStatement(
            """
            INSERT INTO users(user_id, username, display_name, email, password_hash, created_at, avatar_key, failed_login_count, locked_until)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0, NULL)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setString(1, userId)
            stmt.setString(2, username)
            stmt.setString(3, displayName)
            stmt.setNullableString(4, email)
            stmt.setString(5, passwordHash)
            stmt.setLong(6, createdAt)
            stmt.setNullableString(7, avatarKey)
            stmt.executeUpdate()
            return generatedKey(stmt)
        }
    }

    private fun insertChild(conn: Connection, child: SeedChild): Long {
        conn.prepareStatement(
            """
            INSERT INTO children(child_id, name, age, intervention_duration, birth_date, intervention_start_date, avatar_key)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setString(1, child.childId)
            stmt.setString(2, child.name)
            stmt.setInt(3, child.age)
            stmt.setString(4, child.interventionDuration)
            child.birthDate?.let { parseDate(it, "出生日期") }?.let { stmt.setDate(5, Date.valueOf(it)) }
                ?: stmt.setNull(5, Types.DATE)
            child.interventionStartDate?.let { parseDate(it, "干预开始日期") }?.let { stmt.setDate(6, Date.valueOf(it)) }
                ?: stmt.setNull(6, Types.DATE)
            stmt.setNullableString(7, child.avatarKey)
            stmt.executeUpdate()
            return generatedKey(stmt)
        }
    }

    private fun connectChildGuardian(conn: Connection, childDbId: Long, userDbId: Long) {
        conn.prepareStatement("INSERT IGNORE INTO child_guardians(child_id, user_id) VALUES (?, ?)").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setLong(2, userDbId)
            stmt.executeUpdate()
        }
    }

    private fun connectChildTeacher(conn: Connection, childDbId: Long, userDbId: Long) {
        conn.prepareStatement("INSERT IGNORE INTO child_teachers(child_id, user_id) VALUES (?, ?)").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setLong(2, userDbId)
            stmt.executeUpdate()
        }
    }

    private fun insertGoalPlan(conn: Connection, childDbId: Long, goal: SeedGoalPlan) {
        conn.prepareStatement("INSERT INTO goal_plans(child_id, semester_goal, monthly_goal) VALUES (?, ?, ?)").use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setString(2, goal.semesterGoal)
            stmt.setString(3, goal.monthlyGoal)
            stmt.executeUpdate()
        }
        goal.weeklyCheckIns.forEach { checkIn ->
            conn.prepareStatement(
                """
                INSERT INTO weekly_checkins(item_id, child_id, dimension_id, title, completed, reward_stars)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ).use { stmt ->
                stmt.setString(1, checkIn.itemId)
                stmt.setLong(2, childDbId)
                stmt.setString(3, checkIn.dimensionId)
                stmt.setString(4, checkIn.title)
                stmt.setBoolean(5, checkIn.completed)
                stmt.setInt(6, checkIn.rewardStars)
                stmt.executeUpdate()
            }
        }
    }

    private fun insertReportSummary(conn: Connection, childDbId: Long, report: SeedReportSummary, updatedAt: Long) {
        conn.prepareStatement(
            """
            INSERT INTO report_summaries(
                child_id, overview, overall_evaluation, next_suggestions, ai_analysis,
                dimension_scores_json, dimension_highlights_json, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setString(2, report.overview)
            stmt.setString(3, report.overallEvaluation)
            stmt.setString(4, report.nextSuggestions)
            stmt.setString(5, report.aiAnalysis)
            stmt.setString(6, gson.toJson(report.dimensionScores))
            stmt.setString(7, gson.toJson(report.dimensionHighlights))
            stmt.setLong(8, updatedAt)
            stmt.executeUpdate()
        }
    }

    private fun insertResource(conn: Connection, resource: SeedResourceItem) {
        conn.prepareStatement(
            """
            INSERT INTO resources(resource_id, title, category, is_paid, summary, recommended_reason, asset_path, source_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, resource.resourceId)
            stmt.setString(2, resource.title)
            stmt.setString(3, resource.category)
            stmt.setBoolean(4, resource.isPaid)
            stmt.setString(5, resource.summary)
            stmt.setString(6, resource.recommendedReason)
            stmt.setNullableString(7, resource.assetPath)
            stmt.setNullableString(8, resource.sourceUrl)
            stmt.executeUpdate()
        }
    }

    private fun upsertReportSummary(conn: Connection, childDbId: Long, report: PublicReportSummary) {
        conn.prepareStatement(
            """
            INSERT INTO report_summaries(
                child_id, overview, overall_evaluation, next_suggestions, ai_analysis,
                dimension_scores_json, dimension_highlights_json, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                overview = VALUES(overview),
                overall_evaluation = VALUES(overall_evaluation),
                next_suggestions = VALUES(next_suggestions),
                ai_analysis = VALUES(ai_analysis),
                dimension_scores_json = VALUES(dimension_scores_json),
                dimension_highlights_json = VALUES(dimension_highlights_json),
                updated_at = VALUES(updated_at)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, childDbId)
            stmt.setString(2, report.overview)
            stmt.setString(3, report.overallEvaluation)
            stmt.setString(4, report.nextSuggestions)
            stmt.setString(5, report.aiAnalysis)
            stmt.setString(6, gson.toJson(report.dimensionScores))
            stmt.setString(7, gson.toJson(report.dimensionHighlights))
            stmt.setLong(8, now())
            stmt.executeUpdate()
        }
    }

    private fun insertReportHistory(
        conn: Connection,
        childDbId: Long,
        checkIn: PublicWeeklyCheckIn,
        note: String,
        report: PublicReportSummary,
        generatedAt: Long
    ) {
        conn.prepareStatement(
            """
            INSERT INTO report_history_entries(
                entry_id, child_id, source_item_id, source_dimension_id, note, generated_at,
                dimension_scores_json, overview, ai_analysis, overall_evaluation, next_suggestions
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, "hist_${uuid().take(20)}")
            stmt.setLong(2, childDbId)
            stmt.setString(3, checkIn.itemId)
            stmt.setString(4, checkIn.dimensionId)
            stmt.setString(5, note)
            stmt.setLong(6, generatedAt)
            stmt.setString(7, gson.toJson(report.dimensionScores))
            stmt.setString(8, report.overview)
            stmt.setString(9, report.aiAnalysis)
            stmt.setString(10, report.overallEvaluation)
            stmt.setString(11, report.nextSuggestions)
            stmt.executeUpdate()
        }
    }

    private fun attachRole(conn: Connection, userDbId: Long, role: String) {
        conn.prepareStatement("INSERT IGNORE INTO user_roles(user_id, role_name) VALUES (?, ?)").use { stmt ->
            stmt.setLong(1, userDbId)
            stmt.setString(2, role)
            stmt.executeUpdate()
        }
    }

    private fun findUserByAccount(conn: Connection, account: String): UserRecord? {
        conn.prepareStatement(
            """
            SELECT id, user_id, username, display_name, email, avatar_key, password_hash, failed_login_count, locked_until
            FROM users
            WHERE username = ? OR email = ?
            LIMIT 1
            """.trimIndent()
        ).use { stmt ->
            stmt.setString(1, account)
            stmt.setString(2, account)
            stmt.executeQuery().use { rs -> return if (rs.next()) userRecord(rs) else null }
        }
    }

    private fun findUserByUsername(conn: Connection, username: String): UserRecord? {
        conn.prepareStatement(
            "SELECT id, user_id, username, display_name, email, avatar_key, password_hash, failed_login_count, locked_until FROM users WHERE username = ?"
        ).use { stmt ->
            stmt.setString(1, username)
            stmt.executeQuery().use { rs -> return if (rs.next()) userRecord(rs) else null }
        }
    }

    private fun findUserByEmail(conn: Connection, email: String): UserRecord? {
        conn.prepareStatement(
            "SELECT id, user_id, username, display_name, email, avatar_key, password_hash, failed_login_count, locked_until FROM users WHERE email = ?"
        ).use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs -> return if (rs.next()) userRecord(rs) else null }
        }
    }

    private fun findUserByPublicId(conn: Connection, userId: String): UserRecord? {
        conn.prepareStatement(
            "SELECT id, user_id, username, display_name, email, avatar_key, password_hash, failed_login_count, locked_until FROM users WHERE user_id = ?"
        ).use { stmt ->
            stmt.setString(1, userId)
            stmt.executeQuery().use { rs -> return if (rs.next()) userRecord(rs) else null }
        }
    }

    private fun userRecord(rs: ResultSet): UserRecord {
        return UserRecord(
            dbId = rs.getLong("id"),
            userId = rs.getString("user_id"),
            username = rs.getString("username"),
            name = rs.getString("display_name"),
            email = rs.getNullableString("email"),
            avatarKey = rs.getNullableString("avatar_key"),
            passwordHash = rs.getString("password_hash"),
            failedLoginCount = rs.getInt("failed_login_count"),
            lockedUntil = rs.getNullableLong("locked_until")
        )
    }

    private fun generatedKey(stmt: PreparedStatement): Long {
        stmt.generatedKeys.use { rs ->
            if (!rs.next()) throw ApiException(HttpStatusCode.InternalServerError, "数据库未返回主键")
            return rs.getLong(1)
        }
    }

    private fun readStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun readMap(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson<Map<String, Int>>(json, type).orEmpty()
        }.getOrDefault(emptyMap())
    }

    private fun readIepWeeklyGoals(json: String?): List<IepWeeklyGoalInput> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<IepWeeklyGoalInput>>() {}.type
            gson.fromJson<List<IepWeeklyGoalInput>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun currentWeekWindow(): Pair<Long, Long> {
        val today = LocalDate.now(zoneId)
        val start = today.with(DayOfWeek.MONDAY).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to start + 7L * 24 * 60 * 60 * 1000
    }

    private fun enforceRegistrationRisk(
        conn: Connection,
        clientIp: String?,
        deviceId: String,
        currentTime: Long
    ) {
        val keyIp = safeKey(clientIp) ?: "unknown"
        val windowStart = currentTime - 10 * 60 * 1000
        val recent = conn.prepareStatement(
            """
            SELECT COUNT(*) AS total
            FROM registration_risk
            WHERE created_at >= ? AND (ip_address = ? OR device_id = ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, windowStart)
            stmt.setString(2, keyIp)
            stmt.setString(3, deviceId)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getLong("total")
            }
        }
        if (recent >= 5) {
            BasicMetrics.recordRateLimitHit()
            throw ApiException(HttpStatusCode.TooManyRequests, "注册过于频繁，请稍后再试")
        }
        conn.prepareStatement(
            "INSERT INTO registration_risk(ip_address, device_id, action, created_at) VALUES (?, ?, 'REGISTER', ?)"
        ).use { stmt ->
            stmt.setString(1, keyIp)
            stmt.setString(2, deviceId)
            stmt.setLong(3, currentTime)
            stmt.executeUpdate()
        }
    }

    private fun enforceLoginRateLimit(
        conn: Connection,
        clientIp: String?,
        deviceId: String,
        currentTime: Long
    ) {
        val keyIp = safeKey(clientIp) ?: "unknown"
        val windowStart = currentTime - 10 * 60 * 1000
        val recent = conn.prepareStatement(
            """
            SELECT COUNT(*) AS total
            FROM registration_risk
            WHERE action = 'LOGIN_FAILED'
              AND created_at >= ?
              AND (ip_address = ? OR device_id = ?)
            """.trimIndent()
        ).use { stmt ->
            stmt.setLong(1, windowStart)
            stmt.setString(2, keyIp)
            stmt.setString(3, deviceId)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getLong("total")
            }
        }
        if (recent >= 10) {
            BasicMetrics.recordRateLimitHit()
            throw ApiException(HttpStatusCode.TooManyRequests, "登录尝试过于频繁，请稍后再试")
        }
    }

    private fun recordLoginFailure(
        conn: Connection,
        userDbId: Long?,
        account: String,
        clientIp: String?,
        deviceId: String,
        details: String
    ) {
        val keyIp = safeKey(clientIp) ?: "unknown"
        conn.prepareStatement(
            "INSERT INTO registration_risk(ip_address, device_id, action, created_at) VALUES (?, ?, 'LOGIN_FAILED', ?)"
        ).use { stmt ->
            stmt.setString(1, keyIp)
            stmt.setString(2, deviceId)
            stmt.setLong(3, now())
            stmt.executeUpdate()
        }
        BasicMetrics.recordLoginFailure()
        audit(conn, userDbId, "LOGIN_FAILED", account, clientIp, false, details)
    }

    private fun clearLoginRisk(conn: Connection, clientIp: String?, deviceId: String) {
        val keyIp = safeKey(clientIp) ?: "unknown"
        conn.prepareStatement(
            "DELETE FROM registration_risk WHERE action = 'LOGIN_FAILED' AND (ip_address = ? OR device_id = ?)"
        ).use { stmt ->
            stmt.setString(1, keyIp)
            stmt.setString(2, deviceId)
            stmt.executeUpdate()
        }
    }

    private fun verifyCaptchaIfPresent(
        conn: Connection,
        request: RegisterRequest,
        clientIp: String?,
        deviceId: String,
        currentTime: Long
    ) {
        val captchaId = request.captchaId?.trim()?.takeIf { it.isNotBlank() }
        val answer = request.captchaAnswer?.trim()?.takeIf { it.isNotBlank() }
        if (captchaId == null || answer == null) {
            val keyIp = safeKey(clientIp) ?: "unknown"
            val windowStart = currentTime - 10 * 60 * 1000
            val recent = conn.prepareStatement(
                """
                SELECT COUNT(*) AS total
                FROM registration_risk
                WHERE created_at >= ? AND (ip_address = ? OR device_id = ?)
                """.trimIndent()
            ).use { stmt ->
                stmt.setLong(1, windowStart)
                stmt.setString(2, keyIp)
                stmt.setString(3, deviceId)
                stmt.executeQuery().use { rs ->
                    rs.next()
                    rs.getLong("total")
                }
            }
            if (recent >= 3) {
                BasicMetrics.recordRateLimitHit()
                throw ApiException(HttpStatusCode.BadRequest, "请先完成验证码")
            }
            return
        }

        conn.prepareStatement(
            "SELECT answer_hash, expires_at, used FROM captchas WHERE captcha_id = ?"
        ).use { stmt ->
            stmt.setString(1, captchaId)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) throw ApiException(HttpStatusCode.BadRequest, "验证码不存在或已过期")
                if (rs.getBoolean("used") || rs.getLong("expires_at") < currentTime) {
                    throw ApiException(HttpStatusCode.BadRequest, "验证码不存在或已过期")
                }
                if (!passwordHasher.verify(answer, rs.getString("answer_hash"))) {
                    throw ApiException(HttpStatusCode.BadRequest, "验证码错误")
                }
            }
        }
        conn.prepareStatement("UPDATE captchas SET used = TRUE WHERE captcha_id = ?").use { stmt ->
            stmt.setString(1, captchaId)
            stmt.executeUpdate()
        }
    }

    private fun audit(
        conn: Connection,
        actorUserId: Long?,
        action: String,
        target: String?,
        ipAddress: String?,
        success: Boolean,
        details: String?
    ) {
        conn.prepareStatement(
            """
            INSERT INTO audit_logs(actor_user_id, action, target, ip_address, success, details, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { stmt ->
            if (actorUserId == null) stmt.setNull(1, Types.BIGINT) else stmt.setLong(1, actorUserId)
            stmt.setString(2, action.take(64))
            stmt.setNullableString(3, target?.take(128))
            stmt.setNullableString(4, safeKey(ipAddress))
            stmt.setBoolean(5, success)
            stmt.setNullableString(6, details?.take(500))
            stmt.setLong(7, now())
            stmt.executeUpdate()
        }
    }

    private fun auditIepUploadFailure(actorUserId: Long?, childId: String, error: Throwable) {
        runCatching {
            dataSource.connection.use { auditConn ->
                audit(
                    auditConn,
                    actorUserId,
                    "IEP_UPLOAD_FAILED",
                    childId,
                    null,
                    false,
                    "${error::class.simpleName}: ${error.message.orEmpty()}".take(500)
                )
            }
        }
    }

    private fun enqueueEvent(conn: Connection, eventType: String, payload: Any) {
        conn.prepareStatement(
            "INSERT INTO event_outbox(event_type, payload_json, created_at, processed_at) VALUES (?, ?, ?, NULL)"
        ).use { stmt ->
            stmt.setString(1, eventType.take(64))
            stmt.setString(2, gson.toJson(payload))
            stmt.setLong(3, now())
            stmt.executeUpdate()
        }
    }

    private fun defaultReport(childId: String): PublicReportSummary {
        return PublicReportSummary(
            childId = childId,
            overview = "已开始记录成长数据，报告将在打卡后持续更新。",
            overallEvaluation = "当前数据量较少，建议持续完成周目标后再进行综合评价。",
            nextSuggestions = "保持家校同步记录，优先选择可观察、可量化的微目标。",
            aiAnalysis = "系统已建立基础报告档案，等待更多打卡数据进入分析。",
            dimensionScores = emptyMap(),
            dimensionHighlights = emptyList()
        )
    }

    private fun validateUsername(username: String) {
        if (!Regex("^[A-Za-z0-9_]{3,48}$").matches(username)) {
            throw ApiException(HttpStatusCode.BadRequest, "用户名仅支持 3-48 位字母、数字和下划线")
        }
    }

    private fun validatePasswordStrength(password: String) {
        if (password.length < 6 || password.length > 72) {
            throw ApiException(HttpStatusCode.BadRequest, "密码长度需为 6-72 位")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            throw ApiException(HttpStatusCode.BadRequest, "密码需同时包含字母和数字")
        }
    }

    private fun validateEmail(email: String) {
        if (email.length > 128 || !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matches(email)) {
            throw ApiException(HttpStatusCode.BadRequest, "邮箱格式不正确")
        }
    }

    private fun cleanText(value: String, maxLength: Int, fieldName: String): String {
        val cleaned = cleanOptionalText(value, maxLength)
        if (cleaned.isBlank()) {
            throw ApiException(HttpStatusCode.BadRequest, "$fieldName 不能为空")
        }
        return cleaned
    }

    private fun cleanOptionalText(value: String, maxLength: Int): String {
        val cleaned = value
            .filter { it == '\n' || it == '\r' || it == '\t' || it >= ' ' }
            .replace(Regex("(?i)<\\s*/?\\s*script"), "[script]")
            .replace(Regex("[\\r\\n]{3,}"), "\n\n")
            .trim()
        if (cleaned.length > maxLength) {
            throw ApiException(HttpStatusCode.BadRequest, "文本长度不能超过 $maxLength 个字符")
        }
        return cleaned
    }

    private fun parseDate(value: String, fieldName: String): LocalDate {
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { throw ApiException(HttpStatusCode.BadRequest, "$fieldName 格式需为 yyyy-MM-dd") }
    }

    private fun calculateAge(birthDate: LocalDate): Int {
        return Period.between(birthDate, LocalDate.now(zoneId)).years.coerceIn(0, 99)
    }

    private fun calculateInterventionDuration(startDate: LocalDate): String {
        val months = Period.between(startDate, LocalDate.now(zoneId)).toTotalMonths().coerceAtLeast(0)
        return when {
            months < 1 -> "不足 1 个月"
            months < 12 -> "$months 个月"
            else -> {
                val years = months / 12
                val rest = months % 12
                if (rest == 0L) "$years 年" else "$years 年 $rest 个月"
            }
        }
    }

    private fun safeKey(value: String?): String? {
        return value?.trim()?.takeIf { it.isNotEmpty() }?.take(128)
    }

    private fun demoAccountNames(): List<String> = listOf("parent001", "teacher001", "parent002", "1")

    private fun demoAccountStatuses(conn: Connection): Map<String, String> {
        return demoAccountNames().associateWith { username ->
            val user = findUserByUsername(conn, username) ?: return@associateWith "MISSING"
            val roles = queryRoles(conn, user.dbId)
            val expectedRole = when (username) {
                "parent001", "parent002" -> BackendRole.PARENT.name
                "teacher001" -> BackendRole.TEACHER.name
                "1" -> BackendRole.ADMIN.name
                else -> null
            }
            when {
                expectedRole != null && expectedRole !in roles -> "ROLE_INCOMPLETE"
                !passwordHasher.verify(if (username == "1") adminPassword else username, user.passwordHash) -> "PASSWORD_MISMATCH"
                else -> "READY"
            }
        }
    }

    private fun isLocalDemoMode(): Boolean {
        return runMode == "local-demo" || runMode == "local_demo" || runMode == "demo"
    }

    private fun now(): Long = System.currentTimeMillis()

    private fun uuid(): String = UUID.randomUUID().toString().replace("-", "")
}

private data class UserRecord(
    val dbId: Long,
    val userId: String,
    val username: String,
    val name: String,
    val email: String?,
    val avatarKey: String?,
    val passwordHash: String,
    val failedLoginCount: Int,
    val lockedUntil: Long?
)

private data class SessionContext(
    val token: String,
    val userDbId: Long,
    val userId: String,
    val username: String,
    val name: String,
    val email: String?,
    val avatarKey: String?,
    val roles: List<String>,
    val availableRoles: List<String>,
    val activeRole: String?,
    val selectedChildDbId: Long?,
    val selectedChildId: String?
)

private fun PreparedStatement.setNullableString(index: Int, value: String?) {
    if (value == null) {
        setNull(index, Types.VARCHAR)
    } else {
        setString(index, value)
    }
}

private fun ResultSet.getNullableString(column: String): String? {
    val value = getString(column)
    return if (wasNull()) null else value
}

private fun ResultSet.getNullableLong(column: String): Long? {
    val value = getLong(column)
    return if (wasNull()) null else value
}
