package org.bernshtam.weather.datasources

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.utils.TokenManager
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime


@Suppress("UNCHECKED_CAST")
object TAFConnector {

    private val secretKey = TokenManager.get("avwx.token")

    fun getJsonString(p: PointAtTime): String {
        val url = URL("https://avwx.rest/api/taf/${p.lat},${p.long}?reporting=true&format=json&options=info&airport=false&token=$secretKey")

        with(url.openConnection() as HttpURLConnection) {
            connectTimeout = 20000
            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            return inputStream.bufferedReader().use { it.readText() }
        }
    }


    fun getTAFForecast(p: PointAtTime): JsonObject? {
        val str = getJsonString(p)
        val json = Parser.default().parse(StringReader(str)) as JsonObject
        val forecast: JsonArray<JsonObject> = json.get("forecast") as JsonArray<JsonObject>
        val timePeriod = forecast.find { f ->
            inTimeSlice(p.time, f["start_time"] as JsonObject, f["end_time"] as JsonObject)
        }

        return timePeriod
    }

    private fun inTimeSlice(time: ZonedDateTime, start: JsonObject, end: JsonObject): Boolean {
        val start = strToDate(start["dt"] as String)
        val end = strToDate(end["dt"] as String)
       // println("$time between $start and $end")
        return start.isBefore(time) && end.isAfter(time)
    }

    private fun strToDate(str: String): ZonedDateTime {
        return try {
            ZonedDateTime.parse(str)
        } catch (e: Exception) {
            ZonedDateTime.parse(str.removeRange(str.length - 1, str.length))
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val message = getTAFForecast(PointAtTime.at(31.93, 34.70, LocalDateTime.now().atZone(ZoneId.systemDefault())))
        println(message)
    }
}