package com.unyieldingmight.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

/**
 * Thin JDBC layer: a pooled DataSource plus [Tx] helpers for parameterised queries.
 * Every call runs inside a transaction on the IO dispatcher, so Ktor's request threads never block.
 */
class Database(val dataSource: DataSource) {

    suspend fun <T> tx(block: Tx.() -> T): T = withContext(Dispatchers.IO) { txBlocking(block) }

    fun <T> txBlocking(block: Tx.() -> T): T = dataSource.connection.use { conn ->
        conn.autoCommit = false
        try {
            val result = Tx(conn).block()
            conn.commit()
            result
        } catch (e: Throwable) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }

    companion object {
        fun pooled(url: String, user: String, password: String, maxPoolSize: Int = 10): Database {
            val config = HikariConfig().apply {
                jdbcUrl = url
                username = user
                this.password = password
                maximumPoolSize = maxPoolSize
                poolName = "unyielding-might"
                isAutoCommit = true
            }
            return Database(HikariDataSource(config))
        }
    }
}

class Tx(val connection: Connection) {

    fun <T> query(sql: String, vararg params: Any?, map: (ResultSet) -> T): List<T> =
        prepare(sql, params).use { stmt ->
            stmt.executeQuery().use { rs ->
                buildList { while (rs.next()) add(map(rs)) }
            }
        }

    fun <T> queryOne(sql: String, vararg params: Any?, map: (ResultSet) -> T): T? =
        query(sql, *params, map = map).firstOrNull()

    fun count(sql: String, vararg params: Any?): Long =
        queryOne(sql, *params) { it.getLong(1) } ?: 0L

    /** Executes an UPDATE/DELETE/INSERT and returns the affected row count. */
    fun update(sql: String, vararg params: Any?): Int = prepare(sql, params).use { it.executeUpdate() }

    /** Executes an INSERT and returns the generated key. */
    fun insert(sql: String, vararg params: Any?): Int {
        val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        return stmt.use {
            bind(it, params)
            it.executeUpdate()
            it.generatedKeys.use { keys ->
                check(keys.next()) { "Insert did not return a generated key" }
                keys.getInt(1)
            }
        }
    }

    private fun prepare(sql: String, params: Array<out Any?>): PreparedStatement =
        connection.prepareStatement(sql).also { bind(it, params) }

    private fun bind(stmt: PreparedStatement, params: Array<out Any?>) {
        params.forEachIndexed { index, value ->
            val i = index + 1
            when (value) {
                null -> stmt.setNull(i, Types.NULL)
                is String -> stmt.setString(i, value)
                is Int -> stmt.setInt(i, value)
                is Long -> stmt.setLong(i, value)
                is Double -> stmt.setBigDecimal(i, BigDecimal.valueOf(value))
                is Boolean -> stmt.setBoolean(i, value)
                is Enum<*> -> stmt.setString(i, value.name)
                is Instant -> stmt.setTimestamp(i, Timestamp.valueOf(LocalDateTime.ofInstant(value, ZoneOffset.UTC)))
                is LocalDate -> stmt.setDate(i, Date.valueOf(value))
                else -> stmt.setObject(i, value)
            }
        }
    }
}

// Timestamps are stored as UTC wall-clock DATETIMEs; reading through LocalDateTime avoids driver time-zone shifts.
fun ResultSet.instant(column: String): Instant =
    getObject(column, LocalDateTime::class.java).toInstant(ZoneOffset.UTC)

fun ResultSet.instantOrNull(column: String): Instant? =
    getObject(column, LocalDateTime::class.java)?.toInstant(ZoneOffset.UTC)

fun ResultSet.epochMillis(column: String): Long = instant(column).toEpochMilli()

fun ResultSet.localDate(column: String): LocalDate = getObject(column, LocalDate::class.java)

fun ResultSet.intOrNull(column: String): Int? = getInt(column).takeUnless { wasNull() }

inline fun <reified E : Enum<E>> ResultSet.enum(column: String): E = enumValueOf(getString(column))
