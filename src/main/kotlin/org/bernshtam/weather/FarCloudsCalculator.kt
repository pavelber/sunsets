package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import org.bernshtam.weather.datasources.OpenWeatherConnector
import org.bernshtam.weather.utils.Utils
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

object FarCloudsCalculator {
    fun recalculate() {
        val days = (0L..2L).map { LocalDate.now().plusDays(it) }
        val cellPerDay = days.map { DB.getCells(it) }
        val cellsToday = cellPerDay[0]
        try {
            cellsToday.forEach { cell ->

                val lat = cell.latitude
                val long = cell.longitude
                val c = cellPerDay.map { cells -> cells.first { it.latitude == lat && it.longitude == long } }
                val sunsetMoment = days.map { Utils.getSunsetMoment(lat, long, it) }
                val point5Min = sunsetMoment.map { Utils.getPointAfterMinutes(it, 5L, lat, long) }
                val point10Min = sunsetMoment.map { Utils.getPointAfterMinutes(it, 10L, lat, long) }
                val point50km = sunsetMoment.map { Utils.getPointAfterKm(it, lat, long, 50.0) }
                val point100km = sunsetMoment.map { Utils.getPointAfterKm(it, lat, long, 100.0) }
                val json5Min = point5Min.map { OpenWeatherConnector.getJson(it) }
                val json10Min = point10Min.map { OpenWeatherConnector.getJson(it) }
                val json50km = point50km.map { OpenWeatherConnector.getJson(it) }
                val json100km = point100km.map { OpenWeatherConnector.getJson(it) }
                c.forEachIndexed { index, cl ->
                    cl.sun_blocking_near = getClouds(json5Min[index], point5Min[index].time)
                    cl.sun_blocking_far = getClouds(json10Min[index], point10Min[index].time)
                    cl.sunset_near = getClouds(json50km[index], point50km[index].time)
                    cl.sunset_far = getClouds(json100km[index], point100km[index].time)
                }
                c.forEach { DB.updateCell(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getClouds(json: JsonObject, time: ZonedDateTime): Double {
        val t = time.toInstant().epochSecond

        val forecast = json.array<JsonObject>("list")?.first {
            abs(it.long("dt")!! - t) < 30 * 60
        }

        return (forecast?.obj("clouds")?.int("all") ?: 0) / 100.0
    }

}
