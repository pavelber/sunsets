package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.bernshtam.weather.Utils.distanceToDegree
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat

object MainTestDB {


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd KK:mm:ss aa")
    private const val R = 6731.0 // km
    private const val deltaPosition = 10.0 // km
    private val deltaDegrees = distanceToDegree(deltaPosition)

    @JvmStatic
    fun main(args: Array<String>) {


        DB.migrate()

        val rehovotLat = 31.897852
        val rehovotLong = 34.8089183
        val date = "2019-10-11"
        val sunset = Main.getTimeOfSunset(rehovotLat, rehovotLong, date)
        val timestampInSec = dateFormat.parse("$date $sunset").time / 1000
        val p = PointAtTime.at(rehovotLat, rehovotLong, timestampInSec)
        val json = "json"//DarkSkyConnector.getJsonString(p)
        DB.putToDB(p, json)
        println(json)
        println(DB.get(p))
        DB.close()
    }


    fun getTimeOfSunset(lat: Double, long: Double, date: String): String {
        val url = URL("https://api.sunrise-sunset.org/json?lat=$lat&lng=$long&date=$date")

        with(url.openConnection() as HttpURLConnection) {

            val json = Parser.default().parse(inputStream) as JsonObject
            val results = json["results"] as JsonObject
            return results.get("sunset") as String
        }
    }


}