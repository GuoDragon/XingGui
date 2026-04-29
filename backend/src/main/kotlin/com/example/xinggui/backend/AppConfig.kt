package com.example.xinggui.backend

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale

data class BackendConfig(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val seedDataDir: Path,
    val uploadDir: Path,
    val mode: String,
    val adminPassword: String
) {
    val isLocalDemoMode: Boolean
        get() = mode == "local-demo" || mode == "local_demo" || mode == "demo"

    fun validateSafety() {
        if (isLocalDemoMode) return

        val insecureSettings = buildList {
            if (dbUser == "root" && dbPassword == "root") {
                add("XINGGUI_DB_USER/XINGGUI_DB_PASSWORD must not use root/root")
            }
            if (adminPassword == "1") {
                add("XINGGUI_ADMIN_PASSWORD must not be the demo default 1")
            }
            if (dbUrl.contains("useSSL=false", ignoreCase = true)) {
                add("XINGGUI_DB_URL must not disable TLS outside local demo mode")
            }
            env("XINGGUI_PUBLIC_BASE_URL")?.let { publicUrl ->
                if (publicUrl.startsWith("http://", ignoreCase = true)) {
                    add("XINGGUI_PUBLIC_BASE_URL must use HTTPS outside local demo mode")
                }
            }
        }

        check(insecureSettings.isEmpty()) {
            "Unsafe production configuration:\n${insecureSettings.joinToString(separator = "\n") { "- $it" }}"
        }
    }

    fun startupSummary(): List<String> {
        return listOf(
            "XingGui backend starting",
            "mode=$mode",
            "port=$port",
            "db=${redactDbUrl(dbUrl)}",
            "dbUser=$dbUser",
            "seedDir=$seedDataDir",
            "uploadDir=$uploadDir",
            "localDemoMode=$isLocalDemoMode"
        )
    }

    companion object {
        fun load(): BackendConfig {
            val root = Paths.get("").toAbsolutePath().normalize()
            val defaultSeedDir = root.resolve("app").resolve("src").resolve("main")
                .resolve("assets").resolve("data")
            val fallbackSeedDir = root.resolve("src").resolve("main").resolve("resources").resolve("seed")
            val seedDir = env("XINGGUI_SEED_DIR")
                ?.let { Paths.get(it) }
                ?: if (Files.exists(defaultSeedDir)) defaultSeedDir else fallbackSeedDir

            val uploadDir = env("XINGGUI_UPLOAD_DIR")
                ?.let { Paths.get(it) }
                ?: root.resolve("storage").resolve("iep-documents")

            return BackendConfig(
                port = env("XINGGUI_PORT")?.toIntOrNull() ?: 8080,
                dbUrl = env("XINGGUI_DB_URL")
                    ?: "jdbc:mysql://127.0.0.1:3306/xinggui?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8",
                dbUser = env("XINGGUI_DB_USER") ?: "root",
                dbPassword = env("XINGGUI_DB_PASSWORD") ?: "root",
                seedDataDir = seedDir.toAbsolutePath().normalize(),
                uploadDir = uploadDir.toAbsolutePath().normalize(),
                mode = (env("XINGGUI_MODE") ?: "local-demo").lowercase(Locale.ROOT),
                adminPassword = env("XINGGUI_ADMIN_PASSWORD") ?: "1"
            )
        }

        fun env(name: String): String? {
            return System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() }
                ?: System.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }
        }

        private fun redactDbUrl(value: String): String {
            return value.replace(Regex("(?i)(password=)[^&;]+"), "\$1***")
        }
    }
}
