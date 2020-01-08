package org.bernshtam.weather.datasources

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.common.util.concurrent.RateLimiter
import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.utils.TokenManager
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

object OpenWeatherConnector {

    private val secretKey = TokenManager.get("openweather.token")
    private val rateLimiter =  RateLimiter.create(1.0)

    fun getJson(p: PointAtTime): JsonObject {
        val attempts = 3
        for (i in 0..attempts) {
            try {
                val str = getJsonString(p)
                return Parser.default().parse(StringReader(str)) as JsonObject
            } catch (e: Exception) {
                println(e)
                Thread.sleep(1000 * 60)
            }
        }
        throw RuntimeException("Can't access open weather")
    }

    fun getJsonString(p: PointAtTime): String {
        rateLimiter.acquire()
        val url = URL("https://api.openweathermap.org/data/2.5/forecast?lat=${p.lat}&lon=${p.long}&APPID=$secretKey")
        with(url.openConnection() as HttpURLConnection) {
           // println(url)
            require(responseCode == 200) { responseMessage }
            return inputStream.bufferedReader().use { it.readText() }
        }

    }
}