package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import io.ktor.application.ApplicationCall
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
        DB.migrate()
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
                    SunsetTextProvider.handle(call)
                }
                get("/locations") {
                    LocationsProvider.handle(call)
                }
                get("/source") {

                }
            }
        }
        server.start(wait = true)
    }


}