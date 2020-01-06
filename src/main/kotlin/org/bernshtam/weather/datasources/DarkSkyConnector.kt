package org.bernshtam.weather.datasources

import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.utils.TokenManager
import java.net.HttpURLConnection
import java.net.URL

object DarkSkyConnector {

    private val secretKey = TokenManager.get("darksky.token")

    fun getJsonString(p: PointAtTime): String {
        val url = URL("https://api.darksky.net/forecast/$secretKey/${p.lat},${p.long},${p.getLocalTime()}")

        with(url.openConnection() as HttpURLConnection) {
            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            return inputStream.bufferedReader().use { it.readText() }
        }

    }
}