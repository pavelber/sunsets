package org.bernshtam.weather

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

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
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
                    else call.respond(SunSetService.getMarkAndDescription(lat, long))
                }
                get("/locations") {
                    call.respond("""{ 
                            |"Ashdod":{ "lat":31.80, "long":34.65},
                            |"Herzlia":{ "lat":32.16, "long":34.84},
                            |"Palmachim":{ "lat":31.93, "long":34.70}
                            |"Tel Aviv":{ "lat":32.08, "long":34.78}
                            |"Haifa":{ "lat":32.79, "long":34.98}
                            |"Ashkelon":{ "lat":31.66, "long":34.57}
                            |"Netania":{ "lat":32.32, "long":34.85}
                            |}
                        """.trimMargin())
                }
            }
        }
        server.start(wait = true)
    }
}