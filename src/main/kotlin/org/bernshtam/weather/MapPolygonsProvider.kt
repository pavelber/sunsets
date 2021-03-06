package org.bernshtam.weather

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import io.ktor.application.ApplicationCall
import io.ktor.response.respond
import org.bernshtam.weather.dal.CellDAL.getCells
import java.time.LocalDate

object MapPolygonsProvider {


    suspend fun handle(call: ApplicationCall) {
        val request = call.request
        val day = request.queryParameters["day"]?.toLong() ?: 0
        val map = request.queryParameters["map"]
        val value = when (map) {
            "low" -> { c: Cell -> "0,0,0,${c.low / 100.0}" }
            "medium" -> { c: Cell -> "0,0,255,${c.medium / 100.0}" }
            "high" -> { c: Cell -> "255,165,0,${c.high / 100.0}" }
            "sunset" -> { c: Cell -> "255,0,0,${(c.rank ?: 0.0) / 100.0}" }
            else -> { c: Cell -> "0,0,0,${c.low}" }
        }
        val filter = when (map) {
            "sunset" -> { c: Cell -> c.rank != null }
            else -> { c: Cell -> true }
        }
        val cells = getCells(LocalDate.now().plusDays(day))

        call.respond("""
           {
              "type": "FeatureCollection",
              "crs": {
                "type": "name",
                "properties": {
                  "name": "EPSG:4326"
                }
              }, 
                            "features":${convertToJson(cells, filter, value)}}""".trimIndent())
    }

    private fun convertToJson(cells: List<Cell>, filter: (Cell) -> Boolean, value: (Cell) -> String): String {
        return cells.filter(filter).map { c ->
            JsonObject().also {
                it["type"] = "Feature"
                it["properties"] = JsonObject().also { p ->
                    p["color"] = "rgba(${value(c)})"
                }
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
}