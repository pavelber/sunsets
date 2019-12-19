package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.bernshtam.weather.datasources.IMSStreamProvider

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        DataRetrievalSchedulers.runSchedulers()
        val service = SunSetService(false)

        val server = embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                jackson {
                }
            }
            routing {
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
            }
        }
        server.start(wait = true)
    }
}