package org.bernshtam.weather.utils

import org.bernshtam.weather.datasources.OpenWeatherConnector
import java.time.LocalDate

object PointsTester {
    @JvmStatic
    fun main(args: Array<String>) {
        val lat = 31.9
        val long = 34.7

        val sunsetMoment =  Utils.getSunsetMoment(lat, long, LocalDate.now()) 
        val point5Min =  Utils.getPointAfterMinutes(sunsetMoment, 5L, lat, long)
        val point10Min =  Utils.getPointAfterMinutes(sunsetMoment, 10L, lat, long)
        val point50km =  Utils.getPointAfterKm(sunsetMoment, lat, long, 50.0)
        val point100km =  Utils.getPointAfterKm(sunsetMoment, lat, long, 100.0)

        println("$lat, $long")
        println("5 min ${point5Min.lat},${point5Min.long}")
        println("10 min ${point10Min.lat},${point10Min.long}")
        println("50 km ${point50km.lat},${point50km.long}")
        println("100 km ${point100km.lat},${point100km.long}")


    }
}