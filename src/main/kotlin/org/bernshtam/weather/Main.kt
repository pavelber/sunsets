package org.bernshtam.weather

import org.bernshtam.weather.dal.DB
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.bernshtam.weather.utils.TokenManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val prefix = TokenManager.get("api.prefix")
        DB.migrate()
        CoordinatesForRankSaver.save()
        DataRetrievalSchedulers.runSchedulers()

        val server = embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                jackson {
                }
            }
            routing {
                static("$prefix/static") {
                    resources("html")
                }
                get("$prefix/sunset") {
                    SunsetTextProvider.handle(call)
                }
                get("$prefix/locations") {
                    LocationsProvider.handle(call)
                }
                get("$prefix/source") {
                    MapPolygonsProvider.handle(call)
                }
            }
        }
        server.start(wait = true)
    }


}