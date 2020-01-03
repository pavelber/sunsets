package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import org.bernshtam.weather.datasources.OpenWeatherConnector
import org.bernshtam.weather.utils.Utils
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.abs

object FarCloudsCalculator {
    fun recalculate() {
        val now = LocalDate.now()
        val now_1 = LocalDate.now().plusDays(1)
        val now_2 = LocalDate.now().plusDays(2)
        val cells = DB.getCells(now)
        val cells_1 = DB.getCells(now_1)
        val cells_2 = DB.getCells(now_2)
        try {
        cells.forEach { c ->

                println(c)
                val lat = c.latitude
                val long = c.longitude
                val c_1 = cells_1.first { it.latitude == lat && it.longitude == long }
                val c_2 = cells_2.first { it.latitude == lat && it.longitude == long }
                val sunsetMoment = Utils.getSunsetMoment(lat, long, now)
                val sunsetMoment_1 = Utils.getSunsetMoment(lat, long, now_1)
                val sunsetMoment_2 = Utils.getSunsetMoment(lat, long, now_2)
                val point5Min = Utils.getPointAfterMinutes(sunsetMoment, 5L, lat, long)
                val point5Min_1 = Utils.getPointAfterMinutes(sunsetMoment_1, 5L, lat, long)
                val point5Min_2 = Utils.getPointAfterMinutes(sunsetMoment_2, 5L, lat, long)
                val point10Min = Utils.getPointAfterMinutes(sunsetMoment, 10L, lat, long)
                val point10Min_1 = Utils.getPointAfterMinutes(sunsetMoment_1, 10L, lat, long)
                val point10Min_2 = Utils.getPointAfterMinutes(sunsetMoment_2, 10L, lat, long)
                val point50km = Utils.getPointAfterKm(sunsetMoment, lat, long, 50.0)
                val point50km_1 = Utils.getPointAfterKm(sunsetMoment_1, lat, long, 50.0)
                val point50km_2 = Utils.getPointAfterKm(sunsetMoment_2, lat, long, 50.0)
                val point100km = Utils.getPointAfterKm(sunsetMoment, lat, long, 100.0)
                val point100km_1 = Utils.getPointAfterKm(sunsetMoment_1, lat, long, 100.0)
                val point100km_2 = Utils.getPointAfterKm(sunsetMoment_2, lat, long, 100.0)
                val json5Min = OpenWeatherConnector.getJson(point5Min)
                c.sunset_near = getClouds(json5Min,point5Min.time)
                c_1.sunset_near = getClouds(json5Min,point5Min_1.time)
                c_2.sunset_near = getClouds(json5Min,point5Min_2.time)
                val json10Min = OpenWeatherConnector.getJson(point10Min)
                c.sunset_far = getClouds(json10Min,point10Min.time)
                c_1.sunset_far = getClouds(json10Min,point10Min_1.time)
                c_2.sunset_far = getClouds(json10Min,point10Min_2.time)
                val json50km = OpenWeatherConnector.getJson(point50km)
                c.sun_blocking_near = getClouds(json50km,point50km.time)
                c_1.sun_blocking_near = getClouds(json50km,point50km_1.time)
                c_2.sun_blocking_near = getClouds(json50km,point50km_2.time)
                val json100km = OpenWeatherConnector.getJson(point100km)
                c.sun_blocking_far = getClouds(json100km,point100km.time)
                c_1.sun_blocking_far = getClouds(json100km,point100km_1.time)
                c_2.sun_blocking_far = getClouds(json100km,point100km_2.time)
                DB.updateCell(c)
                DB.updateCell(c_1)
                DB.updateCell(c_2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getClouds(json: JsonObject, time: ZonedDateTime): Double {
        val t = time.toInstant().epochSecond

        val forecast = json.array<JsonObject>("list")?.first {
            abs(it.long("dt") !! - t) < 30 * 60
        }

        return (forecast?.obj("clouds")?.int("all")?:0)/100.0
   }

}
