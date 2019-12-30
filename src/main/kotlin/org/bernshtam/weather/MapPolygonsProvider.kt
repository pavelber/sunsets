package org.bernshtam.weather

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import io.ktor.application.ApplicationCall
import io.ktor.response.respond
import java.time.LocalDate

object MapPolygonsProvider {

    suspend fun handle(call: ApplicationCall) {
        val cells = DB.getCells(LocalDate.now())
        call.respond("""
           {
              "type": "FeatureCollection",
              "crs": {
                "type": "name",
                "properties": {
                  "name": "EPSG:4326"
                }
              }, 
                            "features":${convertToJson(cells)}}""".trimIndent())
    }

    private fun convertToJson(cells: List<Cell>): String {
        return cells.map { c ->
            JsonObject().also {
                it["type"] = "Feature"
                it["properties"] = JsonObject().also { p ->
                    p["color"] = "rgba(0, 0, 255, ${c.high / 100.0})"
                }//"""{ "color": "rgba(0, 0, 255, ${c.high})" }"""
                it["geometry"] = JsonObject().also { g ->
                    g["type"] = "Polygon"
                    g["coordinates"] = JsonArray(listOf(JsonArray(listOf(
                            JsonArray(listOf(c.longitude - c.square_size / 2, c.latitude - c.square_size / 2)),
                            JsonArray(listOf(c.longitude + c.square_size / 2, c.latitude - c.square_size / 2)),
                            JsonArray(listOf(c.longitude + c.square_size / 2, c.latitude + c.square_size / 2)),
                            JsonArray(listOf(c.longitude - c.square_size / 2, c.latitude + c.square_size / 2)),
                            JsonArray(listOf(c.longitude - c.square_size / 2, c.latitude - c.square_size / 2))))))
                }
            }
        }.joinToString(separator = ",", prefix = "[", postfix = "]") { it.toJsonString() }
    }
/*[
{
    "type": "Feature",
    "properties": { "color": "rgba(0, 0, 255,0.4)" },
    "geometry": {
       "type": "Polygon",
       "coordinates": [
               [[-5e6, 6e6], [-5e6, 8e6], [-3e6, 8e6], [-3e6, 6e6], [-5e6, 6e6]]
          ]
        }
}
]*/
}