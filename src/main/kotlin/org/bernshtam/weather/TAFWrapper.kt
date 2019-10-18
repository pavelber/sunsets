package org.bernshtam.weather

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader

object TAFWrapper {
    fun getCloudsAlt(p: PointAtTime): Int? {
        var json = DB.get(p,"taf")
        if (json == null) {
             json = TAFConnector.getJsonString(p)
            DB.putToDB(p, json,"taf")
        }

        return getTAFCloudsAltitudeInKm(Parser.default().parse(StringReader(json)) as JsonObject)
    }

    fun getTAFCloudsAltitudeInKm(json:JsonObject): Int? {
        val clouds = json?.get("clouds") as JsonArray<JsonObject?>?
        val cloudsObject = clouds?.get(0)
        val altInFeets = cloudsObject?.get("altitude") as Int?
        return if (altInFeets==null) null else (altInFeets.toDouble()*0.3048).toInt()
    }
}