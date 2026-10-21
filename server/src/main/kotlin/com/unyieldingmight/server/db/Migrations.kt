package com.unyieldingmight.server.db

import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Minimal versioned migration runner. Applies `db/migration/V{n}__name.sql` resources in order
 * and records them in `schema_migration`, so the same scripts run locally and on Aiven.
 */
object Migrations {
    private val log = LoggerFactory.getLogger(Migrations::class.java)

    /** Listed explicitly because classpath directory listing doesn't work inside a jar. */
    private val scripts = listOf(
        1 to "V1__schema.sql",
    )

    fun migrate(db: Database) {
        db.txBlocking {
            update(
                """
                CREATE TABLE IF NOT EXISTS schema_migration (
                    version INT NOT NULL PRIMARY KEY,
                    script VARCHAR(100) NOT NULL,
                    applied_at DATETIME NOT NULL
                )
                """.trimIndent()
            )
        }
        val applied = db.txBlocking { query("SELECT version FROM schema_migration") { it.getInt(1) }.toSet() }

        for ((version, script) in scripts) {
            if (version in applied) continue
            val sql = Migrations::class.java.getResourceAsStream("/db/migration/$script")
                ?.bufferedReader()?.use { it.readText() }
                ?: error("Migration $script not found on the classpath")
            log.info("Applying migration {}", script)
            // MySQL implicitly commits DDL, so each script is written to be applied once against an empty schema.
            db.txBlocking {
                splitStatements(sql).forEach { update(it) }
                update("INSERT INTO schema_migration (version, script, applied_at) VALUES (?, ?, ?)", version, script, Instant.now())
            }
        }
    }

    internal fun splitStatements(sql: String): List<String> =
        sql.lineSequence()
            .map { line -> line.substringBefore("--").trimEnd() }
            .joinToString("\n")
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
