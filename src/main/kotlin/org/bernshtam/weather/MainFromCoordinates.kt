package org.bernshtam.weather

import org.bernshtam.weather.SunSetService.getMarkAndDescription
import java.time.LocalDate


object MainFromCoordinates {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 31.9
        val long = 34.7
        println(getMarkAndDescription(lat, long, LocalDate.now()))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(1)))
        //    println(getMarkAndDescription(lat, long, LocalDate.now().plusDays(2)))
        DB.shutdown()
    }

}