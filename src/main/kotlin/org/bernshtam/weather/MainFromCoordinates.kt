package org.bernshtam.weather

import org.bernshtam.weather.SunSetService.getMarkAndDescription
import java.time.LocalDate


object MainFromCoordinates {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 32.08
        val long = 34.78
        println(getMarkAndDescription(lat, long, LocalDate.now().minusDays(2)))
        DB.shutdown()
    }

}