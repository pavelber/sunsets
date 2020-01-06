package org.bernshtam.weather

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.commons.lang.time.DateUtils
import org.flywaydb.core.Flyway
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
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

    private fun getConnection(): Connection {
        return cpds.connection
    }

    fun shutdown() {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("SHUTDOWN ")
            }
        }
    }

    fun updateCell(cell: Cell) {
        getConnection().use { connection ->
            val insertCellStatement = connection.prepareStatement(
                    """
                        update cell set low = ?, medium = ?, high = ?, sunset_near = ?, sunset_far= ?, sun_blocking_near = ?, sun_blocking_far = ?, rank = ?, description = ?
                        where date = ? and latitude = ? and  longitude = ?  
                         """)


            var int = 1
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.setDouble(int++, cell.sunset_near)
            insertCellStatement.setDouble(int++, cell.sunset_far)
            insertCellStatement.setDouble(int++, cell.sun_blocking_near)
            insertCellStatement.setDouble(int++, cell.sun_blocking_far)
            insertCellStatement.setDouble(int++, cell.rank)
            insertCellStatement.setString(int++, cell.description)
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.latitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.longitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.executeUpdate()
        }
    }

    fun saveLocalCloudsCell(cell: Cell) {
        getConnection().use { connection ->
            val insertCellStatement = connection.prepareStatement(
                    """
                        MERGE INTO cell AS t USING (VALUES(?,?,?)) AS vals(date,latitude,longitude)
                        ON t.latitude=vals.latitude AND t.longitude=vals.longitude and t.date = vals.date
                        WHEN NOT MATCHED THEN  
                            INSERT values ?, ?, ? , ?, ?, ?, ?, ?, ?,?,?,?,?
                        WHEN MATCHED THEN update set low = ?, medium = ?, high = ?
                         """)


            var int = 1
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.latitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.longitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setDouble(int++, cell.square_size)
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.latitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.setBigDecimal(int++, BigDecimal(cell.longitude).setScale(2, RoundingMode.HALF_EVEN))
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.setDouble(int++, cell.sunset_near)
            insertCellStatement.setDouble(int++, cell.sunset_far)
            insertCellStatement.setDouble(int++, cell.sun_blocking_near)
            insertCellStatement.setDouble(int++, cell.sun_blocking_far)
            insertCellStatement.setDouble(int++, cell.rank)
            insertCellStatement.setString(int++, cell.description)
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.executeUpdate()
        }
    }

    fun getCells(date: LocalDate): List<Cell> {
        getConnection().use { connection ->
            connection.createStatement().use { statement ->

                val resultSet = statement.executeQuery(
                        """select date,square_size, latitude,longitude,low, medium, high, rank, sunset_near, sunset_far, sun_blocking_near,sun_blocking_far, description  
                            |from cell where date = '$date'""".trimMargin())
                resultSet.use {
                    return generateSequence {
                        if (resultSet.next())
                            cell(resultSet) else null
                    }.toList()
                }
            }
        }
    }

    private fun cell(resultSet: ResultSet): Cell {
        var i = 1
        return Cell(
                resultSet.getDate(i++).toLocalDate(),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getDouble(i++),
                resultSet.getString(i++)
        )
    }

    fun findNearestCell(lat: Double, long: Double, date: LocalDate): Cell? {
        getConnection().use { connection ->
            val statement = connection.prepareStatement(
                    """
                        select date,square_size, latitude,longitude,low, medium, high, rank, sunset_near, sunset_far, sun_blocking_near,sun_blocking_far, description  
                        from cell where DATE = ? and abs(LATITUDE-?)<$CELL_SIZE and abs(LONGITUDE-?)<$CELL_SIZE 
                         """)


            var int = 1
            statement.setDate(int++, java.sql.Date.valueOf(date))
            statement.setDouble(int++, lat)
            statement.setDouble(int++, long)

            val resultSet = statement.executeQuery()
            return if (resultSet.next())
                cell(resultSet)
            else null
        }
    }
}