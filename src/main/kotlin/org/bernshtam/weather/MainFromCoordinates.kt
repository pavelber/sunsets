package org.bernshtam.weather


import org.bernshtam.weather.dal.DB
import java.time.LocalDate


object MainFromCoordinates {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 32.5
        val long = 34.5
        val place = Place.PLACES.getValue("Tel Aviv")
        val service = DBSunSetService()
        println(service.getMarkAndDescription(place.lat,place.long, LocalDate.now()))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(1)))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(2)))
        DB.shutdown()
    }

}