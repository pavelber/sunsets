package org.bernshtam.weather

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

    fun putToDB(p: PointAtTime, json: String) {
        getConnection().use { connection ->
            connection.autoCommit = true
            connection.createStatement().use { statement ->
                p.apply {
                    val lat1 = (lat * 1000).toInt()
                    val long1 = (long * 1000).toInt()
                    val time1 = time
                    statement.execute("insert into darksky VALUES ($lat1,$long1,$time1,'$json',cast(TIMESTAMP ($time) as date))")
                }
            }
        }

    }

    fun get(p: PointAtTime): String? {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->

                val lat = (p.lat*1000).toInt()
                val long = (p.long*1000).toInt()
                val time = p.time
                val resultSet = statement.executeQuery("select json from darksky where latitude = $lat and longitude = $long and time = $time")
                resultSet.use {
                    return if (it.next())
                        it.getString(1)
                    else null
                }
            }

        }
    }
}