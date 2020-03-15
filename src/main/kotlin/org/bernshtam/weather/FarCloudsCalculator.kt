package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import org.bernshtam.weather.dal.CellDAL.getCells
import org.bernshtam.weather.dal.CellDAL.updateCell
import org.bernshtam.weather.dal.CoordinatesDAL
import org.bernshtam.weather.datasources.OpenWeatherConnector
import org.bernshtam.weather.utils.Utils
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

object FarCloudsCalculator {
    fun recalculate() {
        val startDay = Utils.getTodayOrTommorrowDependsOnTimeNow()
        val days = (0L..2L).map { startDay.plusDays(it) }
        val cellPerDay = days.map { getCells(it) }
        val cellsToday = cellPerDay[0]

        try {
            cellsToday.forEach { cell ->
                if (IsraelCoordinatesStore.isInsideIsrael(cell)) {
                    val lat = cell.latitude
                    val long = cell.longitude
                    val c = cellPerDay.map { cells -> cells.first { it.latitude == lat && it.longitude == long } }
                    val sunsetMoment = days.map { Utils.getSunsetMoment(lat, long, it) }
                    val point5Min = sunsetMoment.map { Utils.getPointAfterMinutes(it, 5L, lat, long) }
                    val point10Min = sunsetMoment.map { Utils.getPointAfterMinutes(it, 10L, lat, long) }
                    val point50km = sunsetMoment.map { Utils.getPointAfterKm(it, lat, long, 50.0) }
                    val point100km = sunsetMoment.map { Utils.getPointAfterKm(it, lat, long, 100.0) }

                    val json5Min = OpenWeatherConnector.getJson(point5Min[0])
                    val json10Min = OpenWeatherConnector.getJson(point10Min[0])
                    val json50km = OpenWeatherConnector.getJson(point50km[0])
                    val json100km = OpenWeatherConnector.getJson(point100km[0])

                    c.forEachIndexed { index, cl ->
                        cl.sun_blocking_near = getClouds(json5Min, point5Min[index].time)
                        cl.sun_blocking_far = getClouds(json10Min, point10Min[index].time)
                        cl.sunset_near = getClouds(json50km, point50km[index].time)
                        cl.sunset_far = getClouds(json100km, point100km[index].time)
                    }
                    c.forEach { updateCell(it) }
                    print(".")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println()
    }

    private fun getClouds(json: JsonObject, time: ZonedDateTime): Double {
        val t = time.toInstant().epochSecond

        val forecast = json.array<JsonObject>("list")?.firstOrNull{
            abs(it.long("dt")!! - t) < 100 * 60
        }

        if (forecast == null) {
            println("****** NOT FOUND FORECAST FOR THE TIME $time ($t) in json \n${json.toJsonString(prettyPrint = true)}")
        }

        return (forecast?.obj("clouds")?.int("all") ?: 0).toDouble()
    }

}
