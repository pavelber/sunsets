package org.bernshtam.weather

import org.bernshtam.weather.dal.CoordinatesDAL
import org.bernshtam.weather.utils.Utils.eq

object IsraelCoordinatesStore {
    private val coordinates = CoordinatesDAL.getAll()

    fun isInsideIsrael(c: Cell): Boolean {
        return coordinates.any { coord -> c.latitude.eq(coord.lat - CELL_SIZE) && c.longitude.eq(coord.long - CELL_SIZE) } ||
                coordinates.any { coord -> c.latitude.eq(coord.lat + CELL_SIZE) && c.longitude.eq(coord.long - CELL_SIZE) } ||
                coordinates.any { coord -> c.latitude.eq(coord.lat - CELL_SIZE) && c.longitude.eq(coord.long + CELL_SIZE) } ||
                coordinates.any { coord -> c.latitude.eq(coord.lat + CELL_SIZE) && c.longitude.eq(coord.long + CELL_SIZE) }
    }

}