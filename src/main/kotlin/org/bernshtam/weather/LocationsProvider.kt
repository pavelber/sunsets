package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

object LocationsProvider {
    suspend fun handle(call: ApplicationCall) {
        call.respond(JsonObject(Place.PLACES.mapValues { p -> mapOf("lat" to p.value.lat, "long" to p.value.long) }).toJsonString())
    }
}