package org.bernshtam.weather

import org.bernshtam.weather.dal.DB
import org.bernshtam.weather.datasources.IMSConnector
import java.time.LocalDateTime
import java.time.ZoneId


object MainFromCoordinatesRunningTime {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 32.5
        val long = 34.5
        val place = Place.PLACES.getValue("Tel Aviv")
        val dateTime = LocalDateTime.of(2019, 12, 14, 14, 0)
        (0..6L).forEach {
            val plusHours = dateTime.plusHours(it)
            println(plusHours.toString() + " " + IMSConnector.getCloudsParams(PointAtTime.at(place.lat, place.long, plusHours.atZone(ZoneId.systemDefault()))))

        }//    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(1)))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(2)))
        DB.shutdown()
    }

}