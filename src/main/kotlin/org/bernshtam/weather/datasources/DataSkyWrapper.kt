package org.bernshtam.weather.datasources

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.bernshtam.weather.DB
import org.bernshtam.weather.PointAtTime
import java.io.StringReader

object DataSkyWrapper {
    private val parser = Parser.default()

    fun get(p: PointAtTime): JsonObject {
        var json = DB.get(p, "darksky")
        if (json == null) {
            json = DarkSkyConnector.getJsonString(p)
            DB.putToDB(p, json, "darksky")
        }

        return parser.parse(StringReader(json)) as JsonObject
    }
}