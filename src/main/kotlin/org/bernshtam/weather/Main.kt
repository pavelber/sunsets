package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        DataRetrievalSchedulers.runSchedulers()
        val service = SunSetService()

        val server = embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                jackson {
                }
            }
            routing {
                static("static") {
                    resources("html")
                }
                get("/sunset") {
                    val request = call.request
                    val lat = request.queryParameters["lat"]?.toDouble()
                    val long = request.queryParameters["long"]?.toDouble()
                    if (lat == null || long == null) call.respond(HttpStatusCode.BadRequest, "no latitude and longitude")
                    else if (long < 34.0 || long > 36.0) call.respond(HttpStatusCode.BadRequest, "working in israel only")
                    else if (lat < 29.5 || lat > 33.5) call.respond(HttpStatusCode.BadRequest, "working in israel only")
                    else call.respond(service.getMarkAndDescription(lat, long))
                }
                get("/locations") {
                    call.respond(JsonObject(Place.PLACES.mapValues { p -> mapOf("lat" to p.value.lat, "long" to p.value.long) }).toJsonString())
                }
                get("/source") {
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
        }
        server.start(wait = true)
    }
}