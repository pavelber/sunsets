package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

object DarkSkyConnector {
    //val secretKeyJavaap = "6d60c36834d4e75cd60b143de3e43999"
    //val secretKeyBernshtam = "97b2dfeec47e6eae1ba7191d13b0de1f"
    fun getJson(p:PointAtTime): JsonObject {
        val str = getJsonString(p)
        return Parser.default().parse(StringReader(str)) as JsonObject

    }

    fun getJsonString(p:PointAtTime): String {
        val url = URL("https://api.darksky.net/forecast/$secretKeyBernshtam/${p.lat},${p.long},${p.time}")

        with(url.openConnection() as HttpURLConnection) {
           println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            return inputStream.bufferedReader().use { it.readText() }
        }

    }
}