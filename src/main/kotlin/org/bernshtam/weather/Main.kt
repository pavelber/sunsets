package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

import org.bernshtam.weather.Utils.distanceToDegree
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat

object Main {


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd KK:mm:ss aa")
    private const val R = 6731.0 // km
    private const val deltaPosition = 10.0 // km
    private val deltaDegrees = distanceToDegree(deltaPosition)

    @JvmStatic
    fun main(args: Array<String>) {

        DB.migrate()
        val rehovotLat = 31.897852
        val rehovotLong = 34.8089183
        val dates = listOf("2017-12-12",
                "2018-02-15", "2019-01-12",
                "2017-12-27", "2016-02-11",
                "2019-10-05",
                //"2018-12-16",
                "2018-03-18", "2019-10-10",
                "2019-10-11", "2017-01-20","2015-06-19").sorted()
        //val dates = listOf("2019-10-08","2019-10-09","2019-10-10","2019-10-11","2019-10-12","2019-10-13","2019-10-14").sorted()
        dates.forEach { date ->
            println()
            println()
            println(date)
            val sunset = getTimeOfSunset(rehovotLat, rehovotLong, date)
            val timestampInSec = dateFormat.parse("$date $sunset").time / 1000
            val cloudsHeight = listOf(2.0, 4.0)
            cloudsHeight.forEach { h ->
                val deltaSun = Math.acos(R / (R + h)) * 180 / Math.PI
                //  println("$h: $deltaSun ${degreeToDistance(deltaSun)}")


                val cloudsIn = getClouds(rehovotLat, rehovotLong, timestampInSec)

                val cloudsSea = getClouds(rehovotLat, rehovotLong - deltaSun, timestampInSec)
                print("\t$h km:\t\t($cloudsIn,$cloudsSea)")

            }


        }
    }

    fun getClouds(lat: Double, long: Double, time: Long): Double? {
        val json = DataSkyWrapper.get(PointAtTime.at(lat, getLongtitude(long), time))
        val currently = json["currently"] as JsonObject
        return getClouds(currently)
    }

    private fun getClouds(currently: JsonObject): Double? {
        val cStr = currently["cloudCover"]
        if (cStr != null) return Utils.getDoubleFromJson(cStr)
        return when (currently["icon"]) {
            "clear-day" -> 0.0
            "partly-cloudy-day" -> 50.0
            else -> 100.0
        }
    }




    fun getTimeOfSunset(lat: Double, long: Double, date: String): String {
        val url = URL("https://api.sunrise-sunset.org/json?lat=$lat&lng=$long&date=$date")

        with(url.openConnection() as HttpURLConnection) {

            val json = Parser.default().parse(inputStream) as JsonObject
            val results = json["results"] as JsonObject
            return results.get("sunset") as String
        }
    }

    private fun getLongtitude(long: Double): Double {
        //return if (long < 34.32) 34.32 else long
        return long
    }

}