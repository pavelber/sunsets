package org.bernshtam.weather.datasources

import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.utils.TokenManager
import java.net.HttpURLConnection
import java.net.URL

object GeoNamesConnector {

    private val username = TokenManager.get("geonames.user")

    fun getCountry(latitide:Double, longtitude:Double): String {
        val url = URL("http://api.geonames.org/countryCode?lat=$latitide&lng=$longtitude&radius=0&username=${username}")

        with(url.openConnection() as HttpURLConnection) {
            connectTimeout = 20000
            return inputStream.bufferedReader().use { it.readText() }
        }

    }
}