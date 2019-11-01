package org.bernshtam.weather

import org.bernshtam.weather.SunSetService.getMarkAndDescription


object MainFromCoordinates {


    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
        val lat = 31.68
        val long = 34.55
        println(getMarkAndDescription(lat, long))
        DB.shutdown()
    }

}