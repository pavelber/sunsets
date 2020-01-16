package org.bernshtam.weather.dal

import org.bernshtam.weather.CELL_SIZE
import org.bernshtam.weather.Cell
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.ResultSet
import java.time.LocalDate

object CellDAL {

    fun updateCell(cell: Cell) {
        DB.getConnection().use { connection ->
            val insertCellStatement = connection.prepareStatement(
                    """
                        update cell set low = ?, medium = ?, high = ?, sunset_near = ?, sunset_far= ?, sun_blocking_near = ?, sun_blocking_far = ?, rank = ?, description = ?
                        where date = ? and latitude = ? and  longitude = ?  
                         """)


            var int = 1
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.setBigDecimal(int++, round(cell.sunset_near))
            insertCellStatement.setBigDecimal(int++, round(cell.sunset_far))
            insertCellStatement.setBigDecimal(int++, round(cell.sun_blocking_near))
            insertCellStatement.setBigDecimal(int++, round(cell.sun_blocking_far))
            insertCellStatement.setBigDecimal(int++, round(cell.rank))
            insertCellStatement.setString(int++, cell.description)
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setBigDecimal(int++, round(cell.latitude))
            insertCellStatement.setBigDecimal(int++, round(cell.longitude))
            insertCellStatement.executeUpdate()
        }
    }

    fun saveLocalCloudsCell(cell: Cell) {
        DB.getConnection().use { connection ->
            val insertCellStatement = connection.prepareStatement(
                    """
                        MERGE INTO cell AS t USING (VALUES(?,?,?)) AS vals(date,latitude,longitude)
                        ON t.latitude=vals.latitude AND t.longitude=vals.longitude and t.date = vals.date
                        WHEN NOT MATCHED THEN  
                            INSERT values ?, ?, ? , ?, ?, ?, ?, ?, ?, ?,?,?,?,?
                        WHEN MATCHED THEN update set low = ?, medium = ?, high = ?
                         """)


            var int = 1
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setBigDecimal(int++, round(cell.latitude))
            insertCellStatement.setBigDecimal(int++, round(cell.longitude))
            insertCellStatement.setDate(int++, java.sql.Date.valueOf(cell.date))
            insertCellStatement.setDouble(int++, cell.square_size)
            insertCellStatement.setBigDecimal(int++, round(cell.latitude))
            insertCellStatement.setBigDecimal(int++, round(cell.longitude))
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.setDouble(int++, cell.rain)
            insertCellStatement.setBigDecimal(int++, round(cell.sunset_near))
            insertCellStatement.setBigDecimal(int++, round(cell.sunset_far))
            insertCellStatement.setBigDecimal(int++, round(cell.sun_blocking_near))
            insertCellStatement.setBigDecimal(int++, round(cell.sun_blocking_far))
            insertCellStatement.setBigDecimal(int++, round(cell.rank))
            insertCellStatement.setString(int++, cell.description)
            insertCellStatement.setDouble(int++, cell.low)
            insertCellStatement.setDouble(int++, cell.medium)
            insertCellStatement.setDouble(int++, cell.high)
            insertCellStatement.executeUpdate()
        }
    }

    private fun round(v: Double?) = if (v != null) BigDecimal(v).setScale(2, RoundingMode.HALF_EVEN) else null
    private fun d(v: BigDecimal?) = v?.toDouble()

    fun getCells(date: LocalDate): List<Cell> {
        DB.getConnection().use { connection ->
            connection.createStatement().use { statement ->

                val resultSet = statement.executeQuery(
                        """select date,square_size, latitude,longitude,low, medium, high, rain, rank, sunset_near, sunset_far, sun_blocking_near,sun_blocking_far, description  
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

    fun findNearestCell(lat: Double, long: Double, date: LocalDate): Cell? {
        DB.getConnection().use { connection ->
            val statement = connection.prepareStatement(
                    """
                        select date,square_size, latitude,longitude,low, medium, high, rain, rank, sunset_near, sunset_far, sun_blocking_near,sun_blocking_far, description  
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
                d(resultSet.getBigDecimal(i++)),
                d(resultSet.getBigDecimal(i++)),
                d(resultSet.getBigDecimal(i++)),
                d(resultSet.getBigDecimal(i++)),
                d(resultSet.getBigDecimal(i++)),
                resultSet.getString(i++)
        )
    }


}