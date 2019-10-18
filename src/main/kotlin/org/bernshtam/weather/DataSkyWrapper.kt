package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.StringReader

object DataSkyWrapper {
    fun get(p: PointAtTime): JsonObject {
        var json = DB.get(p,"darksky")
        if (json == null) {
            json = DarkSkyConnector.getJsonString(p)
            DB.putToDB(p, json,"darksky")
        }

        return Parser.default().parse(StringReader(json)) as JsonObject
    }
}