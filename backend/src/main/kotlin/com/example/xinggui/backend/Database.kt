package com.example.xinggui.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DatabaseFactory {
    fun create(config: BackendConfig): HikariDataSource {
        val hikari = HikariConfig().apply {
            jdbcUrl = config.dbUrl
            username = config.dbUser
            password = config.dbPassword
            driverClassName = "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = System.getenv("XINGGUI_DB_POOL_SIZE")?.toIntOrNull() ?: 10
            minimumIdle = 2
            connectionTimeout = 10_000
            validationTimeout = 5_000
            idleTimeout = 60_000
            maxLifetime = 30 * 60_000
            poolName = "xinggui-hikari"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            addDataSourceProperty("useUnicode", "true")
            addDataSourceProperty("characterEncoding", "utf8")
        }
        return HikariDataSource(hikari)
    }

    fun migrate(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
