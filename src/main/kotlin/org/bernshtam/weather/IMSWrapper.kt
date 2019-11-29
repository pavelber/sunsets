package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader

object IMSWrapper {
    private val parser = Parser.default()

    fun getClouds(p: PointAtTime): IMSClouds {
        var json = DB.get(p, "ims")
        if (json == null) {
            json = IMSConnector.getCloudsParams(p).toJsonString()
            DB.putToDB(p, json, "ims")
        }

        return getIMSClouds(parser.parse(StringReader(json)) as JsonObject)
    }

    @Suppress("UNCHECKED_CAST")
    fun getIMSClouds(json: JsonObject): IMSClouds {
        return IMSClouds(
                (json[IMSConnector.HIGH_CLOUDS_PARAM] as Double)/100.0,
                (json[IMSConnector.MEDIUM_CLOUDS_PARAM] as Double)/100.0,
                (json[IMSConnector.LOW_CLOUDS_PARAM] as Double)/100.0
        )
    }
}