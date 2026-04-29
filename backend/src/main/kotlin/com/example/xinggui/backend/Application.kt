package com.example.xinggui.backend

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.serialization.gson.gson
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.readAvailable
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.util.UUID

private const val IEP_UPLOAD_MAX_BYTES = 20 * 1024 * 1024

fun main() {
    val config = BackendConfig.load()
    config.validateSafety()
    config.startupSummary().forEach { println(it) }
    val gson = GsonBuilder().create()
    val dataSource = DatabaseFactory.create(config)
    DatabaseFactory.migrate(dataSource)
    val service = XingGuiService(
        dataSource = dataSource,
        gson = gson,
        passwordHasher = PasswordHasher(),
        uploadDir = config.uploadDir,
        runMode = config.mode,
        adminPassword = config.adminPassword
    )
    runBlocking {
        service.seedIfNeeded(JsonSeedLoader(gson, config.seedDataDir))
    }
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
        install(StatusPages) {
            exception<ApiException> { call, cause ->
                val requestId = call.requestId()
                System.err.println("requestId=$requestId path=${call.request.path()} error=${cause::class.simpleName} status=${cause.status.value}")
                call.respond(cause.status, ApiError(cause.message, requestId))
            }
            exception<BadRequestException> { call, cause ->
                val requestId = call.requestId()
                System.err.println("requestId=$requestId path=${call.request.path()} error=${cause::class.simpleName} status=400")
                call.respond(HttpStatusCode.BadRequest, ApiError(cause.message ?: "请求参数错误", requestId))
            }
            exception<Throwable> { call, cause ->
                val requestId = call.requestId()
                System.err.println("requestId=$requestId path=${call.request.path()} error=${cause::class.qualifiedName}: ${cause.message}")
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, ApiError("服务暂时不可用，请稍后再试", requestId))
            }
        }

        routing {
            get("/health") {
                call.respondMeasured { service.health() }
            }
            get("/metrics/basic") {
                call.respondMeasured { service.metrics() }
            }
            get("/security/captcha") {
                call.respondMeasured { service.createCaptcha(call.clientIp(), call.request.headers["X-Device-Id"]) }
            }

            post("/auth/register") {
                call.respondMeasured(HttpStatusCode.Created) {
                    service.register(call.receive(), call.clientIp())
                }
            }
            post("/auth/login") {
                call.respondMeasured {
                    service.login(call.receive(), call.clientIp())
                }
            }
            get("/auth/me") {
                call.respondMeasured {
                    service.me(call.bearerToken())
                }
            }
            post("/auth/logout") {
                call.respondMeasured {
                    service.logout(call.bearerToken())
                    mapOf("success" to true)
                }
            }
            post("/auth/logout-all") {
                call.respondMeasured {
                    service.logoutAll(call.bearerToken())
                    mapOf("success" to true)
                }
            }

            get("/session/roles") {
                call.respondMeasured { service.getSessionRoles(call.bearerToken()) }
            }
            post("/session/active-role") {
                call.respondMeasured { service.updateActiveRole(call.bearerToken(), call.receive()) }
            }
            post("/session/selected-child") {
                call.respondMeasured { service.updateSelectedChild(call.bearerToken(), call.receive()) }
            }

            get("/users/me") {
                call.respondMeasured { service.getCurrentUser(call.bearerToken()) }
            }
            put("/users/me/profile") {
                call.respondMeasured { service.updateCurrentUserProfile(call.bearerToken(), call.receive()) }
            }

            get("/admin/users") {
                call.respondMeasured { service.getUsers(call.bearerToken()) }
            }
            get("/admin/users/{userId}/roles") {
                val userId = call.parameters["userId"].orEmpty()
                call.respondMeasured { service.getUserRoles(call.bearerToken(), userId) }
            }
            post("/admin/users/{userId}/roles") {
                val userId = call.parameters["userId"].orEmpty()
                call.respondMeasured { service.assignRole(call.bearerToken(), userId, call.receive()) }
            }
            delete("/admin/users/{userId}/roles/{role}") {
                val userId = call.parameters["userId"].orEmpty()
                val role = call.parameters["role"].orEmpty()
                call.respondMeasured { service.removeRole(call.bearerToken(), userId, role) }
            }

            get("/children") {
                call.respondMeasured { service.listChildren(call.bearerToken()) }
            }
            get("/children/{childId}") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getChild(call.bearerToken(), childId) }
            }
            put("/children/{childId}/profile") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.updateChildProfile(call.bearerToken(), childId, call.receive()) }
            }

            get("/goals/{childId}") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getGoalPlan(call.bearerToken(), childId) }
            }
            get("/goals/{childId}/iep") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getLatestIepDocument(call.bearerToken(), childId) }
            }
            post("/goals/{childId}/iep") {
                val childId = call.parameters["childId"].orEmpty()
                val uploadRequest = call.receiveIepUploadRequest(gson)
                call.respondMeasured(HttpStatusCode.Created) {
                    service.uploadIepDocument(call.bearerToken(), childId, uploadRequest)
                }
            }

            post("/archive/checkin") {
                call.respondMeasured { service.submitArchiveCheckIn(call.bearerToken(), call.receive()) }
            }
            get("/archive/weekly-counts/{childId}") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getWeeklyCounts(call.bearerToken(), childId) }
            }

            get("/reports/{childId}") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getReportSummary(call.bearerToken(), childId) }
            }
            get("/reports/{childId}/history") {
                val childId = call.parameters["childId"].orEmpty()
                call.respondMeasured { service.getReportHistory(call.bearerToken(), childId) }
            }

            get("/resources") {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val query = call.request.queryParameters["q"]
                call.respondMeasured { service.getResources(call.bearerToken(), limit, offset, query) }
            }
            get("/resources/runtime") {
                call.respondMeasured { service.getResourceRuntimeState(call.bearerToken()) }
            }
            put("/resources/runtime") {
                call.respondMeasured {
                    service.saveResourceRuntimeState(call.bearerToken(), call.receive())
                }
            }
        }
    }.start(wait = true)
}

private fun ApplicationCall.bearerToken(): String {
    val header = request.headers[HttpHeaders.Authorization].orEmpty()
    if (!header.startsWith("Bearer ", ignoreCase = true)) {
        throw ApiException(HttpStatusCode.Unauthorized, "请先登录")
    }
    return header.removePrefix("Bearer").trim()
}

private fun ApplicationCall.clientIp(): String {
    return request.headers["X-Forwarded-For"]?.substringBefore(',')?.trim()?.takeIf { it.isNotBlank() }
        ?: request.headers["X-Real-IP"]?.trim()?.takeIf { it.isNotBlank() }
        ?: request.origin.remoteHost
}

private fun ApplicationCall.requestId(): String {
    return request.headers["X-Request-Id"]?.trim()?.takeIf { it.isNotBlank() }
        ?: UUID.randomUUID().toString()
}

private suspend fun ApplicationCall.respondMeasured(
    status: HttpStatusCode = HttpStatusCode.OK,
    block: suspend () -> Any
) {
    val started = System.nanoTime()
    try {
        val result = block()
        respond(status, result)
    } finally {
        BasicMetrics.recordRequest(System.nanoTime() - started)
    }
}

private suspend fun ApplicationCall.receiveIepUploadRequest(gson: com.google.gson.Gson): IepUploadRequest {
    val multipart = receiveMultipart()
    val fields = linkedMapOf<String, String>()
    var uploadedFile: UploadedIepFile? = null
    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                part.name?.let { fields[it] = part.value }
            }

            is PartData.FileItem -> {
                if (part.name == "file") {
                    val bytes = part.provider().use { input ->
                        readBytesLimited(input, IEP_UPLOAD_MAX_BYTES + 1)
                    }
                    uploadedFile = UploadedIepFile(
                        originalFileName = part.originalFileName ?: "upload.bin",
                        contentType = part.contentType?.toString(),
                        bytes = bytes
                    )
                }
            }

            else -> Unit
        }
        part.dispose()
    }

    val weeklyGoalType = object : TypeToken<List<IepWeeklyGoalInput>>() {}.type
    val weeklyGoals = fields["weeklyGoalsJson"]?.let {
        gson.fromJson<List<IepWeeklyGoalInput>>(it, weeklyGoalType)
    }.orEmpty()

    return IepUploadRequest(
        semesterGoal = fields["semesterGoal"].orEmpty(),
        monthlyGoal = fields["monthlyGoal"].orEmpty(),
        weeklyGoals = weeklyGoals,
        notes = fields["notes"],
        file = uploadedFile ?: throw ApiException(HttpStatusCode.BadRequest, "请上传 IEP 文档")
    )
}

private fun readBytesLimited(input: Input, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (!input.endOfInput) {
        val remaining = maxBytes - output.size()
        if (remaining <= 0) {
            throw ApiException(HttpStatusCode.PayloadTooLarge, "上传文件超过大小限制")
        }
        val read = input.readAvailable(buffer, 0, minOf(buffer.size, remaining))
        if (read <= 0) {
            break
        }
        output.write(buffer, 0, read)
    }
    if (output.size() >= maxBytes) {
        throw ApiException(HttpStatusCode.PayloadTooLarge, "上传文件超过大小限制")
    }
    return output.toByteArray()
}
