package org.bernshtam.weather

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.commons.lang.time.DateUtils
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit


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

    fun putToDB(p: PointAtTime, json: String, source:String) {
        getConnection().use { connection ->
            connection.autoCommit = true
            connection.createStatement().use { statement ->

                val lat = (p.lat * 1000).toInt()
                val long = (p.long * 1000).toInt()
                val time = p.getLocalTime()
                val sql = """
                        MERGE INTO cache AS t USING (VALUES($lat,$long,$time)) AS vals(lat,long,time)
                        ON t.latitude=vals.lat AND t.longitude=vals.long and t.time = vals.time
                        WHEN NOT MATCHED THEN insert  VALUES $lat,$long,$time,'$json',cast(TIMESTAMP ($time) as date), now(), '$source'
                        WHEN MATCHED THEN update set json = '$json', update_time = now()
                        """
               // println(sql)
                statement.execute(sql)

            }
        }

    }

    fun get(p: PointAtTime, source:String): String? {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->

                val lat = (p.lat * 1000).toInt()
                val long = (p.long * 1000).toInt()
                val time = p.getLocalTime()
             //   println("select json from darksky where latitude = $lat and longitude = $long and time = $time")
                val resultSet = statement.executeQuery("select json,update_time from cache where latitude = $lat and longitude = $long and time = $time and source = '$source'")
                resultSet.use {
                    return if (it.next())
                        if (shouldUpdate(time, it.getTimestamp(2)))
                            null
                        else it.getString(1)
                    else null
                }
            }

        }
    }
    fun shutdown() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("SHUTDOWN ")
            }
        }
    }
    private fun shouldUpdate(time: Long, timestamp: Timestamp): Boolean {
        val nowDate = Date()
        val requestDate = Date(time * 1000)

        // past not update
        if (requestDate < nowDate)
            return false

        val diffInMillies = Math.abs(nowDate.time - timestamp.time)
        val updatedHoursAgo = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        return if (DateUtils.isSameDay(nowDate, requestDate))
            updatedHoursAgo > 0 // today update every hour
        else updatedHoursAgo > 6 // future update each 6 hours
    }
}