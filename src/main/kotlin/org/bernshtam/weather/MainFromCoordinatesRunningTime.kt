package org.bernshtam.weather

import org.bernshtam.weather.dal.DB
import org.bernshtam.weather.datasources.IMSConnector
import java.time.LocalDateTime
import java.time.ZoneId


object MainFromCoordinatesRunningTime {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 31.8
        val long = 35.0
        //val place = Place.PLACES.getValue("Tel Aviv")
        val dateTime = LocalDateTime.of(2020, 1, 17, 0, 0)
        (0..6L).forEach {
            val plusHours = dateTime.plusHours(it)
            //println(plusHours.toString() + " " + IMSConnector.getIMSParams(PointAtTime.at(place.lat, place.long, plusHours.atZone(ZoneId.systemDefault()))))
            println(plusHours.toString() + " " + IMSConnector.getIMSParams(PointAtTime.at(lat, long, plusHours.atZone(ZoneId.systemDefault()))))

        }//    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(1)))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(2)))
        DB.shutdown()
    }

}