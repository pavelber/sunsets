package org.bernshtam.weather.dal

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.flywaydb.core.Flyway
import java.sql.Connection

object DB {
    private const val url = "jdbc:hsqldb:file:db/weather.db;shutdown=true"
    private const val user = "SA"
    private const val password = ""

    private val cpds = ComboPooledDataSource()

    init {
        cpds.driverClass = "org.hsqldb.jdbcDriver" //loads the jdbc driver
        cpds.jdbcUrl = url
        cpds.user = user
        cpds.password = password

        cpds.minPoolSize = 5
        cpds.initialPoolSize = 5
        cpds.acquireIncrement = 5
        cpds.maxPoolSize = 20
    }

    fun migrate() {
        val flyway = Flyway.configure().dataSource(url, user, password).load()
        flyway.migrate()

    }

    fun close() {
        cpds.close()
    }

    fun getConnection(): Connection {
        return cpds.connection
    }

    fun shutdown() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("SHUTDOWN ")
            }
        }
    }
}