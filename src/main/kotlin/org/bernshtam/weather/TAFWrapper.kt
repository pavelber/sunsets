package org.bernshtam.weather

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader

object TAFWrapper {
    fun getCloudsAlt(p: PointAtTime): Int? {
        var json = DB.get(p, "taf")
        if (json == null) {
            json = TAFConnector.getTAFForecast(p)?.toJsonString()
            DB.putToDB(p, json, "taf")
        }

        return if (json != null && json !="null") getTAFCloudsAltitudeInMeters(Parser.default().parse(StringReader(json)) as JsonObject) else null
    }

    @Suppress("UNCHECKED_CAST")
    fun getTAFCloudsAltitudeInMeters(json: JsonObject): Int? {
        val clouds = json["clouds"] as JsonArray<JsonObject?>?
        val cloudsObject = (clouds?.sortedBy { it?.get("altitude") as Int? })?.firstOrNull()
        val altInFeets = cloudsObject?.get("altitude") as Int?
        return if (altInFeets == null) null else (altInFeets.toDouble() * 30.48).toInt()
    }
}