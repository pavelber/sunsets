package org.bernshtam.weather.dal

import org.bernshtam.weather.Coordinates
import java.math.BigDecimal
import java.math.RoundingMode

object CoordinatesDAL {

    fun save(coordinates: Coordinates) {
        DB.getConnection().use { connection ->
            val insertStatement = connection.prepareStatement(
                    "insert into COORDINATES_FOR_RANK values(?,?)")

            var int = 1
            insertStatement.setBigDecimal(int++, BigDecimal(coordinates.lat).setScale(2, RoundingMode.HALF_EVEN))
            insertStatement.setBigDecimal(int++, BigDecimal(coordinates.long).setScale(2, RoundingMode.HALF_EVEN))
            insertStatement.execute()
        }
    }

    fun getAll(): List<Coordinates> {
        DB.getConnection().use { connection ->
            connection.createStatement().use { statement ->

                val resultSet = statement.executeQuery("select latitude,longitude from COORDINATES_FOR_RANK")
                resultSet.use {
                    return generateSequence {
                        if (resultSet.next())
                            Coordinates(resultSet.getDouble(1),
                                    resultSet.getDouble(2)) else null
                    }.toList()
                }
            }
        }
    }

}