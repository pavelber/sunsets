package org.bernshtam.weather.datasources

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.utils.TokenManager
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

object DarkSkyConnector {

    private val secretKey = TokenManager.get("darksky.token")

    fun getJson(p: PointAtTime): JsonObject {
        val str = getJsonString(p)
        return Parser.default().parse(StringReader(str)) as JsonObject

    }

    fun getJsonString(p: PointAtTime): String {
        val url = URL("https://api.darksky.net/forecast/$secretKey/${p.lat},${p.long},${p.getLocalTime()}")

        with(url.openConnection() as HttpURLConnection) {
            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            return inputStream.bufferedReader().use { it.readText() }
        }

    }
}