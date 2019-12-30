package org.bernshtam.weather

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

object MapPolygonsProvider {
    private val service = SunSetService()

    suspend fun handle(call: ApplicationCall) {
        call.respond("""
           {
              "type": "FeatureCollection",
              "crs": {
                "type": "name",
                "properties": {
                  "name": "EPSG:3857"
                }
              }, 
                            "features":[
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
              ]}""".trimIndent())
    }
}